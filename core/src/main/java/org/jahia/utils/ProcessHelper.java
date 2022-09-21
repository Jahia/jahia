/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
