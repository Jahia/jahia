/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

 package org.jahia.services.importexport;

import org.slf4j.Logger;
import org.jahia.api.Constants;
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
 */
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
