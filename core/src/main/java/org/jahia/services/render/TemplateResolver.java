package org.jahia.services.render;

import org.jahia.services.content.nodetypes.ExtendedNodeType;

import javax.jcr.RepositoryException;
import java.util.Set;

/**
 * Service used to resolve templates for full page rendering of contents like jnt:page using pageTemplate,
 * but also for any contents using contentTemplate.
 * Usually templates are JCR nodes created in the studio and packaged into modules.
 * But in order to make this system extensible by third party modules we moved the logic in a dedicated implementation
 * and created this interface
 */
public interface TemplateResolver {

    /**
     * Check if a template with the given name exists for the given node type
     * @param templateName the name of the template
     * @param nodeType the node type for which the template must be checked
     * @param templatePackages the set of template packages to check
     * @return true if a template with the given name exists for the given node type
     * @throws RepositoryException in case any things unexpected happens during the check
     */
    boolean hasTemplate(String templateName, ExtendedNodeType nodeType, Set<String> templatePackages) throws RepositoryException;

    /**
     * Resolve the template of the given resource, in the given rendering context
     * @param resource the resource for which the template must be resolved
     * @param renderContext the current rendering context
     * @return the resolved Template
     * @throws RepositoryException in case any things unexpected happens during the resolution
     */
    Template resolveTemplate(Resource resource, RenderContext renderContext) throws RepositoryException;

    /**
     * Flush internal caches related to the template resolution.
     * (in case modules operations start/stop, etc...)
     * @param modulePath the module path that need invalidation
     */
    void flushCache(String modulePath);
}
