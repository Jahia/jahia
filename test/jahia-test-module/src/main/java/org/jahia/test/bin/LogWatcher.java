/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.test.bin;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.jahia.bin.JahiaController;
import org.jahia.bin.errors.DefaultErrorHandler;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

/**
 * Servlet class creating an appender in the log system, which stores all errors between a start 
 * ( http://[server:port]/logwatcher?op=start ) and stop command ( http://[server:port]/logwatcher?op=stop ).
 * 
 * The stop command returns eventually logged errors since the start command as plain text in the response body.
 */
public class LogWatcher extends JahiaController {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(LogWatcher.class);
    
    private Map<String, LogExceptionExtractor> errorLogAppenders = new HashMap<String, LogExceptionExtractor>();
    private static final String DEFAULT_APPENDER_KEY = "defaultAppenderKey";

    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        try {
            if (request.getMethod().equalsIgnoreCase("GET")) {
                String operation = request.getParameter("op");
                String appenderKey = request.getParameter("key");
                if (StringUtils.isEmpty(appenderKey)) {
                    appenderKey = DEFAULT_APPENDER_KEY;
                }
                LogExceptionExtractor logAppender = errorLogAppenders.remove(appenderKey);
                Logger rootLogger = Logger.getRootLogger();
                if (logAppender != null) {
                    rootLogger.removeAppender(logAppender);
                    logger.info("Error logging (key=" + appenderKey + ") stopped");
                }

                if ("start".equals(operation)) {
                    logAppender = new LogExceptionExtractor();
                    logAppender.setName(appenderKey);
                    errorLogAppenders.put(appenderKey, logAppender);
                    logger.info("Error logging (key=" + appenderKey + ") started");
                    response.getWriter().println("OK");

                    rootLogger.addAppender(logAppender);
                } else if (logAppender != null) {
                    response.getWriter().println(logAppender.getErrorLogs());
                }

            } else if (request.getMethod().equals("OPTIONS")) {
                response.setHeader("Allow", "GET, OPTIONS");
            } else {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            DefaultErrorHandler.getInstance().handle(e, request, response);
        }
        return null;
    }

    
    class LogExceptionExtractor extends AppenderSkeleton {
        StringBuffer errorLogs = new StringBuffer();
        FastDateFormat timestampFormatter = FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss,mmm");
        private String newLine = System.getProperty("line.separator") != null ? System.getProperty("line.separator") : "\n";
        long lastTimeStamp = 0L;

        public LogExceptionExtractor() {
        }

        @Override
        protected void append(LoggingEvent event) {

            if (event.getLevel().toInt() >= Priority.ERROR_INT) {
                StringBuilder errorLog = new StringBuilder();
                if (event.timeStamp - lastTimeStamp > 2000) {
                    errorLog.append(newLine);
                }
                errorLog.append(timestampFormatter.format(new Date(event.timeStamp))).append(" ")
                        .append(event.getRenderedMessage()).append(newLine);
                String[] throwableStringRep = event.getThrowableStrRep();
                if (throwableStringRep != null) {
                    for (String stacktraceLine : throwableStringRep) {
                        errorLog.append(stacktraceLine).append(newLine);
                    }
                    errorLog.append(newLine);
                }
                errorLogs.append(errorLog.toString());
                lastTimeStamp = event.timeStamp;
            }
        }

        public void close() {
        }

        public boolean requiresLayout() {
            return false;
        }

        public String getErrorLogs() {
            return errorLogs.toString();
        }
    }
}
