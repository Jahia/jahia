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
package org.jahia.utils;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.jahia.exceptions.JahiaRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

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
    public static int execute(String command, String[] arguments) throws JahiaRuntimeException {
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
    public static int execute(String command, String[] arguments,
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
    public static int execute(String command, String arguments[],
            Map<String, Object> parameterSubstitutionMap, File workingDir, StringBuilder resultOut,
            StringBuilder resultErr) throws JahiaRuntimeException {
        return execute(command, arguments, parameterSubstitutionMap, workingDir, resultOut, resultErr, true);
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
     * @return redirectOutputs if set to <code>true</code> the output of the execution will be also redirected to standard system out and
     *         the error to error out
     * @throws JahiaRuntimeException
     *             in case the process execution failed
     */
    public static int execute(String command, String arguments[],
            Map<String, Object> parameterSubstitutionMap, File workingDir, StringBuilder resultOut,
            StringBuilder resultErr, boolean redirectOutputs) throws JahiaRuntimeException {

        long timer = System.currentTimeMillis();

        CommandLine cmd = new CommandLine(command);

        if (arguments != null && arguments.length > 0) {
            cmd.addArguments(arguments, false);
        }

        if (parameterSubstitutionMap != null && !parameterSubstitutionMap.isEmpty()) {
            cmd.setSubstitutionMap(parameterSubstitutionMap);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Executing command: {}", cmd.toString());
        } else if (redirectOutputs) {
            logger.info("Executing command: ");
            logger.info(cmd.toString());
        }

        int exitValue = 0;

        StringOutputStream out = new StringOutputStream(redirectOutputs ? System.out : null);
        StringOutputStream err = new StringOutputStream(redirectOutputs ? System.err : null);
        try {
            DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(new PumpStreamHandler(out, err));
            if (workingDir != null) {
                if (workingDir.exists() || workingDir.mkdirs()) {
                    executor.setWorkingDirectory(workingDir);
                }
            }
            exitValue = executor.execute(cmd, System.getenv());
        } catch (ExecuteException ee) {
            return ee.getExitValue();
        } catch (Exception e) {
            throw new JahiaRuntimeException(e);
        } finally {
            if (resultErr != null) {
                resultErr.append(err.toString());
            }
            if (resultOut != null) {
                resultOut.append(out.toString());
            }
            if (exitValue > 0) {
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
