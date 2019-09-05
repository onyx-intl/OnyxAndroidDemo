Base SDK for Onyx-Intl Android E-Ink devices

Latest version is 1.4.3.7, can be referenced with following gradle statement:
```gradle
  compile ('com.onyx.android.sdk:onyxsdk-base:1.4.3.7')
```

Change logs:
* 1.4.3.7

   Add Dict Query API, for more detail to see [DictionaryUtils-API](./DictionaryUtils-API.md)
* 1.4.3.4

  Add method `setWebViewContrastOptimize(WebView webview, boolean enable)` prevent webview enter A2 mode
* 1.3.2

    Small improvements
* 1.3.1
    
    Add OTAUtil and DeviceInfoUtil for onyxsdk-data ota
* 1.3.0

    Expose public names for internal development convenience, for 3rdparty developers, it's recommended to use APIs inside com.onyx.android.sdk.api package which are more stable.
* 1.2.4

    Small fixes
* 1.2.3

    Add [EpdDeviceManager](./EpdDeviceManager.md)
* 1.2.2

    Add [DeviceEnvironment](./DeviceEnvironment.md)
* 1.2.1

    Add [Scribble API](./Scribble-API.md) to [EpdController](./EpdController.md)

* 1.2.0

    First public version of SDK