import org.jahia.services.content.JCRTemplate
import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRSessionWrapper
import javax.jcr.RepositoryException
import javax.jcr.query.QueryResult
import javax.jcr.NodeIterator
import org.jahia.services.content.JCRNodeWrapper
import javax.jcr.query.Query
import org.jahia.services.content.JCRObservationManager
import org.jahia.registries.ServicesRegistry
import javax.jcr.Value
import org.jahia.services.templates.JahiaTemplateManagerService
import org.jahia.data.templates.JahiaTemplatesPackage

if (!org.jahia.settings.SettingsBean.getInstance().isProcessingServer()) {
    return;
}

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
    public Object doInJCR(JCRSessionWrapper jcrsession) throws RepositoryException {
        JCRObservationManager.setEventsDisabled(Boolean.TRUE);
        try {
            JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
            QueryResult result = jcrsession.getWorkspace().getQueryManager().createQuery("select * from [jnt:virtualsite]", Query.JCR_SQL2).execute();
            NodeIterator ni = result.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper site = (JCRNodeWrapper) ni.next();
                if (site.hasProperty("j:installedModules")) {
                    Value[] installedModules = site.getProperty("j:installedModules").getValues();
                    List<String> correctInstalledModules = new ArrayList<String>();
                    for (Value v : installedModules) {
                        String folderName = v.getString();
                        if (templateManagerService.getTemplatePackageByFileName(folderName) == null) {
                            JahiaTemplatesPackage templatesPackage = templateManagerService.getTemplatePackage(folderName);
                            if (templatesPackage != null) {
                                folderName = templatesPackage.getRootFolder();
                            }
                        }
                        correctInstalledModules.add(folderName);
                    }
                    site.setProperty("j:installedModules", correctInstalledModules.toArray(new String[correctInstalledModules.size()]));
                }
                jcrsession.save();
            }
        } finally {
            JCRObservationManager.setEventsDisabled(Boolean.FALSE);
        }
        return null;
    }
});
