#### Open with ADB command ```adb shell am start -n <component>```, here list the component of some apps
|App|component|
|---|---|
|Dictionary|com.onyx.dict/.activity.DictMainActivity|
|Email|com.android.email/.activity.Welcome|
|Calculator|com.onyx.calculator/.Calculator|
|Browser|com.android.browser/.BrowserActivity|
|Recorder|com.android.soundrecorder/.SoundRecorder|
|Calender|com.android.calendar/.AllInOneActivity|
|Clock|com.onyx.deskclock/.deskclock.DeskClock|
|Music|com.onyx.music/.MusicBrowserActivity|
|App Market|com.onyx.appmarket/.activity.AppMarketActivity|
|Floating Button|com.onyx.floatingbutton/.FloatButtonSettingActivity|
#### Exceptions
* Book Shop: `am start -n com.onyx/.main.ui.MainActivity --es "json" "{'action':'OPEN_SHOP'}"` , Intent needs to carry a String whose key is `json` with content `{'action':'OPEN_SHOP'}`
* Note: `am start -n com.onyx/.main.ui.MainActivity --es "json" "{'action':'OPEN_NOTE'}"` , Intent needs to carry a String whose key is `json` with content `{'action':'OPEN_NOTE'}`
    > Call this command after entering adb shell mode
