package org.jahia.modules.system;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSitesService;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

public class SystemSiteInitializer implements InitializingBean {
    private JCRTemplate jcrTemplate;
    private JahiaSitesService sitesService;

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:virtualsite]", Query.JCR_SQL2);
                NodeIterator ni = q.execute().getNodes();

                while (ni.hasNext()) {
                    JCRSiteNode node = (JCRSiteNode) ni.next();
                    if (!node.getName().equals(JahiaSitesService.SYSTEM_SITE_KEY) && node.hasProperty("j:languages")) {
                        sitesService.updateSystemSiteLanguages(node, session);
                    }
                }
                JCRSiteNode siteByKey = sitesService.getSiteByKey(JahiaSitesService.SYSTEM_SITE_KEY, session);
                sitesService.updateSystemSitePermissions(siteByKey, session);
                session.save();
                return null;
            }
        });
    }
}
