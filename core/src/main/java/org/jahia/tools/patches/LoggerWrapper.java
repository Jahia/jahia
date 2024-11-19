/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.tools.patches;

import java.io.PrintWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

/**
 * SLF4J logger wrapper to also log into the provided instance of {@link PrintWriter}. Used by Groovy Console and {@link GroovyPatcher}.
 *
 * @author Sergiy Shyrkov
 */
public class LoggerWrapper extends org.slf4j.ext.LoggerWrapper {

    private PrintWriter out;

    /**
     * Initializes an instance of this class.
     *
     * @param logger
     * @param fqcn
     */
    public LoggerWrapper(Logger logger, String fqcn, Writer out) {
        super(logger, fqcn);
        this.out = new PrintWriter(out, true);
    }

    @Override
    public void error(String msg) {
        out(msg, null, null);
        super.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        out(format, new Object[] { arg }, null);
        super.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        out(format, new Object[] { arg1, arg2 }, null);
        super.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object[] argArray) {
        out(format, argArray, null);
        super.error(format, argArray);
    }

    @Override
    public void error(String msg, Throwable t) {
        out(msg, null, t);
        super.error(msg, t);
    }

    public void info(Object msg) {
        info(String.valueOf(msg));
    }

    @Override
    public void info(String msg) {
        out(msg, null, null);
        super.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        out(format, new Object[] { arg }, null);
        super.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        out(format, new Object[] { arg1, arg2 }, null);
        super.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object[] argArray) {
        out(format, argArray, null);
        super.info(format, argArray);
    }

    @Override
    public void info(String msg, Throwable t) {
        out(msg, null, t);
        super.info(msg, t);
    }

    private void out(String format, Object[] argArray, Throwable t) {
        out.println(argArray != null ? MessageFormatter.arrayFormat(format, argArray) : format);

        if (t != null) {
            out.println(t.getMessage());
            t.printStackTrace(out);
        }
    }

    @Override
    public void warn(String msg) {
        out(msg, null, null);
        super.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        out(format, new Object[] { arg }, null);
        super.warn(format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        out(format, new Object[] { arg1, arg2 }, null);
        super.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object[] argArray) {
        out(format, argArray, null);
        super.warn(format, argArray);
    }

    @Override
    public void warn(String msg, Throwable t) {
        out(msg, null, t);
        super.warn(msg, t);
    }

}
