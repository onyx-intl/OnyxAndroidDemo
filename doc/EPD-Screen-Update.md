# UpdateOption:

* NORMAL

  Good display effect, suitable for general text reading.

* FAST_QUALITY

  Slight ghosting, suitable for quickly skimming through images and text.

* REGAL

  Minimal ghosting, slightly flickering on the dark backgrounds, suitable for light-colored backgrounds.

* FAST

  Slightly heavier ghosting, suitable for scrolling images and text.

* FAST_X

  Heavier loss of details, suitable for viewing webpage and playing videos.

[EpdController](./EpdController.md) provides API to update screen with different [Update Mode](./EPD-Update-Mode.md):
* Partial

    Default update mode

    `EpdController.setViewDefaultUpdateMode(view, UpdateMode.GU);`

* Regal Partial

    Optimized partial update mode for text pages

    `EpdController.setViewDefaultUpdateMode(view, UpdateMode.REGAL);`

* Full screen

    Full screen update

    `EpdController.invalidate(view, UpdateMode.GC);`

* Fast (Animation) mode

    Black/white mode for fast screen update, such as zooming/scrolling/dragging

    Enter fast mode:

    `EpdController.applyApplicationFastMode(APP, true, clear);`

    Leave fast mdoe:

    `EpdController.applyApplicationFastMode(APP, false, clear);`
