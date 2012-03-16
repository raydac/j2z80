package com.igormaznitsa.test.helloworld;

public class ZXScreen {
    public static final int COLOR_BLACK = 0;
    public static final int COLOR_BLUE = 1;
    public static final int COLOR_RED = 2;
    public static final int COLOR_VIOLET = 3;
    public static final int COLOR_GREEN = 4;
    public static final int COLOR_LIGHTBLUE = 5;
    public static final int COLOR_YELLOW = 6;
    public static final int COLOR_WHITE = 7;
    
    public static native void setBorderColor(int color);
    public static native void setTextColor(int inkColor, int paperColor);
    public static native void clearWholeScreen();
    public static native void clearServiceScreen();
}
