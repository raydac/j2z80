package com.igormaznitsa.j2z80.translator.optimizator.base;

import java.util.Collections;
import java.util.List;

interface OptimizationConst {
  String CLRLOC_STR = "CLRLOC";
  List<String> NONE = Collections.emptyList();
  List<String> CLRLOC = Collections.singletonList(CLRLOC_STR);
}
