package org.jahia.osgi.http.bridge;

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.jahia.services.SpringContextSingleton;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;

import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * OSGi framework service
 *
 * @author loom
 *         Date: Oct 11, 2010
 *         Time: 5:17:53 PM
 */
public class FrameworkService {
    
    private static final Logger logger = LoggerFactory.getLogger(FrameworkService.class);
    private static FrameworkService instance;
    private final ServletContext context;
    private Felix felix;
    private ProvisionActivator provisionActivator = null;

    public FrameworkService(ServletContext context) {
        this.context = context;
        instance = this;
    }

    public void start() throws Exception {
        doStart();
    }

    public void stop() throws Exception {
        doStop();
    }

    public ProvisionActivator getProvisionActivator() {
        return provisionActivator;
    }

    private void doStart()
            throws Exception {
        Felix tmp = new Felix(createConfig());
        tmp.start();
        this.felix = tmp;

        logger.info("OSGi framework started");
    }

    private void doStop()
            throws Exception {
        provisionActivator = null;
        if (this.felix != null) {
            this.felix.stop();
            logger.info("Waiting for OSGi framework shutdown...");
            this.felix.waitForStop(10000);
        }

        logger.info("OSGi framework stopped");
    }

    private Map<String, Object> createConfig()
            throws Exception {

        Map<String,String> unreplaced = (Map<String,String>) SpringContextSingleton.getBean("felixProperties");
        PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}");

        HashMap<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<String, String> entry : unreplaced.entrySet()) {
            map.put(entry.getKey(), placeholderHelper.replacePlaceholders(entry.getValue(), System.getProperties()));
        }

        map.put("org.jahia.servlet.context", context);

        provisionActivator = new ProvisionActivator(this.context);
        map.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, Arrays.asList(provisionActivator));
        return map;
    }

    public static BundleContext getBundleContext() {
        if (instance != null && instance.felix != null) {
            return instance.felix.getBundleContext();
        } else {
            return null;
        }
    }
}
