import org.jahia.osgi.BundleUtils
import org.osgi.service.cm.ConfigurationAdmin

/**
 * Upgrade script to automatically configure shards/replica templates and create new indexes
 */
log.info("Starting Clustering migration to 8.1.0.3");
updateCfg();


void updateCfg() {
    def configAdmin = BundleUtils.getOsgiService(ConfigurationAdmin.class, null);
    def configs = configAdmin.listConfigurations("(service.pid=org.apache.karaf.cellar.node)");
    if (configs != null && configs.length > 0) {
        log.info("Updating Clustering/Cellar configuration...");
        def configuration = configs[0];
        // manually add additional configuration to upgrade existing cfg
        Dictionary<String, Object> props = configuration.getProperties();
        if (props != null && props.get("handler.org.jahia.bundles.clustering.impl.ClusterBundleEventHandler") == null) {
            putProps(props, "handler.org.jahia.bundles.clustering.impl.ClusterBundleEventHandler", Boolean.TRUE);
            props.remove("handler.org.jahia.bundles.clustering.impl.BundleEventHandler");
            configuration.update(props);
            log.info("Clustering Configuration updated.");
        }
    } else {
        log.info("Could not find an existing configuration for Clustering, no need to migrate.")
    }
}

void putProps(Dictionary<String, Object> props, String key, Object val) {
    Object oldVal = props.get(key);
    if (oldVal == null) {
        props.put(key, val)
    }
}
