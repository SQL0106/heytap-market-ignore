# HeyTap Market Permanent Ignore

让 realme / OPPO 软件商店（`com.heytap.market`）的「忽略更新」永久生效。

商店原本只记录「包名 + 目标版本号」，当已安装版本追上该版本号后忽略状态自动失效，更新条目再次出现。本模块在用户点一次「忽略更新」时记住包名，之后：

- `isIgnored(pkg)` 恒为 `true`
- 待更新列表中过滤掉该包
- 在商店的「忽略更新」页取消忽略后恢复

## 实现

基于 **libxposed API 102**（`io.github.libxposed:api:102.0.0`），无旧版 Xposed API。

Hook 点：

| 类 | 方法 | 作用 |
|---|---|---|
| `a.a.kch`（混淆实现，26.11.3_CN） | `ބ(String)Z` / `ޏ(String,J)V` / `ސ(String)V` | 核心判定、写入、删除 |
| `com.heytap.cdo.client.download.util.b`（稳定门面） | `isIgnored` / `setIgnore` / `setNoIgnore` / `getUpgradeInfoBeansNoIgnore` | 双保险 + 列表过滤 |

包名集合保存在商店进程自己的 `SharedPreferences`（`market_permanent_ignore`）中。

## 构建

GitHub Actions（`.github/workflows/android.yml`）原生 `assembleDebug`，产物为该 workflow 的 artifact。

工具链：

- JDK 21、Gradle 9.9.3（wrapper）、AGP 9.5.3
- `actions/checkout@v6`、`actions/setup-java@v6`、`gradle/actions/setup-gradle@v6`、`actions/upload-artifact@v7`（均 Node 24 运行时，无弃用警告）
- 依赖源：`google()` + Google 官方 Maven Central 镜像（`maven-central.storage-download.googleapis.com`）+ `mavenCentral()`。镜像放在前面是为了绕开 CI 共享 runner IP 被 Maven Central 限流（429）的问题。

## 安装

1. 安装 APK。
2. 在 LSPosed 中启用模块，作用域勾选「软件商店」（`com.heytap.market`）。
3. 强制停止软件商店后重新打开。
4. 对想永久忽略的应用点一次「忽略更新」。

## 注意

- 只针对 `com.heytap.market` 26.11.3_CN 验证；商店大版本更新后混淆类名可能变化，届时更新 `IMPL_CLASS`。
- 模块不申请任何权限，不联网。
