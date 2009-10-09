package org.jahia.services.render;

import org.jahia.services.content.nodetypes.ExtendedNodeType;

import java.util.SortedSet;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
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
