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
    boolean isTouchAreaIgnoreRegionDetect(Context context)
```

***Set the region you want to disable touch***

there have two method to set regions, we suggested use first method:
```
    /**
     *
     * @param context
     * @param regions 
     *        the regions that you want to disable touch
     */
    void setTouchAreaIgnoreRegion(Context context, Rect[] regions);
```

or use this

```
    /**
     * 
     * @param context
     * @param array 
     *        a array of rect which four point order by left, top, right, bottom 
     */
    void setTouchAreaIgnoreRegion(Context context, int[] array)
```



***Set the region you want to enable touch***
to exclude some region enable touch use this:
```
    /**
     * 
     * @param context
     * @param regions
     *        the regions that your want to exclude from disabled touch area
     */
    void setTouchAreaIgnoreExcludeRegion(Context context, Rect[] regions)`
```

***Change the state***
 if you set region to disable touch, you should change status
```
    /**
     * 
     * @param context
     * @param enable
     *        the state of touch, true if touch is disable, otherwise false
     */
    void setTouchAreaIgnoreRegionDetectStatus(Context context, boolean enable)
```


***Reset the touch***
Reset touch function to default, default status is the screen can be touch.
```
    /**
     * 
     * @param context
     */
    void resetTouchAreaIgnoreRegion(Context context)
```
