package com.igormaznitsa.j2z80.translator.utils;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

public final class JarClassLoaderFactory {

  public static URLClassLoader create(
      final List<Path> jarFiles,
      final ClassLoader parent
  ) {
    final URL[] urls = jarFiles.stream().map(path -> {
      try {
        return path.toUri().toURL();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }).toArray(URL[]::new);
    return new URLClassLoader(urls, parent);
  }
}
