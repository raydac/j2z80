package com.igormaznitsa.test.helloworld;

import static com.igormaznitsa.test.helloworld.ZXScreen.*;

public class main {
    public static final void mainz(){
        setTextColor(COLOR_RED, COLOR_WHITE);
        clearWholeScreen();
        System.out.println("Hello world!");
        setTextColor(COLOR_YELLOW, COLOR_BLUE);
        clearServiceScreen();
        System.err.println("Written in Java!!!");
        setBorderColor(COLOR_RED);
    }
}
