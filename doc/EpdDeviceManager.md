Wrapper of [EpdController](./EpdController.md) to provide uniform and simple [EPD Screen Update](./EPD-Screen-Update.md) interface for both RK3026 and IMX6 devices

You can set partial and full screen update intervals by calling:

```
EpdDeviceManager.setGcInterval(THE_INTERVAL_YOU_WANT_TO_SET);
```

Then update screen with:

```
EpdDeviceManager.applyWithGCInterval(textView, isTextPage);
```

EpdDeviceManager will take care of device type and choose to use normal/regal partial screen update automatically.

You can also force full screen update by calling:

```
EpdDeviceManager.applyGCUpdate(textView);
```


Enter/leave fast update mode:

`EpdDeviceManager.enterAnimationUpdate(true);`

`EpdDeviceManager.exitAnimationUpdate(true);`

You can see sample code in [EpdDemoActivity](../app/src/main/java/com/onyx/android/demo/EpdDemoActivity.java)