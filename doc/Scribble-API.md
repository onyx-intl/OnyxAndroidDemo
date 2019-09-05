# init scribble 

Before using scribble feature, developer needs to call 

```
Device.currentDevice().enterScribbleMode(view);
```
to request framework to change to scribble mode. When scribble is finished, developer could call

```
Device.currentDevice().leaveScribbleMode(view);
```

Note: During scribble mode, all other screen update request will be ignored.


# moveTo and lineTo

When using scribble, Onyx Android SDK provides two functions to draw line on screen

```
Device.currentDevice().moveTo(x, y, lineWidth);
Device.currentDevice().lineTo(x, y, UpdateMode.DU);
```

The x and y can be in view coordinates. For M96, developer could use following code

```
 Device.currentDevice().moveTo(view, x, y, lineWidth);
```

# stroke color 

you can change stroke color by 

```
 // so far, only black and white are supported due to eink display limit.
 Device.currentDevice().setStrokeColor(0xff000000);  // black
 Device.currentDevice().setStrokeColor(0xffffffff);  // white

```

# stroke style
You can change the stroke style by 

```
 // only pen style and brush style supported.
 Device.currentDevice().setStrokeStyle(0);  // pen style, the line width will not be changed
 Device.currentDevice().setStrokeStyle(1);  // brush style, line width will be changed when pressure or speed changed.

```

# Painter style

You can change the painter style by 

```

Device.currentDevice().setPainterStyle(true,   // antiAlias or not
 Paint.Style.FILL_AND_STROKE,         // stroke style
 Paint.Join.ROUND,                    // join style
 Paint.Cap.ROUND);                    // cap style

```

smooth scribble
=============================

Developer may need to call enterScribbleMode and leaveScribbleMode() during scribbling. 

```
Device.currentDevice().enterScribbleMode(view);
Device.currentDevice().leaveScribbleMode(view);

```
```

public class PaintView extends View {

    float [] mapPoint(float x, float y) {
        int viewLocation[] = {0, 0};
        getLocationOnScreen(viewLocation);
        float screenPoints[] = {viewLocation[0] + x, viewLocation[1] + y};
        float dst[] = {0, 0};
        matrix.mapPoints(dst, screenPoints);
        return dst;
    }
}


public class ScribbleActivity extends Activity {

    public boolean onTouchEvent(ScribbleActivity activity, MotionEvent e) {
        // ignore multi touch
        if (e.getPointerCount() > 1) {
            return false;
        }

        final float baseWidth = 5;
        paintView.init(paintView.getWidth(), paintView.getHeight());

        switch (e.getAction() & MotionEvent.ACTION_MASK) {
            case (MotionEvent.ACTION_DOWN):
                float dst[] = paintView.mapPoint(e.getX(), e.getY());
                Device.currentDevice().startStroke(baseWidth, dst[0], dst[1], e.getPressure(), e.getSize(), e.getEventTime());
                return true;
            case (MotionEvent.ACTION_CANCEL):
            case (MotionEvent.ACTION_OUTSIDE):
                break;
            case MotionEvent.ACTION_UP:
                dst = paintView.mapPoint(e.getX(), e.getY());
                Device.currentDevice().finishStroke(baseWidth, dst[0], dst[1], e.getPressure(), e.getSize(), e.getEventTime());
                return true;
            case MotionEvent.ACTION_MOVE:
                int n = e.getHistorySize();
                for (int i = 0; i < n; i++) {
                    dst = paintView.mapPoint(e.getHistoricalX(i), e.getHistoricalY(i));
                    Device.currentDevice().addStrokePoint(baseWidth,  dst[0], dst[1], e.getPressure(), e.getSize(), e.getEventTime());
                }
                dst = paintView.mapPoint(e.getX(), e.getY());
                Device.currentDevice().addStrokePoint(baseWidth, dst[0], dst[1], e.getPressure(), e.getSize(), e.getEventTime());
                return true;
            default:
                break;
        }
        return true;
    }

```

# scribble line width

```
public static float startStroke(float baseWidth, float x, float y, float pressure, float size, float time);
public static float addStrokePoint(float baseWidth, float x, float y, float pressure, float size, float time);
public static float finishStroke(float baseWidth, float x, float y, float pressure, float size, float time) ;

Above functions return line width currently calculated. Application may need to change the line width to proper value and save into persistent storage.

```


# handle eraser and quick start

```
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
                // eraser has been pressed
                return false;
            case KeyEvent.KEYCODE_BUTTON_START:
                // the quick start button has been pressed.
                return false;
            default:
                return super.onKeyDown(activity,keyCode,event);
        }
    }
```
