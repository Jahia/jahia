package org.jahia.bundles.pax.logging.bridging.appender;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.jahia.osgi.LogBridge;

/**
 * An appender for the Log4J using inside the pax-logging-service implementation that will bridge the log messages
 * to the Log4J implementation used by DX's core.
 */
public class LogBridgeAppender extends AppenderSkeleton {
    @Override
    protected void append(LoggingEvent event) {
        LogBridge.log(event.getLogger().getName(), event.getLevel().toInt(), event.getMessage(), (event.getThrowableInformation() != null ?  event.getThrowableInformation().getThrowable() : null) );
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}