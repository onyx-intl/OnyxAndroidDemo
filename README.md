# Overview
Sample project of SDKs from Onyx-Intl, including [onyxsdk-base](doc/Onyx-Base-SDK.md), [onyxsdk-scribble](doc/Onyx-Scribble-SDK.md), [onyxsdk-pen](doc/Onyx-Pen-SDK.md)

To use the SDK, please include the following dependencies in your build.gradle:
```gradle
    implementation('com.onyx.android.sdk:onyxsdk-device:1.1.11')
    implementation('com.onyx.android.sdk:onyxsdk-pen:1.2.1')
```

    
The `onyxsdk-scribble` SDK has a dependency on the `dbflow` library which is hosted in the jitpack repository. This can be added in your project's build.gradle with the following entry:
```gradle
    maven { url "https://jitpack.io" }
    
    maven { url "http://repo.boox.com/repository/maven-public/" }
    
```

# Demo
This repository contains the following examples:

## 1. Screen
* [EPDDemoActivity](app/src/main/java/com/onyx/android/demo/EpdDemoActivity.java): basic demo of [refreshing the EPD Screen](doc/EPD-Screen-Update.md) with the [EpdController](doc/EpdController.md)
* [FrontLightDemoActivity](app/src/main/java/com/onyx/android/demo/FrontLightDemoActivity.java): demo of [FrontLightController](doc/FrontLightController.md). If a device includes a frontLight, the screen brightness can be adjusted.
* [FullScreenDemoActivity](app/src/main/java/com/onyx/android/demo/FullScreenDemoActivity.java): demontrates full screen switching. The `DeviceUtils.setFullScreenOnResume(this, fullscreen)` API call is supported on all devices.

## 2. Storage (SD Card)
* [EnvironmentDemoActivity](app/src/main/java/com/onyx/android/demo/EnvironmentDemoActivity.java): shows how to use [DeviceEnvironment](doc/DeviceEnvironment.md) to access internal storage or the removable SD card

## 3. Inking
The following examples demonstrate the use of the [onyxsdk-pen](doc/Onyx-Pen-SDK.md) APIs.

Any type of view can be a drawing target, e.g. a SurfaceView or a WebView. Relative coordinates appropriate to the the view being used are provided.

* [ScribbleTouchHelperDemoActivity](app/src/main/java/com/onyx/android/demo/scribble/ScribbleTouchHelperDemoActivity.java) is an example demonstrating the use of TouchHelper, a simplified drawing API, with a SurfaceView. On touch-enabled devices, fingers can be used for drawing.

* [ScribbleWebViewDemoActivity](app/src/main/java/com/onyx/android/demo/scribble/ScribbleWebViewDemoActivity.java): shows how a WebView can be used to load a Web page and then apply ink to it

The SDK also provides additional functionality such as a move eraser, inking on multiple views, and saving ink strokes.

* [ScribbleMoveEraserDemoActivity](app/src/main/java/com/onyx/android/demo/scribble/ScribbleMoveEraserDemoActivity.java): using the move eraser

* [ScribbleMultipleScribbleViewActivity](app/src/main/java/com/onyx/android/demo/scribble/ScribbleMultipleScribbleViewActivity.java): inking with multiple views


## 4. Screensaver

* [ScreensaverActivity](app/src/main/java/com/onyx/android/demo/ScreensaverActivity.java): setting a screensaver

## 5. Settings

* [OpenSettingActivity](app/src/main/java/com/onyx/android/demo/OpenSettingActivity.java): opening a settings page

## 6. Misc API
* **DictionaryUtil** can be used to query the dictionary. For details see [DictionaryActivity](./app/src/main/java/com/onyx/android/demo/DictionaryActivity.java)

## 7. App Open Guide

* [AppOpenGuide](doc/AppOpenGuide.md): opening an app using the Android Debug Bridge (adb)

## PRs Welcome

All contributions towards documentation, adding/improving unit tests, and examples are appreciated.

- Please comment as much as possible.
- Commit message format should follow [AngularJS's commit message convention.](https://github.com/angular/angular.js/blob/master/CONTRIBUTING.md#-git-commit-guidelines).
- Please keep changes for each commit as small as possible.
