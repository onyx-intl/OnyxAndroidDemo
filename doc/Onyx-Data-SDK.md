Data SDK for Onyx-Intl Android E-Ink devices

Latest version is 1.0.1, can be referenced with following gradle statement:

    compile 'com.onyx.android.sdk:onyxsdk-base:1.3.7'
    compile 'com.onyx.android.sdk:onyxsdk-data:1.0.1'

    base need dependencies
    apt "com.github.Raizlabs.DBFlow:dbflow-processor:$rootProject.dbflowVersion"
    compile "com.github.Raizlabs.DBFlow:dbflow-core:$rootProject.dbflowVersion"
    compile "com.github.Raizlabs.DBFlow:dbflow:$rootProject.dbflowVersion"
    compile "org.apache.commons:commons-collections4:$rootProject.apachecommonsVersion"
    compile "com.alibaba:fastjson:$rootProject.fastjsonVersion"
    compile "com.squareup.retrofit2:retrofit:$rootProject.retrofit2Version"
    compile "com.liulishuo.filedownloader:library:$rootProject.filedownloaderVersion"
    compile "com.squareup.okhttp:okhttp-urlconnection:$rootProject.okhttpurlconnectionVersion"
    compile "com.aliyun.dpa:oss-android-sdk:$rootProject.aliyunOssSdkVersion"
    compile "com.tencent.mm.opensdk:wechat-sdk-android-with-mta:$rootProject.wechatSdkWithMtaVersion"

Change logs:.
* 1.0.1
    Add onyxsdk-data for WechatManager/OssManager/FileDownloader/OTAManager/part Request