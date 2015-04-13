package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.widget.form.CheckBox;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;

/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 * <p/>
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 * <p/>
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 * <p/>
 * 1/ GPL
 * ======================================================================================
 * <p/>
 * IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p/>
 * "This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * <p/>
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, also available here:
 * http://www.jahia.com/license"
 * <p/>
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ======================================================================================
 * <p/>
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p/>
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 * <p/>
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 * <p/>
 * <p/>
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 * <p/>
 * Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 * streamlining Enterprise digital projects across channels to truly control
 * time-to-market and TCO, project after project.
 * Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 * marketing teams to collaboratively and iteratively build cutting-edge
 * online business solutions.
 * These, in turn, are securely and easily deployed as modules and apps,
 * reusable across any digital projects, thanks to the Jahia Private App Store Software.
 * Each solution provided by Jahia stems from this overarching vision:
 * Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 * Founded in 2002 and headquartered in Geneva, Switzerland,
 * Jahia Solutions Group has its North American headquarters in Washington DC,
 * with offices in Chicago, Toronto and throughout Europe.
 * Jahia counts hundreds of global brands and governmental organizations
 * among its loyal customers, in more than 20 countries across the globe.
 * <p/>
 * For more information, please visit http://www.jahia.com
 */

/**
 * Checkbox used for work in progress button item.
 */

public class CheckboxWorkInProgress extends CheckBox {

    /**
     * refresh the value of the button according to the engine value
     * @param engine
     */
    public void refresh(AbstractContentEngine engine, GWTJahiaLanguage language) {
        setValue(engine.isWorkInProgress(language.getLanguage()));
    }
}