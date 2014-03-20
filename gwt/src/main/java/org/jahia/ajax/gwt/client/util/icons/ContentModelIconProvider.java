/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.ajax.gwt.client.util.icons;

import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Icon provider for for different types of content objects.
 * User: ktlili
 * Date: Jul 15, 2009
 * Time: 10:50:04 AM
 */
public class ContentModelIconProvider implements ModelIconProvider<GWTJahiaNode> {

    private static ContentModelIconProvider iconProvider = new ContentModelIconProvider();

    private ContentModelIconProvider() {
        super();
    }


    public static ContentModelIconProvider getInstance() {
        if (iconProvider == null) {
            iconProvider = new ContentModelIconProvider();
        }
        return iconProvider;
    }


    /**
     * Return an AbstractImagePrototype depending on the extension and the displayLock flag
     *
     * @param gwtJahiaNode
     * @return
     */
    public AbstractImagePrototype getIcon(GWTJahiaNode gwtJahiaNode) {
        return getIcon(gwtJahiaNode, false);
    }

    public AbstractImagePrototype getIcon(GWTJahiaNode gwtJahiaNode, boolean large) {
        if (gwtJahiaNode != null) {
            String icon = gwtJahiaNode.getIcon();
            boolean isOpened = gwtJahiaNode.isExpandOnLoad();
            return getIcon(icon, isOpened, large);
        }
        return null;
    }


    public AbstractImagePrototype getIcon(GWTJahiaNodeType gwtJahiaNodeType) {
        return getIcon(gwtJahiaNodeType, false);
    }

    public AbstractImagePrototype getIcon(GWTJahiaNodeType gwtJahiaNodeType, boolean large) {
        if (gwtJahiaNodeType != null) {
            String icon = gwtJahiaNodeType.getIcon();
            return getIcon(icon, false, large);
        }
        return null;
    }

    public AbstractImagePrototype getIcon(final String icon, final boolean isOpened, final boolean large) {
        return new AbstractImagePrototype() {

            public String getUrl() {
                String url = icon;
                String suffix = ".png";
                if (icon != null && icon.contains(".")) {
                    url = icon.substring(0, icon.indexOf("."));
                    suffix = icon.substring(icon.indexOf("."));
                }
                return  url +
                        /*( isOpened ? "_opened" : "" ) +*/
                        ( large ? "_large" : "" ) +
                        suffix;
            }

            @Override
            public void applyTo(Image image) {
                image.setUrl(getUrl());
            }

            @Override
            public void applyTo(ImagePrototypeElement imageElement) {

            }

            @Override
            public ImagePrototypeElement createElement() {
                Element tmp = Document.get().createSpanElement();
                tmp.setInnerHTML(getHTML());
                return (ImagePrototypeElement) tmp.getFirstChildElement();
            }

            @Override
            public Image createImage() {
                return new Image(getUrl());
            }

            @Override
            public String getHTML() {
                return "<img src=\""+getUrl()+"\" />";
            }
        };
    }
}