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
package org.jahia.services.scheduler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.utils.ScriptEngineUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.*;

/**
 * This class allows to execute any JSR-223 (Groovy, Velocity, Javascript) compatible script as a background job.
 * It takes a "jobScriptPath" job data map variable  that points to a resource available in the web app and outputs
 * the result of the script in a job data map variable called "jobScriptOutput".
 * <p/>
 * User: loom
 * Date: Oct 8, 2010
 * Time: 9:33:48 AM
 */
public class JSR223ScriptJob extends BackgroundJob {

    private static final Logger logger = LoggerFactory.getLogger(JSR223ScriptJob.class);

    public final static String JOB_SCRIPT_PATH = "jobScriptPath";
    public final static String JOB_SCRIPT_ABSOLUTE_PATH = "jobScriptAbsolutePath";
    public final static String JOB_SCRIPT_OUTPUT = "jobScriptOutput";

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        final JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
        String jobScriptPath;
        boolean isAbsolutePath = false;
        if (map.containsKey(JOB_SCRIPT_ABSOLUTE_PATH)) {
            isAbsolutePath = true;
            jobScriptPath = map.getString(JOB_SCRIPT_ABSOLUTE_PATH);
        } else {
            jobScriptPath = map.getString(JOB_SCRIPT_PATH);
        }
        logger.info("Start executing JSR223 script job {}", jobScriptPath);

        ScriptEngine scriptEngine = ScriptEngineUtils.getInstance().scriptEngine(FilenameUtils.getExtension(
                jobScriptPath));
        if (scriptEngine != null) {
            ScriptContext scriptContext = new SimpleScriptContext();
            final Bindings bindings = new SimpleBindings();
            bindings.put("jobDataMap", map);

            InputStream scriptInputStream;
            if (!isAbsolutePath) {
                scriptInputStream = JahiaContextLoaderListener.getServletContext().getResourceAsStream(jobScriptPath);
            } else {
                scriptInputStream = FileUtils.openInputStream(new File(jobScriptPath));
            }
            if (scriptInputStream != null) {
                Reader scriptContent = null;
                try {
                    scriptContent = new InputStreamReader(scriptInputStream);
                    StringWriter out = new StringWriter();
                    scriptContext.setWriter(out);
                    // The following binding is necessary for Javascript, which doesn't offer a console by default.
                    bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                    scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                    scriptContext.setBindings(scriptEngine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE),
                            ScriptContext.GLOBAL_SCOPE);
                    scriptEngine.eval(scriptContent, scriptContext);
                    map.put(JOB_SCRIPT_OUTPUT, out.toString());
                    logger.info("...JSR-223 script job {} execution finished", jobScriptPath);
                } catch (ScriptException e) {
                    logger.error("Error during execution of the JSR-223 script job " + jobScriptPath +
                                 " execution failed with error " + e.getMessage(), e);
                    throw new Exception("Error during execution of script " + jobScriptPath, e);
                } finally {
                    if (scriptContent != null) {
                        IOUtils.closeQuietly(scriptContent);
                    }
                }
            }
        }
    }
}
