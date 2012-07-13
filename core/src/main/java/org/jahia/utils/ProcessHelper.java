/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils;

import java.io.File;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for executing external processes.
 * 
 * @author Sergiy Shyrkov
 */
public final class ProcessHelper {

    private static final Logger logger = LoggerFactory.getLogger(ProcessHelper.class);

    /**
     * Executes the external process using the provided command.
     * 
     * @param command
     *            the command to be executed
     * @return the execution status
     * @throws JahiaRuntimeException
     *             in case the process execution failed
     */
    public static int execute(String command) throws JahiaRuntimeException {
        return execute(command, null);
    }

    /**
     * Executes the external process using the provided command, arguments (optional).
     * 
     * @param command
     *            the command to be executed
     * @param arguments
     *            optional arguments for the command
     * @return the execution status
     * @throws JahiaRuntimeException
     *             in case the process execution failed
     */
    public static int execute(String command, String arguments) throws JahiaRuntimeException {
        return execute(command, arguments, null);
    }

    /**
     * Executes the external process using the provided command, arguments (optional), parameter substitution map to expand variables in the
     * command or arguments in form of <code>${variable}<code> (optional).
     * 
     * @param command
     *            the command to be executed
     * @param arguments
     *            optional arguments for the command
     * @param parameterSubstitutionMap
     *            optional values for variables to be expanded
     * @return the execution status
     * @throws JahiaRuntimeException
     *             in case the process execution failed
     */
    public static int execute(String command, String arguments,
            Map<String, Object> parameterSubstitutionMap) throws JahiaRuntimeException {

        return execute(command, arguments, parameterSubstitutionMap, null, null, null);
    }

    /**
     * Executes the external process using the provided command, arguments (optional), parameter substitution map to expand variables in the
     * command or arguments in form of <code>${variable}<code> (optional) and a working directory (optional).
     * Buffers for process output and error stream can be provided.
     * 
     * @param command
     *            the command to be executed
     * @param arguments
     *            optional arguments for the command
     * @param parameterSubstitutionMap
     *            optional values for variables to be expanded
     * @param workingDir
     *            optional working directory for the process to be started from
     * @param resultOut
     *            the buffer to write the process execution output into (optional)
     * @param resultErr
     *            the buffer to write the process execution error into (optional)
     * @return the execution status
     * @throws JahiaRuntimeException
     *             in case the process execution failed
     */
    public static int execute(String command, String arguments,
            Map<String, Object> parameterSubstitutionMap, File workingDir, StringBuilder resultOut,
            StringBuilder resultErr) throws JahiaRuntimeException {

        long timer = System.currentTimeMillis();

        CommandLine cmd = new CommandLine(command);

        if (StringUtils.isNotEmpty(arguments)) {
            cmd.addArguments(arguments);
        }

        if (parameterSubstitutionMap != null && !parameterSubstitutionMap.isEmpty()) {
            cmd.setSubstitutionMap(parameterSubstitutionMap);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Execuiting command: {}", cmd.toString());
        }

        int exitValue = 0;

        StringOutputStream out = new StringOutputStream();
        StringOutputStream err = new StringOutputStream();
        try {
            DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(new PumpStreamHandler(out, err));
            if (workingDir != null) {
                if (workingDir.exists() || workingDir.mkdirs()) {
                    executor.setWorkingDirectory(workingDir);
                }
            }
            exitValue = executor.execute(cmd, System.getenv());
        } catch (Exception e) {
            throw new JahiaRuntimeException(e);
        } finally {
            if (resultErr != null) {
                resultErr.append(err.toString());
            }
            if (resultOut != null) {
                resultOut.append(out.toString());
            }
            if (err.getLength() > 0) {
                logger.error("External process finished with error. Cause: {}", err.toString());
            }
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Execution took {} ms and finished with status {} and output {}",
                        new Object[] { (System.currentTimeMillis() - timer), exitValue,
                                out.toString() });
            }
        }

        return exitValue;
    }

}
