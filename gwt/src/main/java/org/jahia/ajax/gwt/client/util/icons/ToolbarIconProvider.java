/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.ajax.gwt.client.util.icons;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

/**
 * Icon provider for for different types of content objects.
 * User: ktlili
 * Date: Jul 15, 2009
 * Time: 10:50:04 AM
 */
public class ToolbarIconProvider {

    private static ToolbarIconProvider iconProvider = new ToolbarIconProvider();

    private ToolbarIconProvider() {
        super();
    }


    public static ToolbarIconProvider getInstance() {
        if (iconProvider == null) {
            iconProvider = new ToolbarIconProvider();
        }
        return iconProvider;
    }

    public AbstractImagePrototype getIcon(final String icon) {
        return new AbstractImagePrototype() {

            public String getUrl() {
                return JahiaGWTParameters.getContextPath() + (icon.startsWith("/") ? icon : ("/icons/" + icon)) + (icon.contains(".") ? "" : ".png");
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
                return "<img src=\""+getUrl()+"\" width=\"16\" height=\"16\" alt=\" \"/>";
            }
        };
    }
}