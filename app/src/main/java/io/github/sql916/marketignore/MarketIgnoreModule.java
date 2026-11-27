package io.github.sql916.marketignore;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

/**
 * Turns the market's one-shot "ignore this update" into a permanent ignore.
 *
 * The store stores (pkg, versionCode) and compares it with the installed
 * version, so the entry reappears after the app is updated. We remember the
 * package separately: once the user ignores it, isIgnored always returns true
 * and the package is filtered out of the "updatable" list. Un-ignoring in the
 * store UI removes it again.
 */
public class MarketIgnoreModule extends XposedModule {
    private static final String TAG = "MarketIgnore";
    private static final String TARGET = "com.heytap.market";
    private static final String PREFS = "market_permanent_ignore";

    // Obfuscated static implementation, present in 26.11.3_CN.
    private static final String IMPL_CLASS = "a.a.kch";
    private static final String M_IS_IGNORED = "\u0784"; // (String)Z
    private static final String M_SET_IGNORE = "\u078F"; // (String, long)V
    private static final String M_SET_NO_IGNORE = "\u0790"; // (String)V

    // Stable, unobfuscated facade.
    private static final String FACADE_CLASS = "com.heytap.cdo.client.download.util.b";

    private volatile SharedPreferences prefs;
    private ClassLoader classLoader;

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageLoadedParam param) {
        if (!TARGET.equals(param.getPackageName())) return;
        Log.i(TAG, "onPackageLoaded: " + param.getPackageName());
        classLoader = param.getDefaultClassLoader();
        hook(IMPL_CLASS, M_IS_IGNORED, String.class);
        hook(IMPL_CLASS, M_SET_IGNORE, String.class, long.class);
        hook(IMPL_CLASS, M_SET_NO_IGNORE, String.class);
        hook(FACADE_CLASS, "isIgnored", String.class);
        hook(FACADE_CLASS, "setIgnore", String.class, long.class);
        hook(FACADE_CLASS, "setNoIgnore", String.class);
        hook(FACADE_CLASS, "getUpgradeInfoBeansNoIgnore");
    }

    private void hook(String className, String methodName, Class<?>... args) {
        try {
            Class<?> c = Class.forName(className, false, classLoader);
            Method m = c.getDeclaredMethod(methodName, args);
            m.setAccessible(true);
            final boolean isIgnored = "isIgnored".equals(methodName) || M_IS_IGNORED.equals(methodName);
            final boolean isSetIgnore = "setIgnore".equals(methodName) || M_SET_IGNORE.equals(methodName);
            final boolean isSetNoIgnore = "setNoIgnore".equals(methodName) || M_SET_NO_IGNORE.equals(methodName);
            final boolean isList = "getUpgradeInfoBeansNoIgnore".equals(methodName);

            hook(m).setId("marketignore:" + className + "#" + methodName).intercept(chain -> {
                String pkg = chain.getArgs().isEmpty() ? null : String.valueOf(chain.getArg(0));
                if (isIgnored) {
                    if (isPermanent(pkg)) {
                        Log.i(TAG, "isIgnored(" + pkg + ") -> forced true");
                        return true;
                    }
                } else if (isSetIgnore) {
                    Object ver = chain.getArgs().size() > 1 ? chain.getArg(1) : null;
                    if (pkg != null && ver instanceof Number) {
                        Log.i(TAG, "setIgnore(" + pkg + ", " + ver + ") -> remembered");
                        remember(pkg, ((Number) ver).longValue());
                    }
                } else if (isSetNoIgnore) {
                    Log.i(TAG, "setNoIgnore(" + pkg + ") -> forgotten");
                    forget(pkg);
                } else if (isList) {
                    Object result = chain.proceed();
                    if (result instanceof List) {
                        List<Object> filtered = filter((List<?>) result);
                        Log.i(TAG, "getUpgradeInfoBeansNoIgnore: " + ((List<?>) result).size()
                                + " -> " + filtered.size());
                        return filtered;
                    }
                    return result;
                }
                return chain.proceed();
            });
            Log.i(TAG, "hooked " + className + "#" + methodName);
        } catch (Throwable t) {
            Log.w(TAG, "skip hook " + className + "#" + methodName + ": " + t);
        }
    }

    private List<Object> filter(List<?> source) {
        List<Object> out = new ArrayList<>(source.size());
        for (Object bean : source) {
            if (!isPermanent(packageOf(bean))) out.add(bean);
        }
        return out;
    }

    private static String packageOf(Object bean) {
        if (bean == null) return null;
        for (String name : new String[]{"getPkgName", "getPackageName"}) {
            try {
                Object v = bean.getClass().getMethod(name).invoke(bean);
                if (v instanceof String) return (String) v;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private boolean isPermanent(String pkg) {
        SharedPreferences p = prefs();
        return p != null && pkg != null && p.contains(pkg);
    }

    private void remember(String pkg, long version) {
        SharedPreferences p = prefs();
        if (p != null) p.edit().putLong(pkg, version).apply();
    }

    private void forget(String pkg) {
        SharedPreferences p = prefs();
        if (p != null && pkg != null) p.edit().remove(pkg).apply();
    }

    private SharedPreferences prefs() {
        SharedPreferences p = prefs;
        if (p != null) return p;
        synchronized (this) {
            if (prefs != null) return prefs;
            try {
                Class<?> at = Class.forName("android.app.ActivityThread", false, classLoader);
                Method current = at.getDeclaredMethod("currentApplication");
                current.setAccessible(true);
                Application app = (Application) current.invoke(null);
                if (app != null) {
                    prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                }
            } catch (Throwable t) {
                Log.w(TAG, "no application context yet: " + t);
            }
            return prefs;
        }
    }
}
