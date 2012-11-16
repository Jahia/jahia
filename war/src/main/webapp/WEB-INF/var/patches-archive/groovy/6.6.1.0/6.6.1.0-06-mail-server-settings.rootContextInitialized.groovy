import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener
import org.jahia.registries.ServicesRegistry
import org.jahia.services.mail.MailSettings
import org.jahia.settings.SettingsBean
import org.jahia.utils.properties.PropertiesManager


if (!org.jahia.settings.SettingsBean.getInstance().isProcessingServer()) {
    return;
}

def log = log;

log.info("Start migrating mail server settings...")

Properties props = SettingsBean.getInstance().getPropertiesFile();

if (props.containsKey("mail_service_activated")
        || props.containsKey("mail_server")
        || props.containsKey("mail_from")
        || props.containsKey("mail_administrator")
        || props.containsKey("mail_paranoia")) {

    MailSettings cfg = new MailSettings();
    cfg.setServiceActivated(Boolean.valueOf(props.getProperty("mail_service_activated", "false")));
    cfg.setUri(props.getProperty("mail_server", null));
    cfg.setFrom(props.getProperty("mail_from", null));
    cfg.setTo(props.getProperty("mail_administrator", null));
    cfg.setNotificationLevel(StringUtils.defaultIfBlank(props.getProperty("mail_paranoia", "Disabled"), "Disabled"));

    log.info("Loaded mail settings: {}", cfg);
    
    ServicesRegistry.getInstance().getMailService().store(cfg);
    
    String jahiaPropFilePath = JahiaContextLoaderListener.getServletContext().getRealPath(SettingsBean.JAHIA_PROPERTIES_FILE_PATH);
    if (jahiaPropFilePath != null) {
        File jahiaProp = new File(jahiaPropFilePath);
        if (jahiaProp.exists()) {
            // remove settings from jahia.properties
            PropertiesManager mgr = new PropertiesManager(jahiaPropFilePath);
            mgr.removeProperty("mail_service_activated");
            mgr.removeProperty("mail_server");
            mgr.removeProperty("mail_from");
            mgr.removeProperty("mail_administrator");
            mgr.removeProperty("mail_paranoia");
            
            log.info("Removing settings from the file {}", jahiaProp.toString());
            
            mgr.storeProperties();
        }
    }
}

log.info("... mail server settings migration completed.")
