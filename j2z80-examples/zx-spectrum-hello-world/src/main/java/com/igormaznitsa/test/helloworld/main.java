package com.igormaznitsa.test.helloworld;

import static com.igormaznitsa.test.helloworld.ZXScreen.COLOR_BLACK;
import static com.igormaznitsa.test.helloworld.ZXScreen.COLOR_BLUE;
import static com.igormaznitsa.test.helloworld.ZXScreen.COLOR_GREEN;
import static com.igormaznitsa.test.helloworld.ZXScreen.COLOR_WHITE;
import static com.igormaznitsa.test.helloworld.ZXScreen.COLOR_YELLOW;
import static com.igormaznitsa.test.helloworld.ZXScreen.clearServiceScreen;
import static com.igormaznitsa.test.helloworld.ZXScreen.clearWholeScreen;
import static com.igormaznitsa.test.helloworld.ZXScreen.setBorderColor;
import static com.igormaznitsa.test.helloworld.ZXScreen.setTextColor;

public class main {
  public static final void mainz() {
    setTextColor(COLOR_YELLOW, COLOR_BLUE);
    clearWholeScreen();
    System.out.println("Hello world 2026!");
    setTextColor(COLOR_BLACK, COLOR_WHITE);
    clearServiceScreen();
    System.err.println("Tervitused Eestist!");
    setBorderColor(COLOR_GREEN);
  }
}
