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
