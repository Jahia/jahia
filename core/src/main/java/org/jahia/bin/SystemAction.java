package org.jahia.bin;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Performs the default action as system user
 *
 */
public abstract class SystemAction extends Action {

    public ActionResult doExecute(final HttpServletRequest req, final RenderContext renderContext, final Resource resource,
                                  final Map<String, List<String>> parameters, final URLResolver urlResolver) throws Exception {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(JCRSessionFactory.getInstance().getCurrentUser().getUsername(), resource.getWorkspace(), resource.getLocale(), new JCRCallback<ActionResult>() {
            public ActionResult doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    Resource systemResource = new Resource(session.getNode(resource.getNode().getPath()), resource.getTemplateType(), resource.getTemplate() , resource.getContextConfiguration());
                    return doExecuteAsSystem(req, renderContext, systemResource, parameters, urlResolver);
                } catch (Exception e) {
                    throw new RepositoryException(e);
                }
            }
        });
    }

    public abstract ActionResult doExecuteAsSystem(HttpServletRequest req, RenderContext renderContext, Resource resource, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception;

}
