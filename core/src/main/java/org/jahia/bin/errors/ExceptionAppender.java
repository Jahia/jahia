package org.jahia.bin.errors;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;

/**
 * A Log4J appender that will log exceptions through the ErrorFileDumper system.
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Jul 16, 2010
 * Time: 3:40:31 PM
 */
public class ExceptionAppender extends AppenderSkeleton {

    public ExceptionAppender() {
    }

    @Override
    protected void append(LoggingEvent event) {
        if (event.getThrowableInformation() != null) {
            try {
                File errorFile = ErrorFileDumper.dumpToFile(event.getThrowableInformation().getThrowable(), null);
                if (errorFile != null) {
                    System.err.println("Error dumped to file " + errorFile.getAbsolutePath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
    }

    public boolean requiresLayout() {
        return false;
    }

}
