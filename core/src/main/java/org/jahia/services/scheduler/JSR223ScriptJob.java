package org.jahia.services.scheduler;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.utils.ScriptEngineUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

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

    public final static String JOB_SCRIPT_PATH = "jobScriptPath";
    public final static String JOB_SCRIPT_OUTPUT = "jobScriptOutput";

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        final JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();

        String jobScriptPath = map.getString(JOB_SCRIPT_PATH);
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
                    scriptContext.setWriter(new StringWriter());
                    // The following binding is necessary for Javascript, which doesn't offer a console by default.
                    bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                    Object result = scriptEngine.eval(scriptContent, bindings);
                    StringWriter writer = (StringWriter) scriptContext.getWriter();
                    map.put(JOB_SCRIPT_OUTPUT, writer.toString());
                } catch (ScriptException e) {
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
