import org.jahia.osgi.BundleUtils
import org.osgi.service.cm.ConfigurationAdmin

def configAdmin = BundleUtils.getOsgiService(ConfigurationAdmin.class, null);
def configs = configAdmin.listConfigurations("(service.pid=org.apache.karaf.cellar.node)");
if (configs != null && configs.length > 0) {
    log.info("Updating Clustering/Cellar configuration...");
    def configuration = configs[0];
    // manually add additional configuration to upgrade existing cfg
    Dictionary<String, Object> props = configuration.getProperties();
    if (props != null) {
        log.info("Adding new props: config.integrityCheck.retryCount=5, config.integrityCheck.retryIntervalMS=100");
        props.put("config.integrityCheck.retryCount", "5");
        props.put("config.integrityCheck.retryIntervalMS", "100");
        configuration.update(props);
        log.info("Clustering Configuration updated.");
    }
}
