package org.jahia.services.render.scripting;

import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Template;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.render.scripting.Script;

import java.util.SortedSet;

/**
 * A ScriptResolver is responsible for resolving the script to be used to render the resource.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 7:22:55 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ScriptResolver {
    public Script resolveScript(Resource resource, RenderContext context) throws TemplateNotFoundException;

    public boolean hasTemplate(ExtendedNodeType nt, String s);

    public SortedSet<Template> getAllTemplatesSet();

    public SortedSet<Template> getTemplatesSet(ExtendedNodeType nt);
}
