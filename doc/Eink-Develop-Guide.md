# UI/UX

## How to develop Apps for BOOX eink device

Because of the eink feture, the refresh rate in eink device is much lower than normal LCD tablet,
to ensure App runs smoothly and give users good using experience, we would like to provide following suggestion when develop Apps

1. Pls use black and white (16 level gray scale) as the main color of pages;
If need to change the color for state switching, pls try to ensure that the color is in the 256 level gray scale.
2. Pls don’t add transparent layers on images or texts;
3. Pls use the page based way to load more contents;
4. Pls try to avoid animation, such as scrolling/dragging etc.
5. The font size should not be smaller than 14sp;
If embed fonts are needed, pls use bold or bold as much as possible
6. Normally, the button icon in the central area should not be smaller than 36dp x 36dp. The button icon in the edge area should not be smaller than 48dp x 48dp.