## 商店永久忽略更新 v1.0.0

让 realme / OPPO 软件商店（`com.heytap.market`）的「忽略更新」不再只生效一次。

商店原本把忽略记成 `(包名, 目标版本号)`，一旦已装版本追上那个版本号，忽略自动失效，更新条目又冒出来。本模块在你点一次「忽略更新」时把包名单独记下来，之后始终把它从更新列表里过滤掉，直到你在商店的「忽略更新」页手动取消。

### 下载哪个

| 文件 | 说明 |
|---|---|
| `heytap-market-ignore-release.apk` | 日常使用，无日志输出，推荐 |
| `heytap-market-ignore-debug.apk` | 排查问题用，会往 logcat 打 `MarketIgnore` 日志 |

两个包用同一把密钥签名，可以互相覆盖安装。

### 安装

1. 安装 APK（需要 LSPosed，模块基于 **libxposed API 102**）。
2. LSPosed → 模块 → 启用「商店永久忽略更新」。
3. 作用域勾选 **软件商店**（`com.heytap.market`）。
4. 强制停止软件商店后重新打开。
5. 对想永久忽略的应用点一次「忽略更新」。

### 实现

Hook 未混淆门面 `com.heytap.cdo.client.download.util.b`：

- `setIgnore(pkg, ver)` → 记入模块自己的 `SharedPreferences`
- `isIgnored(pkg)` → 命中集合直接返回 `true`
- `setNoIgnore(pkg)` → 移出集合
- `getUpgradeInfoBeansNoIgnore()` → 从待更新列表里过滤掉集合内的包

### 已知限制

- 只针对 `com.heytap.market` **26.11.3_CN**（versionCode 261103）验证，商店大版本更新后门面类名可能变化。
- 混淆实现 `a.a.kch` 不在运行时默认 ClassLoader 里（商店用 Hera 插件框架动态加载业务 dex），因此只 hook 门面；商店内部不走门面的路径（例如部分红点/自动更新逻辑）可能不受影响。
- 模块不申请权限、不联网，数据只写在商店自己的数据目录里。
