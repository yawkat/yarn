package org.slf4j.impl;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * @author yawkat
 */
public class MavenLoggerFactory implements ILoggerFactory {
    public static final MavenLoggerFactory instance = new MavenLoggerFactory();

    private Log log;

    public void setLog(Log log) {
        this.log = log;
    }

    @Override
    public Logger getLogger(String name) {
        if (log == null) {
            log = new SystemStreamLog();
        }
        return new MavenLogger(name, log);
    }
}
