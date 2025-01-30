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
 package org.jahia.services.importexport;

import org.slf4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.categories.CategoryService;
import org.jahia.utils.LanguageCodeConverters;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Import handler for categories.
 * User: toto
 * Date: 5 juin 2006
 * Time: 17:57:47
 * @deprecated Legacy import code
 */
@Deprecated(since = "8.2.1.0", forRemoval = true)
public class CategoriesImportHandler extends DefaultHandler {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(CategoriesImportHandler.class);
    private Stack<Category> cats = new Stack<Category>();
    private CategoryService cs ;
    private List<String[]> uuidProps = new ArrayList<String[]>();
    private Category rootCategory = null;

    public CategoriesImportHandler() {
        cs = ServicesRegistry.getInstance().getCategoryService();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            if (localName.equals("category")) {
                Category c;
                String key = attributes.getValue(ImportExportBaseService.JAHIA_URI, "key");
                if (cats.isEmpty()) {
                    cats.push(rootCategory);
                    return;
                } else {
                    String parentPath;
                    Category parent = cats.peek();
                    if (parent == null) {
                        try {
                            parentPath = cs.getCategoriesRoot().getPath();
                        } catch (RepositoryException e) {
                            throw new SAXException(e);
                        }
                    } else {
                        parentPath = parent.getCategoryPath();
                    }

                    Category cat = cs.getCategoryByPath(parentPath + "/"  + key);

                    if (cat == null) {
                        c = Category.createCategory(key,parent);
                    } else {
                        c = cat;
                    }
                }
                for (int i = 0; i < attributes.getLength(); i++) {
                    String k = (String) attributes.getLocalName(i);
                    if (k.startsWith("title_")) {
                        c.setTitle(LanguageCodeConverters.languageCodeToLocale(k.substring(6)), attributes.getValue(i));
                    }
                }
                String acl = attributes.getValue(ImportExportBaseService.JAHIA_URI, "acl");
                if (acl != null) {
//                    TODO: commented while shifting from DB to JCR, needs to be finished
//                    fillAcl(c.getACL(), acl);
                }
                cats.push(c);
            }
            if (localName.equals("property")) {
                String key = attributes.getValue(ImportExportBaseService.JAHIA_URI, "key");
                String value = attributes.getValue(ImportExportBaseService.JAHIA_URI, "value");
                if (!cats.empty()) {
                    Category c = cats.peek();
                    if (key != null && value != null) {
                        if (key.startsWith("homepage")) {
                            uuidProps.add(new String[] {c.getKey(),key,value});
                        }
                        c.setProperty(key, value);
                    }
                }
            }
        } catch (JahiaException e) {
            throw new SAXException(e);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("category")) {
            cats.pop();
        }
    }

    public List<String[]> getUuidProps() {
        return uuidProps;
    }

    public void setUuidProps(List<String[]> p) {
        if (p == null) {
            return;
        }
        for (Iterator<String[]> iterator = p.iterator(); iterator.hasNext();) {
            try {
                String[] s = (String[]) iterator.next();
                Category c = Category.getCategory(s[0]);
            } catch (JahiaException e) {
                logger.error("Cannot set property for category ",e);
            }
        }
    }

    public void setRootCategory(Category cat) {
        rootCategory = cat;
    }
}
