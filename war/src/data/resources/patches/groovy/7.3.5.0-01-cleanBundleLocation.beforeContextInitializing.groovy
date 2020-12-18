import org.jahia.settings.SettingsBean

def bundleDeployed = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/bundles-deployed")
def mapFile = new File(bundleDeployed, "module-bundle-location.map")

if (!mapFile.exists()) {
    log.warn("Clean-up of module-bundle-location.map file have been aborted because module-bundle-location.map file not found");
    return
}

java.util.Properties mapProps = new java.util.Properties()
mapProps.load(new FileInputStream(mapFile))

boolean updated = false;
for (Iterator<Map.Entry<Object, Object>> it = mapProps.entrySet().iterator(); it.hasNext();) {
    Map.Entry<Object, Object> entry = it.next()
    String filePath = (String) entry.getValue()
    if (!(new File(filePath)).exists()) {
        log.info("Removing entry: " + entry.getKey() + " -> " + entry.getValue())
        it.remove()
        updated = true
    }
}

if (updated) {
    mapProps.store(new FileOutputStream(mapFile), null)
}
