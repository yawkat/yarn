package org.slf4j.impl;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 * @author yawkat
 */
final class MavenLogger extends MarkerIgnoringBase implements Logger {
    private final Log log;

    private boolean trace = false;
    private boolean debug = false;
    private boolean info = false;
    private boolean warn = false;
    private boolean error = false;

    public MavenLogger(String name, Log log) {
        this.name = name;
        this.log = log;

        String path = null;
        String level = null;
        do {
            if (path == null) {
                path = name;
            } else {
                int sep = path.lastIndexOf('.');
                if (sep == -1) {
                    break;
                }
                path = path.substring(0, sep);
            }
            level = System.getProperty("slf4j.level." + path);
        } while (level == null);
        if (level != null) {
            switch (level.toLowerCase()) {
            case "trace":
                trace = true;
            case "debug":
                debug = true;
            case "info":
                info = true;
            case "warn":
                warn = true;
            case "error":
                error = true;
            }
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return trace;
    }

    @Override
    public void trace(String msg) {
        if (isTraceEnabled()) {
            debug(msg);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            debug(format(format, arg));
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isTraceEnabled()) {
            debug(format(format, arg1, arg2));
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (isTraceEnabled()) {
            debug(format(format, arguments));
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (isTraceEnabled()) {
            debug(msg, t);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return debug | log.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        boolean ol = log.isDebugEnabled();
        if (ol) {
            log.debug(msg);
        } else if (debug) {
            log.info(msg);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            debug(format(format, arg));
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            debug(format(format, arg1, arg2));
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnabled()) {
            debug(format(format, arguments));
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        boolean ol = log.isDebugEnabled();
        if (ol) {
            log.debug(msg, t);
        } else if (debug) {
            log.info(msg, t);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return info | log.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        if (isInfoEnabled()) {
            log.info(msg);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            info(format(format, arg));
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            info(format(format, arg1, arg2));
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnabled()) {
            info(format(format, arguments));
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (isInfoEnabled()) {
            log.info(msg, t);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return warn | log.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        if (isWarnEnabled()) {
            log.warn(msg);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            warn(format(format, arg));
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnabled()) {
            warn(format(format, arguments));
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            warn(format(format, arg1, arg2));
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (isWarnEnabled()) {
            log.warn(msg, t);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return error | log.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        if (isErrorEnabled()) {
            log.error(msg);
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            error(format(format, arg));
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            error(format(format, arg1, arg2));
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if (isErrorEnabled()) {
            error(format(format, arguments));
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (isErrorEnabled()) {
            log.error(msg, t);
        }
    }

    private String format(String format, Object... arguments) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        int start = 0;
        int end;
        while ((end = format.indexOf("{}", start)) != -1) {
            builder.append(format, start, end);
            builder.append(arguments[i++]);
            start = end + 2;
        }
        builder.append(format, start, format.length());
        return builder.toString();
    }
}
