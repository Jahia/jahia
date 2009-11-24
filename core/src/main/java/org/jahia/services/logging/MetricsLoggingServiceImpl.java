/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.logging;

import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 24 nov. 2009
 */
public class MetricsLoggingServiceImpl implements MetricsLoggingService {
    private transient static Logger metricsLogger = LoggerFactory.getLogger("loggingService");
    private transient static Logger logger = LoggerFactory.getLogger(MetricsLoggingServiceImpl.class);
    private Map<String, String> logTemplatesMap;
    private static MetricsLoggingServiceImpl instance;
    private final static String headerTemplate = "user {} ip {} path {} nodetype {} ";

    public MetricsLoggingServiceImpl() {
    }

    public void setLogTemplatesMap(Map<String, String> logTemplatesMap) {
        this.logTemplatesMap = new LinkedHashMap<String, String>(logTemplatesMap.size());
        for (Map.Entry<String, String> entry : logTemplatesMap.entrySet()) {
            this.logTemplatesMap.put(entry.getKey(),headerTemplate+entry.getValue());
        }
    }

    public void logContentEvent(JahiaUser user, String ipAddress, String path, String nodeType, String logTemplate, String... args) {
        Profiler profiler = new Profiler("MetricsLogging");
        profiler.setLogger(logger);
        profiler.start("find log templates");
        String template = logTemplatesMap.get(logTemplate);
        profiler.start("fill parameters");
        String[] templateParameters = new String[4+args.length];
        templateParameters[0] = user.getName();
        templateParameters[1] = ipAddress;
        templateParameters[2] = path;
        templateParameters[3] = nodeType;
        int i=4;
        for (String arg : args) {
            templateParameters[i++] = arg;
        }
        profiler.start("log event");
        metricsLogger.trace(template,templateParameters);
        profiler.log();
    }


    public static MetricsLoggingServiceImpl getInstance() {
        if (instance == null) {
            instance = new MetricsLoggingServiceImpl();            
        }
        return instance;
    }
}
