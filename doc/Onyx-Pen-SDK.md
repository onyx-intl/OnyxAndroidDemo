# Introduction

Pen SDK for devices with stylus.To simplify the interface of scribble, we introduce TouchHelper class in `onyxsdk-pen`.

Compared to Scribble SDK, Pen SDK is a lighter weight library.

# Including in your project

## 1.Dependencies

Add the following dependencies to your app's `build.gradle` file

```gradle
    compile('com.onyx.android.sdk:onyxsdk-pen:1.4.10.1')
```
## 2.Init TouchHelper

```java
        TouchHelper.create(view, callback)
				   .setStrokeWidth(3.0f)
                   .setLimitRect(limit, exclude)
                   .openRawDrawing();
```
> view is that you want to scribe.
> callback is `RawInputCallback` that you can receive data being scribbled.
> limit is a rect specify the region you want to scribble on the view.
> exclude is a list of Rect to be excluded from the view.

## 3.Control pen
After `TouchHelper().openRawDrawing()`, you can call `touchHelper.setRawDrawingEnabled(true)` to start scribbling, ` touchHelper.setRawDrawingEnabled(false);` to pause.

You can call `touchHelper.setRawDrawingRenderEnabled(false)` to disable render during scribble.

You can call ` touchHelper.setStrokeStyle();` to set stroke style.

In order to fully stop TouchHelper, you need call ` touchHelper.closeRawDrawing()`.

## 4.Receive touch point data

- pen : `onBeginRawDrawing()` -> `onRawDrawingTouchPointMoveReceived()` -> `onRawDrawingTouchPointListReceived()` -> `onEndRawDrawing()`
- erase :  `onBeginRawErasing()` -> `onRawErasingTouchPointMoveReceived()` -> `onRawErasingTouchPointListReceived()` -> `onEndRawErasing()`

```java
        new RawInputCallback() {

        @Override
        public void onBeginRawDrawing(boolean b, TouchPoint touchPoint) {
          // begin of stylus data
        }

        @Override
        public void onEndRawDrawing(boolean b, TouchPoint touchPoint) {
         // end of stylus data
        }

        @Override
        public void onRawDrawingTouchPointMoveReceived(TouchPoint touchPoint) {
           // stylus data during stylus moving
        }

        @Override
        public void onRawDrawingTouchPointListReceived(TouchPointList touchPointList) {
        	// cumulation of stylus data of stylus moving, you will receive it before onEndRawDrawing
        }

        @Override
        public void onBeginRawErasing(boolean b, TouchPoint touchPoint) {
       		// same as RawData, but triggered by stylus eraser button
        }

        @Override
        public void onEndRawErasing(boolean b, TouchPoint touchPoint) {
            // same as RawData, but triggered by stylus eraser button
        }

        @Override
        public void onRawErasingTouchPointMoveReceived(TouchPoint touchPoint) {
  			// same as RawData, but triggered by stylus eraser button
        }

        @Override
        public void onRawErasingTouchPointListReceived(TouchPointList touchPointList) {
 			 // same as RawData, but triggered by stylus eraser button
        }
    };
```
## 5.Demo

Reference to the [ScribbleTouchHelperDemoActivity](../app/src/main/java/com/android/onyx/demo/scribble/ScribbleTouchHelperDemoActivity.java)

## 6.TouchHelper's API

 - `create(View view, RawInputCallback callback)` view, you want to scrrible; callback, you can receive data being scribbled.
 - `setLimitRect(Rect limit, List<Rect> exclude)` limit, a rect specify the region you want to scribble on the view. exclude, a list of Rect to be excluded from the view.
 - `setStrokeWidth(float var1)`set the width for stroking.
 - `setRawDrawingEnabled(boolean enable)` Set true, you enter scribble mode, and the screen will not refresh.
 - `setRawDrawingRenderEnabled(boolean enable)` set false, disable render during scribble.
 - `openRawDrawing()` Turn on scribble and initialize resources.
 - `closeRawDrawing()` Turn off scribble and release resources. Unlock the screen, screen can refresh..
 - `setStrokeStyle(int style)` Support style: `TouchHelper.STROKE_STYLE_FOUNTAIN`, `TouchHelper.STROKE_STYLE_PENCIL` 

# BrushRender
`BrushRender` is use to scribble pressure, for more detail to see [ScribbleTouchHelperDemoActivity](../app/src/main/java/com/android/onyx/demo/scribble/ScribbleTouchHelperDemoActivity.java) 
```
    /**
     * 
     * @param canvas
     * @param paint
     * @param points
     * @param strokeWidth
     * @param maxTouchPressure
     *        invoke EpdController.getMaxTouchPressure() to get pressure parameter
     */
    public static void drawStroke(final Canvas canvas,final Paint paint,final List<TouchPoint> points,final float strokeWidth,final float maxTouchPressure)
```

# Change logs

## 1.0.6.5

 Add `BrushRender` API

## 1.0.2

Fix issue of stroke style

## 1.0.1

`TouchHelper` add `setStrokeStyle(int style)`.

## 1.0.0
 
Simplify the Scribble SDK, init pen sdk.