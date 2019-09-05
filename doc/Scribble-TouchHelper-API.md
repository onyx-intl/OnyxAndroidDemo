# Including in your project
To simplify the interface of scribble, we introduce TouchHelper class in onyxsdk-scribble 1.0.8.

You can init it using the code below:
```
touchHelper.setup(view)
           .setStrokeWidth(3.0f)
           .setUseRawInput(true)
           .setLimitRect(limit, exclude)
           .openRawDrawing();
```
limit is a rect specify the region you want to scribble on the view, exclude is a list of Rect to be excluded from the view.

After `TouchHelper().openRawDrawing()`, you can call `touchHelper.setRawDrawingEnabled(true)` to start scribbling, ` touchHelper.setRawDrawingEnabled(false);` to pause.

In order to fully stop TouchHelper, you need call ` touchHelper.closeRawDrawing()`.

After TouchHelper is correctly setup, you can scribble on screen with stylus, but you may also want to receive data being scribbled, we pass back scribble touch point data with EventBus, below are the events being sent from TouchHelper:
```
@Subscribe
public void onErasingTouchEvent(ErasingTouchEvent e) {
// ignore
}

@Subscribe
public void onDrawingTouchEvent(DrawingTouchEvent e) {
// ignore
}

@Subscribe
public void onBeginRawDataEvent(BeginRawDataEvent e) {
// begin of stylus data
}

@Subscribe
public void onEndRawDataEvent(EndRawDataEvent e) {
// end of stylus data
}

@Subscribe
public void onRawTouchPointMoveReceivedEvent(RawTouchPointMoveReceivedEvent e) {
// stylus data during stylus moving
}

@Subscribe
public void onRawTouchPointListReceivedEvent(RawTouchPointListReceivedEvent e) {
// cumulation of stylus data of stylus moving, you will receive it before onEndRawDataEvent
}

@Subscribe
public void onRawErasingStartEvent(BeginRawErasingEvent e) {
// same as RawData, but triggered by stylus eraser button
}

@Subscribe
public void onRawErasingFinishEvent(RawErasePointListReceivedEvent e) {
// same as RawData, but triggered by stylus eraser button
}

@Subscribe
public void onRawErasePointMoveReceivedEvent(RawErasePointMoveReceivedEvent e) {
// same as RawData, but triggered by stylus eraser button
}

@Subscribe
public void onRawErasePointListReceivedEvent(RawErasePointListReceivedEvent e) {
// same as RawData, but triggered by stylus eraser button
}
```
You can see sample code in [ScribbleTouchHelperDemoActivity](../app/src/main/java/com/onyx/android/demo/scribble/ScribbleTouchHelperDemoActivity.java)

# API

 - `setup(View view)` view, you want to scrrible 
 - `setLimitRect(Rect limit)`limit, a rect specify the region you want to scribble on the view.
 - `setLimitRect(Rect limit, List<Rect> exclude)` limit, a rect specify the region you want to scribble on the view. exclude, a list of Rect to be excluded from the view.
 - `setStrokeWidth(float var1)`set the width for stroking.
 - `setUseRawInput(boolean var1)`true, you can get touch point data
 - `setRawDrawingEnabled(boolean enable)`: Set true, you enter scribble mode, and the screen will not refresh.
 - `openRawDrawing()` Turn on scribble and initialize resources.
 - `closeRawDrawing()` Turn off scribble and release resources. Unlock the screen, screen can refresh..