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
        props.put("config.excluded.properties", "felix.fileinstall.filename, felix.fileinstall.dir, felix.fileinstall.tmpdir");
        configuration.update(props);
        log.info("Clustering Configuration updated.");
    }
}
