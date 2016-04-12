package org.jahia.osgi;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This LogBridge is used by a custom pax-logging-service appender to bring back up the log messages inside the OSGi
 * runtime back into DX's core.
 */
public class LogBridge {

    public static void log(String loggerName, int level, Object message, Throwable t) {
        Logger logger = LoggerFactory.getLogger(loggerName);
        switch (level) {
            case Level.TRACE_INT:
                logger.trace(message != null ? message.toString() : null, t);
                break;
            case Level.DEBUG_INT:
                logger.debug(message != null ? message.toString() : null, t);
                break;
            case Level.INFO_INT:
                logger.info(message != null ? message.toString() : null, t);
                break;
            case Level.WARN_INT:
                logger.warn(message != null ? message.toString() : null, t);
                break;
            case Level.ERROR_INT:
                logger.error(message != null ? message.toString() : null, t);
                break;
            default:
                logger.info(message != null ? message.toString() : null, t);
        }
    }
}
