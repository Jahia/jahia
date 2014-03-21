package org.jahia.bundles.slf4j.loglistener.internal;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of an OSGi log listener that logs entries into SLF4j
 */
public class SLF4jLogListener implements LogListener {

    private final Logger logger = LoggerFactory.getLogger(SLF4jLogListener.class);

    @Override
    public void logged(LogEntry entry) {
        switch (entry.getLevel()) {
            case LogService.LOG_DEBUG:
                if (logger.isDebugEnabled()) {
                    if (entry.getException() != null) {
                        logger.debug(getLogMessage(entry), entry.getException());
                    } else {
                        logger.debug(getLogMessage(entry));
                    }
                }
                break;
            case LogService.LOG_INFO:
                if (logger.isInfoEnabled()) {
                    if (entry.getException() != null) {
                        logger.info(getLogMessage(entry), entry.getException());
                    } else {
                        logger.info(getLogMessage(entry));
                    }
                }
                break;
            case LogService.LOG_WARNING:
                if (logger.isWarnEnabled()) {
                    if (entry.getException() != null) {
                        logger.warn(getLogMessage(entry), entry.getException());
                    } else {
                        logger.warn(getLogMessage(entry));
                    }
                }
                break;
            case LogService.LOG_ERROR:
                if (logger.isErrorEnabled()) {
                    if (entry.getException() != null) {
                        logger.error(getLogMessage(entry), entry.getException());
                    } else {
                        logger.error(getLogMessage(entry));
                    }
                }
                break;
            default:
                // by default log at info
                if (logger.isInfoEnabled()) {
                    if (entry.getException() != null) {
                        logger.info(entry.getMessage(), entry.getException());
                    } else {
                        logger.info(entry.getMessage());
                    }
                }
        }
    }

    private String getLogMessage(LogEntry logEntry) {
        return "OSGi Bundle ("+logEntry.getBundle().getBundleId()+":"+logEntry.getBundle().getSymbolicName()+") Log : " + logEntry.getMessage();
    }
}
