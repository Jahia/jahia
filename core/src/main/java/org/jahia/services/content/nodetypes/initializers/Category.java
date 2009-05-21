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
package org.jahia.services.content.nodetypes.initializers;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.CategoryService;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.PropertyType;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 19 févr. 2009
 * Time: 18:05:33
 * To change this template use File | Settings | File Templates.
 */
public class Category implements ValueInitializer {
    public Value[] getValues(ProcessingContext jParams, ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params) {
        if (jParams != null) {
            CategoryService categoryService = ServicesRegistry.getInstance().getCategoryService();
            try {
                final org.jahia.services.categories.Category category = categoryService.getCategory(params.get(0));
                final List<org.jahia.services.categories.Category> childCategories = category.getChildCategories();
                SortedSet<Value> values = new TreeSet<Value>(new Comparator<Value>() {
                    public int compare(Value o, Value o1) {
                        try {
                            return o.getString().compareTo(o1.getString());
                        } catch (RepositoryException e) {
                            return -1;
                        }
                    }
                });
                for (org.jahia.services.categories.Category childCategory : childCategories) {
                    values.add(new ValueImpl(childCategory.getTitle(jParams.getCurrentLocale()), PropertyType.STRING,false));
                }
                return values.toArray(new Value[values.size()]);
            } catch (JahiaException e) {
                e.printStackTrace();
            }
        }
        return new Value[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
