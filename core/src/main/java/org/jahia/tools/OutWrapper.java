/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.tools;

import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

/**
 * Wrapper around writer and logger to be able to perform simultaneous output into a logger and JSP writer.
 *
 * @author Sergiy Shyrkov
 */
public class OutWrapper {
    private Logger log;
    private Writer out;

    public OutWrapper(Logger logger, Writer out) {
        this.log = logger;
        this.out = out;
    }

    public OutWrapper echo(String message) {
        log.info(message);
        out(message);
        return this;
    }

    public OutWrapper echo(String format, Object arg1) {
        return echo(MessageFormatter.format(format, arg1).getMessage());
    }

    public OutWrapper echo(String format, Object arg1, Object arg2) {
        return echo(MessageFormatter.format(format, arg1, arg2).getMessage());
    }

    public OutWrapper echo(String format, Object arg1, Object arg2, Object arg3) {
        return echo(MessageFormatter.arrayFormat(format, new Object[] { arg1, arg2, arg3 }).getMessage());
    }

    public OutWrapper echo(String format, Object[] args) {
        return echo(MessageFormatter.format(format, args).getMessage());
    }

    private void out(String message) {
        if (out != null) {
            try {
                out.append(message).append("\n").flush();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
