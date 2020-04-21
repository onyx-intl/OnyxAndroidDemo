# Overview
Sample project of SDKs from Onyx-Intl, including [onyxsdk-base](doc/Onyx-Base-SDK.md), [onyxsdk-scribble](doc/Onyx-Scribble-SDK.md), [onyxsdk-pen](doc/Onyx-Pen-SDK.md)

To use the SDK, please add the following statement to your build.gradle:
```gradle
    compile ('com.onyx.android.sdk:onyxsdk-base:1.4.3.7')
    compile('com.onyx.android.sdk:onyxsdk-pen:1.0.8')
    compile('com.onyx.android.sdk:onyxsdk-data:1.1.0')
    compile('com.onyx.android.sdk:onyxsdk-notedata:1.0.5')
```

    
For onyxsdk-scribble SDK, dbflow library is inside the jitpack, so you have to add the following statement to your project build.gradle:
```gradle
    maven { url "https://jitpack.io" }
```

# Demo
The project contains following examples that you should take care of:

## 1.Screen
* [EpdDemoActivity](app/src/main/java/com/onyx/android/demo/EpdDemoActivity.java): basic demo of [EPD Screen Update](doc/EPD-Screen-Update.md)  with [EpdController](doc/EpdController.md)
* [FrontLightDemoActivity](app/src/main/java/com/onyx/android/demo/FrontLightDemoActivity.java): demo of [FrontLightController](doc/FrontLightController.md). If device support frontLight, you can switch the screen brightness.
* [FullScreenDemoActivity](app/src/main/java/com/onyx/android/demo/FullScreenDemoActivity.java): example of full screen switch. If you want to switch full screen , please call the api ` DeviceUtils.setFullScreenOnResume(this, fullscreen);`.That  supports all devices.

## 2.SDCard
* [EnvironmentDemoActivity](app/src/main/java/com/onyx/android/demo/EnvironmentDemoActivity.java): shows how to use [DeviceEnvironment](doc/DeviceEnvironment.md) to access removeable sdcard. You can call `DeviceEnvironment.getRemovableSDCardDirectory().getAbsolutePath();`

## 3.Scribble
Following demos are example of [onyxsdk-pen](doc/Onyx-Pen-SDK.md).

We use TouchHepler api to draw

* [ScribbleTouchHelperDemoActivity](app/src/main/java/com/onyx/android/demo/scribble/ScribbleTouchHelperDemoActivity.java) is an example of TouchHepler.

We have no restrictions on the view,  you can set anything view.For example, SurfaceView , webview.
We will return relative coordinates, According to the view you set.

* [ScribbleSurfaceViewDemoActivity](app/src/main/java/com/onyx/android/demo/scribble/ScribbleTouchHelperDemoActivity.java): example of SurfaceView

* [ScribbleWebViewDemoActivity](app/src/main/java/com/onyx/android/demo/scribble/ScribbleWebViewDemoActivity.java): example of Webview

If the device supports touch, you scribble with your fingers.
* [ScribbleTouchScreenDemoActivity](app/src/main/java/com/onyx/android/demo/scribble/ScribbleTouchScreenDemoActivity.java): example of [Scribble API](doc/Scribble-API.md) from [EpdController](doc/EpdController.md) for IMX6 devices

We alse support move eraser, multiple view scribble and save scribble points

* [ScribbleMoveEraserDemoActivity](app/src/main/java/com/onyx/android/demo/scribble/ScribbleMoveEraserDemoActivity.java)
: example of move eraser

* [ScribbleMultipleScribbleViewActivity](app/src/main/java/com/onyx/android/demo/scribble/ScribbleMultipleScribbleViewActivity.java)
: example of multiple view scribble

* [ScribbleSavePointsDemoActivity](app/src/main/java/com/onyx/android/demo/scribble/ScribbleSavePointsDemoActivity.java)
: example of save scribble points

* [ScribbleHWRActivity](app/src/main/java/com/onyx/android/demo/scribble/ScribbleHWRActivity.java)
: example of recognition scribble points

## 4.Screensaver

* [ScreensaverActivity](app/src/main/java/com/onyx/android/demo/ScreensaverActivity.java)
: example of setting screensaver

## 5.Open Setting

* [OpenSettingActivity](app/src/main/java/com/onyx/android/demo/OpenSettingActivity.java)
: example of open setting

## 6.Other API
* **DictionaryUtils** to query word in dictionary, for more details to see [DictionaryActivity](./app/src/main/java/com/onyx/android/demo/DictionaryActivity.java)

## 7.App Open Guide

* [AppOpenGuide](doc/AppOpenGuide.md)
: open app through adb command

## Welcome PR

> If you can improve the unit test for this project would be great.

- Comments as much as possible.
- Commit message format follow: [AngularJS's commit message convention](https://github.com/angular/angular.js/blob/master/CONTRIBUTING.md#-git-commit-guidelines) .
- The change of each commit as small as possible.
