# 阿里云移动推送Demo APP Android版
[![maven version](https://img.shields.io/badge/Maven-3.9.2-brightgreen.svg)](https://mhub.console.aliyun.com/downloadSdk) [![platform](https://img.shields.io/badge/platform-android-lightgrey.svg)](https://developer.android.com/index.html) [![language](https://img.shields.io/badge/language-java-orange.svg)](http://www.oracle.com/technetwork/java/index.html) [![website](https://img.shields.io/badge/website-CloudPush-red.svg)](https://www.aliyun.com/product/cps)


<div align="center">
<img src="./assets/logo.png">
</div>

阿里移动推送（Alibaba Cloud Mobile Push）是基于大数据的移动智能推送服务，帮助App快速集成移动推送的功能，在实现高效、精确、实时的移动推送的同时，极大地降低了开发成本。让开发者最有效地与用户保持连接，从而提高用户活跃度、提高应用的留存率。

## 产品特性

-   *高效稳定*——与手机淘宝使用相同架构，基于阿里集团高可用通道。该通道日均消息发送量可达30亿，目前活跃使用的用户达1.8亿。
-   *高到达率*——Android 智能通道保活，多通道支持保证推送高到达率。
-   *精确推送*——基于阿里大数据处理技术，实现精确推送。
-   *应用内消息推送*——支持 Android 与 iOS 应用内私有通道，保证透传消息高速抵达。


## 使用方法

### 1 创建APP

您首先需要登入移动推送控制台，创建一个APP实体以对应您准备使用的Demo APP。关于APP创建的指引文档可以参考：

>[创建项目和App](https://help.aliyun.com/document_detail/436513.html?spm=a2c4g.434660.0.0.255d4289JUbMoM#section-8am-xwe-iqh)

### 2. 下载Demo工程

将工程克隆或下载到本地：

```shell
git clone https://github.com/aliyun/alicloud-android-demo.git
```

并通过Android Studio加载后您可以看到如下目录：

<div align="center">
<img src="./assets/project_struct.png">
</div>


其中`mpush_android_demo`即为移动推送的Demo APP。

mpush_android_demo已经完成了移动推送SDK的集成工作，但我们还是建议您仔细阅读移动推送的集成文档

>[Android SDK配置文档](https://help.aliyun.com/document_detail/51056.html)

**当您在使用您自己的APP集成移动推送遇到问题时，您可以对比下demo APP的配置情况。**

###  3. 配置APP信息

#### 3.1 配置AppKey、AppSecret

为了使Demo APP能够正常运行，您还需要配置您的appkey/appsecret信息。
您可以在移动推送控制台，点击您在第一步创建的APP，点击设置按钮，如图所示：

<div align="center">
<img src="./assets/appkey_location.png">
</div>

在下述`AndroidManifest.xml`代码片段中用您的appkey/appsecret替换`********`字段占据的参数。

```xml
<meta-data android:name="com.alibaba.app.appkey" android:value="********"/> <!-- 请填写你自己的- appKey -->
<meta-data android:name="com.alibaba.app.appsecret" android:value="********"/> <!-- 请填写你自己的appSecret -->
```

#### 3.2 配置包名

将`build.gradle`文件中的`applicationId`参数改成所创建App的包名：

```xml
android {
    compileSdkVersion 23
    buildToolsVersion "23.0.1"

    defaultConfig {
        applicationId "********" // 填写所创建App的包名
        minSdkVersion 11
        targetSdkVersion 23
        versionCode 1
        versionName "1.0"
    }
	......
}
```

### 4. 运行程序

若程序编译通过，在运行时点击`注册并接收推送`,打印出类似如下日志，则说明集成成功：

#### 4.1 回调方法callback.onSuccess()被调用。在logcat日志中，输入tag：MPS：

```
2024-07-03 10:34:48.630 14509-9747  [MPS]                   com.aliyun.emas.pocdemo              I  agoo init success.
2024-07-03 10:34:48.631 14509-9749  [MPS]                   com.aliyun.emas.pocdemo              D  register agoo result 错误码：PUSH_00000, 错误：success
2024-07-03 10:34:48.631 14509-9749  MPS:AppRegister         com.aliyun.emas.pocdemo              I  connState=2;estimatedTime=384;response{msg: success, code: PUSH_00000}
2024-07-03 10:34:48.631 14509-9749  MPS:AppRegister         com.aliyun.emas.pocdemo              D  Looping handleMessage: 1
2024-07-03 10:34:48.631 14509-14509 [MPS]                   com.aliyun.emas.pocdemo              I  errorCode:错误码：PUSH_00000, 错误：success
```
#### 4.2 确认cloud channel初始化正常，在logcat日志中，输入awcn关键字：

```
2024-07-03 10:36:57.464  8890-10129 EMASNAccs_NetworkSdk    com.aliyun.emas.pocdemo              I  [awcn.TnetSpdySession]  statusCode:200
2024-07-03 10:36:57.465  8890-10129 EMASNAccs_NetworkSdk    com.aliyun.emas.pocdemo              I  [awcn.TnetSpdySession]  response headers:{date=[Wed, 03 Jul 2024 02:36:57 GMT], content-length=[0], server=[Tengine/Aserver/3.0.413_20221027005707], s-accs-retcode=[SUCCESS], :status=[200], x-workerid=[360290169770599862], x-at=[ZoS4k5ejQckDADGQGBlEBBUB3347866231719988617]}
2024-07-03 10:36:57.467  8890-10129 EMASNAccs_NetworkSdk    com.aliyun.emas.pocdemo              E  [awcn.Session]|[seq:334786623.AWCN1_1] notifyStatus status:AUTH_SUCC
```
#### 4.3 确认deviceId获取正常：在初始化成功后使用cloudPushService.getDeviceId()可以成功获取deviceId。

#### 4.4 如果注册服务器连接失败，则调用callback.onFailed()方法，并且自动进行重新注册，直到onSuccess为止（重试规则会由网络切换等时机自动触发）。

## 联系我们

-   官网：[移动推送](https://www.aliyun.com/product/cps)
-   钉钉技术支持：11795523（钉钉群号）
-   官方技术博客：[阿里云移动服务](https://yq.aliyun.com/teams/32)