package org.jahia.wiki;

import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.component.embed.EmbeddableComponentManager;


import java.io.StringReader; /**
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


    /**
     * Render wiki content as html
     *
     * @param html
     * @return
     * @throws Exception
     */
    public static String renderWikiSyntaxAsXHTML(String html, SyntaxFactory syntaxFactory, String inputSyntax, String outputSyntax) throws Exception {
        logger.debug("Wiki content before processing: " + html);

        // Initialize Rendering components and allow getting instances
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.initialize(syntaxFactory.getClass().getClassLoader());

        // Use a the Converter component to convert between one syntax to another.
        Converter converter = ecm.lookup(Converter.class);

        // Convert input in Wiki Syntax into Output. The result is stored in the printer.
        WikiPrinter printer = new DefaultWikiPrinter();
        converter.convert(new StringReader(html), createInputSyntax(syntaxFactory, inputSyntax), createOutputSyntax(syntaxFactory, outputSyntax), printer);

        String result = printer.toString();
        logger.debug("Wiki content after processing:" + result);
        return printer.toString();


    }

    /**
     * Create InputSyntax
     *
     * @param syntax
     * @return
     */
    private static Syntax createInputSyntax(SyntaxFactory syntaxFactory, String syntax) {
        Syntax inputSyntax;
        if (syntax == null) {
            inputSyntax = Syntax.XHTML_1_0;
        } else {
            try {
                inputSyntax = syntaxFactory.createSyntaxFromIdString(syntax);
            } catch (ParseException e) {
                logger.error(e, e);
                inputSyntax = Syntax.XHTML_1_0;
            }
        }
        logger.debug("Input syntax: " + inputSyntax.toIdString());
        return inputSyntax;
    }

    /**
     * Create output syntax
     *
     * @param syntax
     * @return
     */
    private static Syntax createOutputSyntax(SyntaxFactory syntaxFactory, String syntax) {
        Syntax outputSyntax;
        if (syntax == null) {
            outputSyntax = Syntax.MEDIAWIKI_1_0;
        } else {
            try {
                outputSyntax = syntaxFactory.createSyntaxFromIdString(syntax);
            } catch (ParseException e) {
                logger.error(e, e);
                outputSyntax = Syntax.MEDIAWIKI_1_0;
            }
        }
        logger.debug("Output syntax: " + outputSyntax.toIdString());
        return outputSyntax;
    }

}
