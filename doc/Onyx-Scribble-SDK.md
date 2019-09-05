Scribble SDK for devices with stylus.

Latest version is 1.0.8, can be referenced with following gradle statement:

    compile 'com.onyx.android.sdk:onyxsdk-scribble:1.0.8'

For onyxsdk-scribble SDK, dbflow library is inside the jitpack, so you have to add the following statement to your project build.gradle:
```gradle
   maven { url "https://jitpack.io" }
```

`TouchHelper` is the latest api that you can scribble with stylus. You should call it.For more detailed usage, check out it out [here](Scribble-TouchHelper-API.md)

# Change logs

## 1.0.8
1. Remove `initRawDrawing()` (use `openRawDrawing()` instead): Turn on scribble and initialize resources.
2. Add `closeRawDrawing()`: Turn off scribble and release resources. Unlock the screen, screen can refresh.
3. Add `setRawDrawingEnabled(boolean enable)`: Set true, you enter scribble mode, and the screen will not refresh.
