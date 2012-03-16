package com.igormaznitsa.j2z80.translator;

import com.igormaznitsa.j2z80.TranslatorLogger;

public class DefaultTranslatorLogger implements TranslatorLogger {
    private static final String DEFAULT_LOG_PREFIX = "J2Z [%1$s] -> %2$s";
    
    @Override
    public void logInfo(final String message) {
        System.out.println(String.format(DEFAULT_LOG_PREFIX, "INFO", message));
    }

    @Override
    public void logWarning(final String message) {
        System.out.println(String.format(DEFAULT_LOG_PREFIX, "WARN", message));
    }

    @Override
    public void logError(final String message) {
        System.err.println(String.format(DEFAULT_LOG_PREFIX, "ERR", message));
    }
    
}
