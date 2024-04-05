import org.jahia.services.content.JCRCallback
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.JCRTemplate
import org.jahia.services.sites.JahiaSitesService

import javax.jcr.RepositoryException

final String LEGACY_COMPONENT_NAME = "legacy-default-components"

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Integer>() {
    Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
        boolean atLeastOneUpdate = false
        JahiaSitesService.getInstance().getSitesNodeList(session).forEach { siteNode ->
            if (!siteNode.getName().equals("systemsite")) {
                List<String> installedModule = siteNode.getInstalledModulesFromProperty()
                if (!installedModule.contains(LEGACY_COMPONENT_NAME)) {
                    log.info("Activate legacy-default-components on: " + siteNode.getName())
                    installedModule.add(LEGACY_COMPONENT_NAME)
                    siteNode.setInstalledModules(installedModule)
                    atLeastOneUpdate = true
                }
            }
        }
        if (atLeastOneUpdate) {
            session.save()
        }
        return null
    }
})
