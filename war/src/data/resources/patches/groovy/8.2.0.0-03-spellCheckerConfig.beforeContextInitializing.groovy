import org.apache.commons.io.FileUtils
import org.jahia.settings.JahiaPropertiesUtils
import org.jahia.settings.SettingsBean
import org.jdom2.Element
import org.jdom2.JDOMException
import org.jdom2.Namespace
import org.jdom2.input.SAXBuilder
import org.jdom2.output.Format
import org.jdom2.output.XMLOutputter

private updateSpellCheckerConfig() {
    log.info("Checks JackRabbit spellChecker configuration migration required or not");
    boolean updated = updateRepositoryConfig(new File(SettingsBean.getInstance().getPathResolver().resolvePath("WEB-INF/etc/repository/jackrabbit/repository.xml")));
    updated = updateRepositoryConfig(new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "repository/workspaces/default/workspace.xml")) || updated;
    updated = updateRepositoryConfig(new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "repository/workspaces/live/workspace.xml")) || updated;
    if (updated) {
        log.info("JackRabbit spellChecker configuration migrated, you can now use jahia.properties " +
                "(jahia.jackrabbit.searchIndex.spellChecker.enabled) to disable/enable spellChecker on JackRabbit search indices.");
    }
}

private updateJahiaProperties(){
    JahiaPropertiesUtils.addEntry("jahia.jackrabbit.searchIndex.spellChecker.enabled",
            "true",
            "# Specifies whether to enable the spell checker for the search index or not\n" +
                    "# Note: If you disable the spell checker, the did-you-mean suggestions will not be available.\n" +
                    "# Note: Disabling the spell checker can improve the performance of the search index.\n",
            ".*jahia\\.jackrabbit\\.searchIndex\\.spellChecker\\.minimumScore.*",
            "A new property was introduced with this version to handle enabling/disabling JackRabbit search indices spellChecker,\n" +
                    "Please manually add the following line into your jahia.properties file if you need to change the default value\n" +
                    "jahia.jackrabbit.searchIndex.spellChecker.enabled = true");
}

private boolean updateRepositoryConfig(File target) {
    try {
        if (target.exists()) {
            log.info("Check repository configuration in file " + target);
            // Do backup
            FileUtils.copyFile(target, new File(target.getAbsolutePath() + ".bak"));

            // Configure Sax and load the file
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setValidation(false);
            saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            doc = saxBuilder.build(target);
            Element root = doc.getRootElement();

            // Check and do the necessary updates
            boolean updated = false;
            if ("Repository".equals(root.getName())) {
                updated = updateWorkspace(root.getChild("Workspace"));
            } else {
                updated = updateWorkspace(root);
            }

            // Write results
            if (updated) {
                Format customFormat = Format.getRawFormat();
                customFormat.setLineSeparator(System.getProperty("line.separator"));
                XMLOutputter xmlOutputter = new XMLOutputter(customFormat);
                xmlOutputter.output(doc, new FileWriter(target));
                log.info("Repository configuration updated in " + target);
                return true;
            }
        }
    } catch (Exception e) {
        log.error("Unable to update repository configuration for " + target, e);
    }
    return false;
}

private boolean updateWorkspace(Element ws) throws JDOMException {
    if (ws == null) {
        return false;
    }

    Element pm = ws.getChild("SearchIndex");
    if (pm == null) {
        return false;
    }
    Namespace ns = pm.getNamespace();

    Element param = null;
    List params = pm.getChildren("param", ns);
    for (Object p : params) {
        if (p instanceof Element && ((Element) p).getAttributeValue("name").equals("spellCheckerClass")) {
            param = (Element) p;
            break;
        }
    }

    if (param != null) {
        String val = param.getAttributeValue("value");
        log.info("Existing value for spellCheckerClass param is: " + val);

        if (val != null && "org.jahia.services.search.spell.CompositeSpellChecker".equals(val)) {
            log.info("The JackRabbit spellChecker is not migrated. Update it");
            param.setAttribute("value", "\${jahia.jackrabbit.searchIndex.spellChecker.spellCheckerClass}");
            return true;
        } else {
            log.info("The JackRabbit spellChecker is already migrated or custom. Skip processing it");
            return false;
        }
    }

    return false;
}

updateSpellCheckerConfig();
updateJahiaProperties();
