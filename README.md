# 百姓话题·轻阅 G

Android原生客户端，自用G版，当前版本 **0.2.2-G**，包名`local.my0511.lite`，支持Android8及以上。

## 下载

- [G 0.2.2 APK](releases/G-0.2.2/My0511Lite-G-0.2.2-test.apk)
- [哈希校验](releases/G-0.2.2/My0511Lite-G-0.2.2-hashes.txt)
- [签名信息](releases/G-0.2.2/My0511Lite-G-0.2.2-signature.txt)

## 功能

只读百姓话题，隐藏直聘推广和置顶列表；账号登录状态、手动多账户切换；手写和`===`分隔粘贴直接加入发送队列，预览可选。支持发布时段、指定时间范围、间隔、每账户及每帖上限、防连帖、运行日志和可选的系统后台调度。结果不确定的任务不会自动重发。

三项普通权限：联网、查询网络状态、接收系统重启。无广告、统计、推送SDK。会话在本机用AndroidKeyStore加密，不保存密码。本仓库不含用户会话、账号运行数据、私人截图或签名私钥。

## 构建与检查

这是轻量Java原生工程，使用SDK构建工具直接构建，并非Gradle/Kotlin工程。默认引导脚本面向macOS ARM64，下载JDK21、AndroidSDK35和Jsoup1.18.3。

```sh
python3 scripts/bootstrap.py
python3 scripts/test.py
python3 scripts/build.py
```

主机测试还需要Node.js。可使用`MY0511_TOOLCHAIN`和`MY0511_JDK`指向已有工具链。首次构建会在本机`work/signing`生成独立的开发密钥；该密钥仅用于自用测试，不应上传。仓库APK沿用原G证书，但证书私钥没有上传，因此新环境自行构建的包通常不能直接覆盖原签名APK。构建产物位于`outputs/`。

如需运行官方编辑器合成测试，先运行`python3 scripts/fetch-editor-fixture-G.py`下载公开脚本并核对固定哈希，然后构建`python3 scripts/build-device-test.py`。官网脚本仅在本机测试使用，未再分发在源码或用户APK内。测试不需要论坛密码。

## 验证与限制

见[本次调整说明](docs/G-0.2.2-调整说明.md)和[测试输出](docs/G-0.2.2-测试结果.txt)。用户已确认曾有帖子实际发出，但后续页面返回503导致回执不确定；503不能作为帖子未发出的证明，遇此情况应先到论坛核实。本版尚未修复完整的503后自动回执恢复。

锁屏、省电、重启后的系统调度不保证准点。论坛原生手机型号尾注模拟尚未实现可验证效果，普通文字签名不等同于原生型号模拟。

Jsoup第三方许可保留于[third-party-notices.txt](app/src/main/assets/third-party-notices.txt)。仓库为私有自用，不授予未声明的公开再分发许可。
