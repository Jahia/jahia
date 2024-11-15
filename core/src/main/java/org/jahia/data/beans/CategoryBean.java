/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.data.beans;

import org.jahia.content.JahiaObject;
import org.jahia.services.categories.Category;
import org.jahia.services.content.JCRSessionFactory;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * <p>Title: A wrapper JavaBean compliant class that uses the current
 * request context to display values back to the output. </p>
 * <p>Description: This class encapsulates a ProcessingContext and a Category class
 * to provide helper methods for template developers to access using Struts
 * or JSTL accessors to beans.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class CategoryBean extends AbstractJahiaObjectBean {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CategoryBean.class);

    private Category category;

    static {
        registerType(Category.class.getName(), CategoryBean.class.getName());
    }

    /**
     * Empty constructor to follow JavaBean compliance rules
     */
    public CategoryBean() {
    }

    /**
     * Constructor for wrapper
     *
     * @param category the category to wrap
     */
    public CategoryBean(Category category) {
        this.category = category;
    }

    /**
     * Static instantiator called from the getInstance() method of the
     * AbstractJahiaObjectBean class.
     *
     * @param jahiaObject       the JahiaObject instance to build this wrapper for
     * @return an instance of an AbstractJahiaObjectBean descendant corresponding
     *         to the JahiaObject type and request context
     */
    public static AbstractJahiaObjectBean getChildInstance(JahiaObject jahiaObject) {
        return new CategoryBean((Category) jahiaObject);
    }

    /**
     * @return the enclosed Category instance
     */
    public Category getCategory() {
        return category;
    }

    /**
     * @return the title of the category for the current locale accessed through
     *         the Constants.getLocale() method.
     */
    public String getTitle() {
        final String title = category.getTitle(JCRSessionFactory.getInstance().getCurrentLocale());
        if (title != null) {
            return title;
        } else {
            return category.getKey();
        }
    }

    /**
     * @return the full set of properties
     */
    public Properties getProperties() {
        return category.getProperties();
    }

    public static Set<CategoryBean> getCategoryBeans(final Set<Category> categories) {
        final Set<CategoryBean> result = new HashSet<CategoryBean>();
        for (Category cat : categories) {
            result.add(new CategoryBean(cat));
        }
        return result;
    }

    /**
     * Returns the category key.
     *
     * @return the category key
     */
    public String getKey() {
        return category.getKey();
    }

}
