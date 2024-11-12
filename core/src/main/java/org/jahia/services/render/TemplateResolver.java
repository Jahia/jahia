/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
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
