package org.jahia.wiki;

import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.LinkLabelGenerator;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.xhtml.XHTMLLinkRenderer;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.transformation.TransformationManager;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.listener.LinkType;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.jahia.services.render.RenderContext;
import org.jahia.services.content.JCRNodeWrapper;


import java.io.StringReader;
import java.util.List; /**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 **/

/**
 * User: ktlili
 * Date: Dec 4, 2009
 * Time: 3:13:47 PM
 */
public class WikiRenderer {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(WikiRenderer.class);
    private static EmbeddableComponentManager componentManager;


    /**
     * Get componentManager. If null, created a new one
     *
     * @param classLoader
     * @return
     */
    private static ComponentManager getComponentManager(ClassLoader classLoader) throws Exception {
        if (componentManager == null) {
            componentManager = new EmbeddableComponentManager();
            componentManager.initialize(classLoader);
            // register use our linkRenderer as link renderer
            DefaultComponentDescriptor<XHTMLLinkRenderer> componentDescriptor = new DefaultComponentDescriptor<XHTMLLinkRenderer>();
            componentDescriptor.setRole(XHTMLLinkRenderer.class);
            componentDescriptor.setImplementation(CustomXHTMLLinkRenderer.class);
            componentDescriptor.setRoleHint("default");
            componentManager.registerComponent(componentDescriptor);


        }
        return componentManager;

    }

    /**
     * Render wiki content as html
     *
     * @param html
     * @return
     * @throws Exception
     */
    public static String renderWikiSyntaxAsXHTML(RenderContext renderContext, String html, SyntaxFactory syntaxFactory, String inputSyntax, String outputSyntax) throws Exception {
        logger.debug("Wiki content before processing: " + html);
        // Initialize Rendering components and allow getting instances
        ComponentManager componentManager = getComponentManager(syntaxFactory.getClass().getClassLoader());

        // update the renderContext
        CustomXHTMLLinkRenderer linkRenderer = (CustomXHTMLLinkRenderer) componentManager.lookup(XHTMLLinkRenderer.class);
        linkRenderer.setRenderContext(renderContext);

        // add .html if there is no extention
        Parser parser = componentManager.lookup(Parser.class, inputSyntax);
        XDOM xdom = parser.parse(new StringReader(html));


        // Execute transformations (for example this executes the Macros which are implemented as Transformations).
        TransformationManager txManager = componentManager.lookup(TransformationManager.class);
        txManager.performTransformations(xdom, parser.getSyntax());

        // Generate XWiki 2.0 Syntax as output for example
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer renderer = componentManager.lookup(BlockRenderer.class, outputSyntax);
        renderer.render(xdom, printer);


        String result = printer.toString();
        logger.debug("Wiki content after processing:" + result);
        return printer.toString();


    }


}
