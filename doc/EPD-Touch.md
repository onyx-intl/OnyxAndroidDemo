**This API add on onyxsdk-base 1.4.3 to use it ,upgrade it**

[EpdController](./EpdController.md)  provides API to enable and disable touch region on screen.

 
***Get touch function state***

```
    /**
     *
     * @param context
     * @return boolean
     *         if true touch is disable, otherwise touch is enable
     */
    boolean isCTPDisableRegion(Context context)
```

***Set the region you want to disable touch***

there have two method to set regions, we suggested use first method:
```
    /**
     *
     * @param context
     * @param disableRegions 
     *        the regions that you want to disable touch
     */
    void setAppCTPDisableRegion(Context context, Rect[] disableRegions);
```

or use this

```
    /**
     * 
     * @param context
     * @param disableRegionArray 
     *        a array of rect which four point order by left, top, right, bottom 
     */
    void setAppCTPDisableRegion(Context context, int[] disableRegionArray)
```



***Set the region you want to enable touch***
to exclude some region enable touch use this:
```
    /**
     * 
     * @param context
     * @param disableRegions
              the regions that you want to disable touch
     * @param excludeRegions
     *        the regions that your want to exclude from disabled touch area
     */
    void setAppCTPDisableRegion(Context context, Rect[] disableRegions, Rect[] excludeRegions)`
```



***Reset the touch***
Reset touch function to default, default status is the screen can be touch.
```
    /**
     * 
     * @param context
     */
    void appResetCTPDisableRegion(Context context)
```
