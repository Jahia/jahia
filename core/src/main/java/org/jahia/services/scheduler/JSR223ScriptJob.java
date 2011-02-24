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
