import org.apache.commons.io.FileUtils
import org.jahia.commons.Version
import org.jahia.settings.SettingsBean
import org.jahia.tools.patches.Patcher
import org.jdom.Document
import org.jdom.Element
import org.jdom.JDOMException
import org.jdom.Namespace
import org.jdom.input.SAXBuilder
import org.jdom.output.Format
import org.jdom.output.XMLOutputter
import org.slf4j.Logger


if (Patcher.getInstance().getJahiaPreviousVersion() != null && Patcher.getInstance().getJahiaPreviousVersion().compareTo(new Version("8.0.0.0")) < 1) {
    log.info("Migrating to jackrabbit 2.18");

    updateDefaultAnalyzer();

    setupReindex();
}

private updateDefaultAnalyzer() {
    updateRepositoryConfig(new File(SettingsBean.getInstance().getPathResolver().resolvePath("WEB-INF/etc/repository/jackrabbit/repository.xml")));
    updateRepositoryConfig(new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "repository/workspaces/default/workspace.xml"));
    updateRepositoryConfig(new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "repository/workspaces/live/workspace.xml"));
}

private void updateRepositoryConfig(File target) {
    try {
        if (target.exists()) {
            log.info("Modifying repository configuration in file " + target);
            FileUtils.copyFile(target, new File(target.getAbsolutePath() + ".bak"));
            RepositoryConfigurator cfg = new RepositoryConfigurator(target, log);
            cfg.update();
            cfg.writeTo(target);
            log.info("Repository configuration updated in " + target);
        }
    } catch (Exception e) {
        log.error("Unable to update repository configuration for " + target, e);
    }
}

private setupReindex() {
    // first let's calculate the total size of the indexes
    File globalIndexDir = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "repository/index");
    if (!globalIndexDir.exists()) {
        return;
    }

    File defaultWorkspaceIndexDir = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "repository/workspaces/default/index");
    File liveWorkspaceIndexDir = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "repository/workspaces/live/index");

    long globalIndexSize = FileUtils.sizeOfDirectory(globalIndexDir);
    long defaultWorkspaceIndexSize = FileUtils.sizeOfDirectory(defaultWorkspaceIndexDir);
    long liveWorkspaceIndexSize = FileUtils.sizeOfDirectory(liveWorkspaceIndexDir);

    long totalIndexSize = globalIndexSize + defaultWorkspaceIndexSize + liveWorkspaceIndexSize;
    log.info("Total jackrabbit index size = " + totalIndexSize + " bytes");
    long maxIndexSize = Long.getLong("maxIndexSize", 1024L * 1024L * 1024L);
    if (totalIndexSize > maxIndexSize) {
        log.info("----------------------------------------------------------------------------------------------------------------------------");
        log.info("WARNING: larger than " + maxIndexSize + " index size, please contact Jahia for proper index migration procedure or use command line -DmaxIndexSize to set maximum size to another limit");
        log.info("----------------------------------------------------------------------------------------------------------------------------");
    } else {
        log.info("Adding reindex file to force re-indexation on Jahia startup.");
        FileUtils.touch(new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "repository/reindex"));
    }
}

class RepositoryConfigurator {

    private Document doc;

    private File source;

    private Logger log;

    public RepositoryConfigurator(File source, Logger log) {
        super();
        this.source = source;
        this.log = log;
    }

    public void update() throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder();
        saxBuilder.setValidation(false);
        saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        doc = saxBuilder.build(source);
        Element root = doc.getRootElement();

        if ("Repository".equals(root.getName())) {
            updateWorkspace(root.getChild("Workspace"));
        } else {
            updateWorkspace(root);
        }
    }

    private void updateAnalyzer(Element pm) throws JDOMException {
        if (pm == null) {
            return;
        }
        Namespace ns = pm.getNamespace();

        Element param = null;
        List params = pm.getChildren("param", ns);
        for (Object p : params) {
            if (p instanceof Element && ((Element) p).getAttributeValue("name").equals("analyzer")) {
                param = (Element) p;
                break;
            }
        }
        String val = param.getAttributeValue("value");
        if (val != null) {
            log.info("Existing value for analyzer param is: " + val);
            if (!"org.jahia.services.search.analyzer.DefaultLanguageAnalyzer".equals(val)) {
                log.info("The value already is not the removed DefaultLanguageAnalyzer. Skip processing it");
                return;
            }
        }
        String value = "org.jahia.services.search.analyzer.EnglishSnowballAnalyzer";
        param.setAttribute("value", value);
        log.info("Set value for analyzer param to: " + value);
    }

    private void updateWorkspace(Element ws) throws JDOMException {
        if (ws == null) {
            return;
        }


        updateAnalyzer(ws.getChild("SearchIndex"));
    }

    public void writeTo(File target) throws IOException {
        Format customFormat = Format.getRawFormat();
        customFormat.setLineSeparator(System.getProperty("line.separator"));
        XMLOutputter xmlOutputter = new XMLOutputter(customFormat);
        xmlOutputter.output(doc, new FileWriter(target));
    }
}
