/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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