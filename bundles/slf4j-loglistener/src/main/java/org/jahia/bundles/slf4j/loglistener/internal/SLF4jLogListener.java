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
