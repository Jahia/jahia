/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
