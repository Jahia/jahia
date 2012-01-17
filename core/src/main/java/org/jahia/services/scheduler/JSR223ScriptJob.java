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

package org.jahia.services.scheduler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.utils.ScriptEngineUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public final static String JOB_SCRIPT_OUTPUT = "jobScriptOutput";

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        final JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();

        String jobScriptPath = map.getString(JOB_SCRIPT_PATH);
    	logger.info("Start executing JSR223 script job {}", jobScriptPath);
    	
        ScriptEngine scriptEngine = ScriptEngineUtils.getInstance().scriptEngine(FilenameUtils.getExtension(jobScriptPath));
        if (scriptEngine != null) {
            ScriptContext scriptContext = scriptEngine.getContext();
            final Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("jobDataMap", map);
            InputStream scriptInputStream = JahiaContextLoaderListener.getServletContext().getResourceAsStream(jobScriptPath);
            if (scriptInputStream != null) {
                Reader scriptContent = null;
                try {
                    scriptContent = new InputStreamReader(scriptInputStream);
                    StringWriter out = new StringWriter();
                    scriptContext.setWriter(out);
                    // The following binding is necessary for Javascript, which doesn't offer a console by default.
                    bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                    scriptEngine.eval(scriptContent, bindings);
                    map.put(JOB_SCRIPT_OUTPUT, out.toString());
                	logger.info("...JSR-223 script job {} execution finished", jobScriptPath);
                } catch (ScriptException e) {
                	logger.error("Error during execution of the JSR-223 script job " + jobScriptPath + " execution failed with error " + e.getMessage(), e);
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
