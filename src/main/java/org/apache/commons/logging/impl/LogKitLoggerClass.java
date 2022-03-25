package org.apache.commons.logging.impl;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

import java.io.Serializable;

public class LogKitLoggerClass implements Serializable {
    private final LogKitLogger logKitLogger;
    /**
     * Logging goes to this {@code LogKit} logger
     */
    protected transient volatile Logger logger;

    public LogKitLoggerClass(LogKitLogger logKitLogger) {
        this.logKitLogger = logKitLogger;
    }

    /**
     * Return the underlying Logger we are using.
     */
    public Logger getLogger() {
        Logger result = logger;
        if (result == null) {
            synchronized (logKitLogger) {
                result = logger;
                if (result == null) {
                    logger = result = Hierarchy.getDefaultHierarchy().getLoggerFor(logKitLogger.getName());
                }
            }
        }
        return result;
    }
}