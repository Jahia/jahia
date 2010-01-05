/**
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
 */

package org.jahia.services.render.filter;

import java.util.List;

import org.jahia.services.templates.JahiaTemplateManagerService;

/**
 * Spring object, used to register a list of {@link RenderFilter} instances for
 * a template module. It will be automatically detected by the
 * {@link JahiaTemplateManagerService} and added into the registry.
 * 
 * @author Sergiy Shyrkov
 */
public class ModuleFilters {

    private List<RenderFilter> filters;

    private String module;

    /**
     * Returns a list of configured filters.
     * 
     * @return list of configured filters
     */
    public List<RenderFilter> getFilters() {
        return filters;
    }

    /**
     * Returns the name of the module, these filters will be applied to. If not
     * specified, filters will be applied to all modules, depending on the
     * filter conditions (a filter can be limited to a particular module also).
     * 
     * @return the name of the module, these filters will be applied to
     */
    public String getModule() {
        return module;
    }

    /**
     * Set the list of filters.
     * 
     * @param filters
     *            the list of filters
     */
    public void setFilters(List<RenderFilter> filters) {
        this.filters = filters;
    }

    /**
     * Sets the name of the module, these filters will be applied to. If not
     * specified, filters will be applied to all modules, depending on the
     * filter conditions (a filter can be limited to a particular module also)
     * 
     * @param module
     *            the name of the module, these filters will be applied to
     */
    public void setModule(String module) {
        this.module = module;
    }

}
