package org.jahia.osgi.http.bridge;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;

import javax.servlet.ServletContext;
import java.util.*;

/**
 * OSGi framework service
 *
 * @author loom
 *         Date: Oct 11, 2010
 *         Time: 5:17:53 PM
 */
public class FrameworkService {
    private final ServletContext context;
    private Felix felix;
    private ProvisionActivator provisionActivator = null;

    public FrameworkService(ServletContext context) {
        this.context = context;
    }

    public void start() {
        try {
            doStart();
        } catch (Exception e) {
            log("Failed to start framework", e);
        }
    }

    public void stop() {
        try {
            doStop();
        } catch (Exception e) {
            log("Error stopping framework", e);
        }
    }

    public ProvisionActivator getProvisionActivator() {
        return provisionActivator;
    }

    private void doStart()
            throws Exception {
        Felix tmp = new Felix(createConfig());
        tmp.start();
        this.felix = tmp;
        log("OSGi framework started", null);
    }

    private void doStop()
            throws Exception {
        provisionActivator = null;
        if (this.felix != null) {
            this.felix.stop();
            log("Waiting for OSGi framework shutdown...", null);
            this.felix.waitForStop(10000);
        }

        log("OSGi framework stopped", null);
    }

    private Map<String, Object> createConfig()
            throws Exception {
        Properties props = new Properties();
        props.load(this.context.getResourceAsStream("/WEB-INF/felix-framework.properties"));

        HashMap<String, Object> map = new HashMap<String, Object>();
        for (Object key : props.keySet()) {
            map.put(key.toString(), props.get(key));
        }

        map.put("org.jahia.servlet.context", context);
        resolveVariables(map);

        provisionActivator = new ProvisionActivator(this.context);
        map.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, Arrays.asList(provisionActivator));
        return map;
    }

    private void resolveVariables(Map<String,Object> map) {
        // @ todo this method doesn't support variable marker escaping yet.
        boolean fullyResolved = false;
        while (!fullyResolved) {
            for (Map.Entry<String,Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof String) {
                    String resolvedValue = resolveStringVariables(entry.getKey(), (String) entry.getValue(), map, new HashSet<String>());
                    if (resolvedValue.equals((String) entry.getValue())) {
                        fullyResolved = true;
                    } else {
                        map.put(entry.getKey(), resolvedValue);
                        fullyResolved = false;
                    }
                }
            }
        }
    }

    private String resolveStringVariables(final String key, final String value,Map<String, Object> map, Set<String> variableStack) {
        if (variableStack.contains(key)) {
            return value;
        }
        variableStack.add(key);
        int variableBeginMarkerPos = value.indexOf("${");
        boolean loopContinued = (variableBeginMarkerPos > -1);
        String result = value;
        while (loopContinued) {
            int variableEndMarkerPos = result.indexOf("}", variableBeginMarkerPos);
            if (variableEndMarkerPos > -1) {
                // we have found both beginning and end markers for a variable.
                String variableName = result.substring(variableBeginMarkerPos + "${".length(), variableEndMarkerPos);
                if (map.containsKey(variableName)) {
                    Object variableValue = map.get(variableName);
                    if (variableValue instanceof String) {
                        String variableValueStr = (String) variableValue;
                        result = result.substring(0, variableBeginMarkerPos) + resolveStringVariables(variableName, variableValueStr, map, variableStack) + result.substring(variableEndMarkerPos + "}".length());
                    } else {
                        result = result.substring(0, variableBeginMarkerPos) + variableValue.toString() + result.substring(variableEndMarkerPos + "}".length());
                    }
                } if (System.getProperty(variableName) != null) {
                    String systemPropertyValue = System.getProperty(variableName);
                    result = result.substring(0, variableBeginMarkerPos) + systemPropertyValue + result.substring(variableEndMarkerPos + "}".length());
                } else {
                    if ("servletContextDir".equals(variableName)) {
                        int nextCommaPos = result.indexOf(",", variableEndMarkerPos+"}".length());
                        String contextRelativePath = null;
                        if (nextCommaPos > -1) {
                            // we found a comma, let's use the value until the comma to build the complete path
                            contextRelativePath = value.substring(variableEndMarkerPos+"}".length(), nextCommaPos).trim();
                            String realPath = context.getRealPath(contextRelativePath);
                            if (realPath != null) {
                                result = result.substring(0, variableBeginMarkerPos) + realPath + result.substring(nextCommaPos);
                            }
                        } else {
                            // otherwise we use the complete string
                            contextRelativePath = result.substring(variableEndMarkerPos+"}".length()).trim();
                            String realPath = context.getRealPath(contextRelativePath);
                            if (realPath != null) {
                                result = result.substring(0, variableBeginMarkerPos) + realPath;
                            }
                        }
                    } else {
                        // no substituation exist, we stop the looping here.
                        loopContinued = false;
                    }
                }
            }
            if (loopContinued) {
                variableBeginMarkerPos = result.indexOf("${");
                loopContinued = (variableBeginMarkerPos > -1);
            }
        }
        variableStack.remove(key);
        return result;
    }

    private void log(String message, Throwable cause) {
        this.context.log(message, cause);
    }
}
