package com.igormaznitsa.test.helloworld;

import static com.igormaznitsa.test.helloworld.ZXScreen.*;

public class main {
    public static final void mainz(){
        setTextColor(COLOR_YELLOW, COLOR_BLUE);
        clearWholeScreen();
        System.out.println("Hello world 2026!");
        setTextColor(COLOR_BLACK, COLOR_WHITE);
        clearServiceScreen();
        System.err.println("Tervitused Eestist!");
        setBorderColor(COLOR_GREEN);
    }
}
