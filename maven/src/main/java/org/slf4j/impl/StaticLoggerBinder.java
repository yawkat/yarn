package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * @author yawkat
 */
@SuppressWarnings("unused")
public class StaticLoggerBinder implements LoggerFactoryBinder {
    private static final StaticLoggerBinder instance = new StaticLoggerBinder();
    public static final String REQUESTED_API_VERSION = "1.7";

    public static StaticLoggerBinder getSingleton() {
        return instance;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return MavenLoggerFactory.instance;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return MavenLoggerFactory.class.getName();
    }
}
