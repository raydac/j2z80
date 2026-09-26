package com.igormaznitsa.test.helloworld;

import j2z80.spectrum.Screen;

public class Portrait {

  // [n, x0, y0, ...] from iam_line.svg; n=0 point, n=1 line, n>1 polyline
  public static final byte[] DATA = {
      (byte) 116, (byte) 131, (byte) 103, (byte) 130, (byte) 104, (byte) 132, (byte) 111,
      (byte) 131, (byte) 113, (byte) 127, (byte) 105, (byte) 124, (byte) 103, (byte) 122,
      (byte) 103, (byte) 124, (byte) 104, (byte) 123, (byte) 105, (byte) 119,
      (byte) 104, (byte) 120, (byte) 106, (byte) 112, (byte) 106, (byte) 110, (byte) 104,
      (byte) 114, (byte) 100, (byte) 125, (byte) 101, (byte) 129, (byte) 100, (byte) 124, (byte) 98,
      (byte) 113, (byte) 98, (byte) 109, (byte) 101, (byte) 108,
      (byte) 105, (byte) 104, (byte) 106, (byte) 106, (byte) 107, (byte) 104, (byte) 110, (byte) 97,
      (byte) 110, (byte) 81, (byte) 106, (byte) 84, (byte) 104, (byte) 85, (byte) 99, (byte) 88,
      (byte) 97, (byte) 88, (byte) 95, (byte) 85,
      (byte) 95, (byte) 83, (byte) 102, (byte) 78, (byte) 107, (byte) 69, (byte) 109, (byte) 68,
      (byte) 111, (byte) 69, (byte) 127, (byte) 72, (byte) 136, (byte) 75, (byte) 139, (byte) 79,
      (byte) 138, (byte) 75, (byte) 137, (byte) 76,
      (byte) 132, (byte) 73, (byte) 132, (byte) 72, (byte) 130, (byte) 74, (byte) 127, (byte) 71,
      (byte) 127, (byte) 72, (byte) 124, (byte) 74, (byte) 124, (byte) 71, (byte) 122, (byte) 73,
      (byte) 120, (byte) 74, (byte) 121, (byte) 70,
      (byte) 118, (byte) 75, (byte) 117, (byte) 71, (byte) 114, (byte) 71, (byte) 112, (byte) 74,
      (byte) 112, (byte) 75, (byte) 110, (byte) 103, (byte) 114, (byte) 113, (byte) 118, (byte) 125,
      (byte) 118, (byte) 135, (byte) 115, (byte) 139,
      (byte) 115, (byte) 144, (byte) 118, (byte) 146, (byte) 117, (byte) 149, (byte) 118,
      (byte) 151, (byte) 120, (byte) 150, (byte) 126, (byte) 142, (byte) 143, (byte) 135,
      (byte) 148, (byte) 121, (byte) 155, (byte) 105, (byte) 156, (byte) 91,
      (byte) 151, (byte) 90, (byte) 150, (byte) 91, (byte) 149, (byte) 87, (byte) 148, (byte) 87,
      (byte) 146, (byte) 89, (byte) 147, (byte) 85, (byte) 144, (byte) 86, (byte) 142, (byte) 82,
      (byte) 144, (byte) 80, (byte) 141, (byte) 76,
      (byte) 141, (byte) 87, (byte) 150, (byte) 94, (byte) 154, (byte) 101, (byte) 156, (byte) 117,
      (byte) 157, (byte) 128, (byte) 154, (byte) 143, (byte) 144, (byte) 149, (byte) 134,
      (byte) 150, (byte) 126, (byte) 153, (byte) 119, (byte) 162,
      (byte) 114, (byte) 163, (byte) 112, (byte) 159, (byte) 99, (byte) 157, (byte) 98, (byte) 150,
      (byte) 99, (byte) 147, (byte) 97, (byte) 145, (byte) 88, (byte) 148, (byte) 80, (byte) 143,
      (byte) 83, (byte) 139, (byte) 83, (byte) 137,
      (byte) 85, (byte) 138, (byte) 86, (byte) 130, (byte) 88, (byte) 128, (byte) 90, (byte) 128,
      (byte) 92, (byte) 129, (byte) 90, (byte) 131, (byte) 90, (byte) 135, (byte) 93, (byte) 137,
      (byte) 93, (byte) 139, (byte) 92, (byte) 139,
      (byte) 89, (byte) 140, (byte) 89, (byte) 145, (byte) 93, (byte) 145, (byte) 97, (byte) 138,
      (byte) 109, (byte) 135, (byte) 111, (byte) 132, (byte) 103, (byte) 131, (byte) 103,
      (byte) 106, (byte) 114, (byte) 41, (byte) 117, (byte) 49,
      (byte) 114, (byte) 51, (byte) 112, (byte) 48, (byte) 111, (byte) 51, (byte) 109, (byte) 51,
      (byte) 105, (byte) 48, (byte) 104, (byte) 49, (byte) 105, (byte) 52, (byte) 103, (byte) 53,
      (byte) 104, (byte) 54, (byte) 109, (byte) 53,
      (byte) 109, (byte) 55, (byte) 108, (byte) 56, (byte) 105, (byte) 55, (byte) 104, (byte) 57,
      (byte) 100, (byte) 58, (byte) 98, (byte) 60, (byte) 103, (byte) 64, (byte) 104, (byte) 61,
      (byte) 102, (byte) 59, (byte) 106, (byte) 59,
      (byte) 106, (byte) 63, (byte) 107, (byte) 61, (byte) 109, (byte) 61, (byte) 110, (byte) 65,
      (byte) 113, (byte) 62, (byte) 115, (byte) 64, (byte) 116, (byte) 63, (byte) 120, (byte) 64,
      (byte) 120, (byte) 61, (byte) 122, (byte) 59,
      (byte) 125, (byte) 61, (byte) 129, (byte) 57, (byte) 133, (byte) 57, (byte) 131, (byte) 59,
      (byte) 132, (byte) 61, (byte) 136, (byte) 57, (byte) 141, (byte) 62, (byte) 138, (byte) 62,
      (byte) 139, (byte) 64, (byte) 147, (byte) 60,
      (byte) 150, (byte) 64, (byte) 151, (byte) 69, (byte) 148, (byte) 74, (byte) 144, (byte) 76,
      (byte) 133, (byte) 78, (byte) 123, (byte) 74, (byte) 121, (byte) 69, (byte) 119, (byte) 69,
      (byte) 119, (byte) 72, (byte) 121, (byte) 77,
      (byte) 137, (byte) 81, (byte) 143, (byte) 81, (byte) 149, (byte) 78, (byte) 152, (byte) 73,
      (byte) 154, (byte) 83, (byte) 154, (byte) 95, (byte) 156, (byte) 96, (byte) 154, (byte) 65,
      (byte) 152, (byte) 60, (byte) 145, (byte) 56,
      (byte) 142, (byte) 48, (byte) 146, (byte) 44, (byte) 158, (byte) 39, (byte) 162, (byte) 36,
      (byte) 168, (byte) 34, (byte) 168, (byte) 33, (byte) 165, (byte) 33, (byte) 154, (byte) 38,
      (byte) 150, (byte) 38, (byte) 152, (byte) 36,
      (byte) 152, (byte) 33, (byte) 150, (byte) 33, (byte) 146, (byte) 39, (byte) 145, (byte) 39,
      (byte) 147, (byte) 36, (byte) 144, (byte) 33, (byte) 133, (byte) 33, (byte) 140, (byte) 47,
      (byte) 140, (byte) 51, (byte) 134, (byte) 44,
      (byte) 131, (byte) 44, (byte) 132, (byte) 46, (byte) 129, (byte) 43, (byte) 128, (byte) 46,
      (byte) 127, (byte) 42, (byte) 130, (byte) 41, (byte) 127, (byte) 40, (byte) 122, (byte) 35,
      (byte) 119, (byte) 35, (byte) 125, (byte) 42,
      (byte) 125, (byte) 45, (byte) 123, (byte) 45, (byte) 122, (byte) 40, (byte) 120, (byte) 40,
      (byte) 121, (byte) 46, (byte) 125, (byte) 48, (byte) 127, (byte) 47, (byte) 126, (byte) 48,
      (byte) 127, (byte) 53, (byte) 117, (byte) 53,
      (byte) 118, (byte) 48, (byte) 121, (byte) 51, (byte) 122, (byte) 51, (byte) 122, (byte) 49,
      (byte) 114, (byte) 41, (byte) 21, (byte) 63, (byte) 47, (byte) 64, (byte) 48, (byte) 62,
      (byte) 49, (byte) 71, (byte) 53, (byte) 76,
      (byte) 61, (byte) 77, (byte) 66, (byte) 76, (byte) 82, (byte) 78, (byte) 80, (byte) 80,
      (byte) 80, (byte) 84, (byte) 83, (byte) 84, (byte) 80, (byte) 79, (byte) 76, (byte) 79,
      (byte) 68, (byte) 76, (byte) 54, (byte) 71,
      (byte) 40, (byte) 72, (byte) 36, (byte) 71, (byte) 34, (byte) 69, (byte) 36, (byte) 70,
      (byte) 45, (byte) 69, (byte) 50, (byte) 62, (byte) 35, (byte) 63, (byte) 47, (byte) 14,
      (byte) 109, (byte) 112, (byte) 110, (byte) 109,
      (byte) 114, (byte) 109, (byte) 118, (byte) 111, (byte) 123, (byte) 111, (byte) 123,
      (byte) 112, (byte) 118, (byte) 112, (byte) 121, (byte) 114, (byte) 120, (byte) 115,
      (byte) 121, (byte) 114, (byte) 127, (byte) 115, (byte) 125, (byte) 116,
      (byte) 112, (byte) 116, (byte) 110, (byte) 115, (byte) 109, (byte) 112, (byte) 12, (byte) 142,
      (byte) 105, (byte) 147, (byte) 100, (byte) 155, (byte) 100, (byte) 159, (byte) 102,
      (byte) 160, (byte) 107, (byte) 157, (byte) 109, (byte) 154,
      (byte) 109, (byte) 153, (byte) 108, (byte) 154, (byte) 102, (byte) 149, (byte) 104,
      (byte) 149, (byte) 106, (byte) 150, (byte) 106, (byte) 142, (byte) 105, (byte) 13, (byte) 86,
      (byte) 74, (byte) 87, (byte) 76, (byte) 90, (byte) 75,
      (byte) 91, (byte) 73, (byte) 90, (byte) 71, (byte) 92, (byte) 67, (byte) 93, (byte) 67,
      (byte) 92, (byte) 70, (byte) 95, (byte) 71, (byte) 93, (byte) 65, (byte) 89, (byte) 67,
      (byte) 88, (byte) 69, (byte) 89, (byte) 74,
      (byte) 86, (byte) 74, (byte) 4, (byte) 54, (byte) 45, (byte) 53, (byte) 42, (byte) 36,
      (byte) 35, (byte) 40, (byte) 39, (byte) 54, (byte) 45, (byte) 10, (byte) 78, (byte) 100,
      (byte) 74, (byte) 101, (byte) 74, (byte) 104,
      (byte) 71, (byte) 104, (byte) 70, (byte) 103, (byte) 70, (byte) 96, (byte) 68, (byte) 98,
      (byte) 69, (byte) 106, (byte) 70, (byte) 107, (byte) 74, (byte) 106, (byte) 78, (byte) 100,
      (byte) 11, (byte) 95, (byte) 66, (byte) 98,
      (byte) 71, (byte) 100, (byte) 72, (byte) 100, (byte) 70, (byte) 97, (byte) 67, (byte) 98,
      (byte) 66, (byte) 102, (byte) 69, (byte) 102, (byte) 66, (byte) 100, (byte) 63, (byte) 95,
      (byte) 61, (byte) 97, (byte) 63, (byte) 95,
      (byte) 66, (byte) 8, (byte) 77, (byte) 100, (byte) 80, (byte) 96, (byte) 82, (byte) 88,
      (byte) 80, (byte) 88, (byte) 77, (byte) 93, (byte) 75, (byte) 91, (byte) 74, (byte) 92,
      (byte) 75, (byte) 98, (byte) 77, (byte) 100,
      (byte) 7, (byte) 69, (byte) 109, (byte) 67, (byte) 106, (byte) 67, (byte) 99, (byte) 68,
      (byte) 99, (byte) 68, (byte) 95, (byte) 67, (byte) 95, (byte) 66, (byte) 106, (byte) 69,
      (byte) 109, (byte) 4, (byte) 76, (byte) 82,
      (byte) 74, (byte) 82, (byte) 69, (byte) 88, (byte) 68, (byte) 94, (byte) 76, (byte) 82,
      (byte) 7, (byte) 90, (byte) 87, (byte) 87, (byte) 84, (byte) 89, (byte) 82, (byte) 86,
      (byte) 78, (byte) 86, (byte) 84, (byte) 89,
      (byte) 89, (byte) 91, (byte) 90, (byte) 90, (byte) 87, (byte) 7, (byte) 133, (byte) 71,
      (byte) 137, (byte) 75, (byte) 140, (byte) 76, (byte) 141, (byte) 74, (byte) 143, (byte) 74,
      (byte) 142, (byte) 72, (byte) 138, (byte) 70,
      (byte) 133, (byte) 71, (byte) 4, (byte) 55, (byte) 35, (byte) 59, (byte) 43, (byte) 62,
      (byte) 44, (byte) 59, (byte) 38, (byte) 55, (byte) 35, (byte) 4, (byte) 62, (byte) 49,
      (byte) 63, (byte) 48, (byte) 62, (byte) 46,
      (byte) 54, (byte) 44, (byte) 62, (byte) 49, (byte) 4, (byte) 123, (byte) 88, (byte) 126,
      (byte) 94, (byte) 128, (byte) 95, (byte) 125, (byte) 89, (byte) 123, (byte) 88, (byte) 6,
      (byte) 121, (byte) 61, (byte) 121, (byte) 63,
      (byte) 124, (byte) 66, (byte) 126, (byte) 63, (byte) 123, (byte) 64, (byte) 123, (byte) 62,
      (byte) 121, (byte) 61, (byte) 6, (byte) 92, (byte) 75, (byte) 93, (byte) 78, (byte) 94,
      (byte) 74, (byte) 92, (byte) 72, (byte) 92,
      (byte) 74, (byte) 93, (byte) 74, (byte) 92, (byte) 75, (byte) 4, (byte) 154, (byte) 113,
      (byte) 157, (byte) 112, (byte) 160, (byte) 114, (byte) 155, (byte) 115, (byte) 154,
      (byte) 113, (byte) 3, (byte) 141, (byte) 113, (byte) 147,
      (byte) 114, (byte) 143, (byte) 115, (byte) 141, (byte) 113, (byte) 5, (byte) 137, (byte) 59,
      (byte) 136, (byte) 58, (byte) 134, (byte) 59, (byte) 135, (byte) 63, (byte) 137, (byte) 62,
      (byte) 137, (byte) 59, (byte) 4, (byte) 87,
      (byte) 125, (byte) 91, (byte) 129, (byte) 89, (byte) 125, (byte) 88, (byte) 124, (byte) 87,
      (byte) 125, (byte) 4, (byte) 119, (byte) 64, (byte) 116, (byte) 64, (byte) 117, (byte) 68,
      (byte) 118, (byte) 68, (byte) 119, (byte) 64,
      (byte) 4, (byte) 86, (byte) 90, (byte) 87, (byte) 93, (byte) 89, (byte) 93, (byte) 88,
      (byte) 89, (byte) 86, (byte) 90, (byte) 4, (byte) 72, (byte) 91, (byte) 75, (byte) 89,
      (byte) 75, (byte) 87, (byte) 72, (byte) 89,
      (byte) 72, (byte) 91, (byte) 3, (byte) 85, (byte) 117, (byte) 87, (byte) 117, (byte) 84,
      (byte) 113, (byte) 85, (byte) 117, (byte) 3, (byte) 146, (byte) 112, (byte) 147, (byte) 111,
      (byte) 151, (byte) 112, (byte) 146, (byte) 112,
      (byte) 4, (byte) 115, (byte) 39, (byte) 116, (byte) 41, (byte) 119, (byte) 41, (byte) 119,
      (byte) 40, (byte) 115, (byte) 39, (byte) 4, (byte) 84, (byte) 139, (byte) 87, (byte) 139,
      (byte) 88, (byte) 137, (byte) 86, (byte) 137,
      (byte) 84, (byte) 139, (byte) 4, (byte) 129, (byte) 79, (byte) 128, (byte) 79, (byte) 129,
      (byte) 82, (byte) 131, (byte) 82, (byte) 129, (byte) 79, (byte) 3, (byte) 134, (byte) 113,
      (byte) 137, (byte) 112, (byte) 138, (byte) 114,
      (byte) 134, (byte) 113, (byte) 3, (byte) 146, (byte) 70, (byte) 148, (byte) 70, (byte) 149,
      (byte) 67, (byte) 146, (byte) 70, (byte) 3, (byte) 98, (byte) 51, (byte) 100, (byte) 51,
      (byte) 101, (byte) 48, (byte) 98, (byte) 51,
      (byte) 3, (byte) 85, (byte) 132, (byte) 85, (byte) 134, (byte) 88, (byte) 135, (byte) 85,
      (byte) 132, (byte) 4, (byte) 100, (byte) 74, (byte) 100, (byte) 76, (byte) 102, (byte) 77,
      (byte) 102, (byte) 75, (byte) 100, (byte) 74,
      (byte) 4, (byte) 105, (byte) 71, (byte) 107, (byte) 72, (byte) 106, (byte) 69, (byte) 105,
      (byte) 69, (byte) 105, (byte) 71, (byte) 4, (byte) 105, (byte) 71, (byte) 104, (byte) 71,
      (byte) 104, (byte) 73, (byte) 106, (byte) 74,
      (byte) 105, (byte) 71, (byte) 4, (byte) 90, (byte) 80, (byte) 91, (byte) 82, (byte) 92,
      (byte) 80, (byte) 90, (byte) 79, (byte) 90, (byte) 80, (byte) 3, (byte) 123, (byte) 52,
      (byte) 125, (byte) 51, (byte) 124, (byte) 49,
      (byte) 123, (byte) 52, (byte) 3, (byte) 89, (byte) 144, (byte) 90, (byte) 145, (byte) 91,
      (byte) 142, (byte) 89, (byte) 144, (byte) 3, (byte) 150, (byte) 115, (byte) 153, (byte) 114,
      (byte) 153, (byte) 115, (byte) 150, (byte) 115,
      (byte) 0, (byte) 83, (byte) 146, (byte) 0, (byte) 90, (byte) 140, (byte) 0, (byte) 82,
      (byte) 134, (byte) 0, (byte) 80, (byte) 133, (byte) 0, (byte) 85, (byte) 117, (byte) 0,
      (byte) 81, (byte) 115, (byte) 0, (byte) 148,
      (byte) 115, (byte) 0, (byte) 128, (byte) 114, (byte) 0, (byte) 152, (byte) 111, (byte) 0,
      (byte) 121, (byte) 108, (byte) 0, (byte) 143, (byte) 108, (byte) 0, (byte) 115, (byte) 107,
      (byte) 0, (byte) 144, (byte) 105, (byte) 0,
      (byte) 90, (byte) 86, (byte) 0, (byte) 131, (byte) 84, (byte) 0, (byte) 133, (byte) 84,
      (byte) 0, (byte) 126, (byte) 81, (byte) 0, (byte) 149, (byte) 80, (byte) 0, (byte) 90,
      (byte) 78, (byte) 0, (byte) 121, (byte) 76,
      (byte) 0, (byte) 96, (byte) 74, (byte) 0, (byte) 108, (byte) 70, (byte) 0, (byte) 112,
      (byte) 68, (byte) 0, (byte) 104, (byte) 67, (byte) 0, (byte) 106, (byte) 67, (byte) 0,
      (byte) 114, (byte) 66, (byte) 0, (byte) 120,
      (byte) 66, (byte) 0, (byte) 99, (byte) 66, (byte) 0, (byte) 148, (byte) 65, (byte) 0,
      (byte) 134, (byte) 64, (byte) 0, (byte) 98, (byte) 63, (byte) 0, (byte) 130, (byte) 64,
      (byte) 0, (byte) 111, (byte) 61, (byte) 0,
      (byte) 128, (byte) 61, (byte) 0, (byte) 109, (byte) 60, (byte) 0, (byte) 112, (byte) 60,
      (byte) 0, (byte) 139, (byte) 58, (byte) 0, (byte) 143, (byte) 59, (byte) 0, (byte) 109,
      (byte) 57, (byte) 0, (byte) 112, (byte) 57,
      (byte) 0, (byte) 141, (byte) 57, (byte) 0, (byte) 96, (byte) 57, (byte) 0, (byte) 126,
      (byte) 57, (byte) 0, (byte) 128, (byte) 56, (byte) 0, (byte) 107, (byte) 55, (byte) 0,
      (byte) 98, (byte) 54, (byte) 0, (byte) 111,
      (byte) 54, (byte) 0, (byte) 114, (byte) 53, (byte) 0, (byte) 138, (byte) 54, (byte) 0,
      (byte) 102, (byte) 52, (byte) 0, (byte) 130, (byte) 52, (byte) 0, (byte) 136, (byte) 50,
      (byte) 0, (byte) 129, (byte) 48, (byte) 0,
      (byte) 133, (byte) 48, (byte) 0, (byte) 113, (byte) 46, (byte) 0, (byte) 110, (byte) 40,
      (byte) 0, (byte) 144, (byte) 41, (byte) 0, (byte) 87, (byte) 142, (byte) 0, (byte) 88,
      (byte) 96, (byte) 0, (byte) 80, (byte) 88,
      (byte) 0, (byte) 113, (byte) 41, (byte) 0, (byte) 71, (byte) 92, (byte) 0, (byte) 135,
      (byte) 61
  };

  private Portrait() {
  }

  public static void draw() {
    int i = 0;
    while (i < DATA.length) {
      int n = DATA[i] & 0xFF;
      i++;
      int x0 = DATA[i] & 0xFF;
      int y0 = (DATA[i + 1] & 0xFF);
      i += 2;
      if (n == 0) {
        line(x0, y0, x0, y0);
        continue;
      }
      int px = x0, py = y0;
      for (int seg = 0; seg < n; seg++) {
        int x = DATA[i] & 0xFF;
        int y = (DATA[i + 1] & 0xFF);
        i += 2;
        line(px, py, x, y);
        px = x;
        py = y;
      }
    }
  }

  public static void line(final int x0, final int y0, final int x1, final int y1) {
    int x = x0;
    int y = y0;
    int dx = x1 - x0;
    int dy = y1 - y0;
    int stepX = 1;
    int stepY = 1;

    if (dx < 0) {
      dx = -dx;
      stepX = -1;
    }
    if (dy < 0) {
      dy = -dy;
      stepY = -1;
    }

    int error = dx - dy;

    while (true) {
      Screen.plot(x, y);
      if (x == x1 && y == y1) {
        return;
      }

      int doubled = error + error;
      if (doubled > -dy) {
        error = error - dy;
        x = x + stepX;
      }
      if (doubled < dx) {
        error = error + dx;
        y = y + stepY;
      }
    }
  }
}
