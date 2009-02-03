/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.template.category;

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.categories.Category;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.comparator.NumericStringComparator;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import java.util.*;

/**
 * @author Xavier Lawrence
 */
public class GetCategoryChildrenTag extends AbstractJahiaTag {

    private static transient final Logger logger = Logger.getLogger(GetCategoryChildrenTag.class);

    private static final String RADIO = "radio";
    private static final String CHECKBOX = "checkbox";
    private static final String SELECT_BOX_SINGLE = "selectBoxSingle";
    private static final String SELECT_BOX_MULTIPLE = "selectBoxMultiple";

    private String display;
    private int level;
    private String startingCategoryKey;
    private String name;
    private String selected;
    private boolean displayRootCategory = true;
    private String messageKey;

    public void setDisplay(String display) {
        this.display = display;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setStartingCategoryKey(String startingCategoryKey) {
        this.startingCategoryKey = startingCategoryKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public void setDisplayRootCategory(boolean displayRootCategory) {
        this.displayRootCategory = displayRootCategory;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public int doStartTag() {
        try {
            if (startingCategoryKey.length() == 0) {
                throw new JspTagException("startingCategoryKey attribute cannot be empty");
            }

            final ServletRequest request = pageContext.getRequest();
            final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final ProcessingContext jParams = jData.getProcessingContext();
            final Category startingCategory = Category.getCategory(startingCategoryKey, jParams.getUser());

            if (startingCategory == null) {
                logger.warn("getCategoryChildrenTag: startingCategory is null");
                return SKIP_BODY;
            }
            final List children = startingCategory.getChildCategories(jParams.getUser());
            final JspWriter out = pageContext.getOut();
            final Locale locale = jParams.getCurrentLocale();
            if (children != null && children.size() > 0) {
                final TreeSet<Category> orderedCategories = new TreeSet<Category>(new NumericStringComparator());
                getCategoryChildren(startingCategory, 0, level, orderedCategories, jParams);

                final StringBuffer buff = new StringBuffer();
                if (cssClassName != null && cssClassName.length() > 0) {
                    buff.append("<div class='");
                    buff.append(cssClassName);
                    buff.append("'>");
                }

                if (SELECT_BOX_SINGLE.equals(display) || display == null || display.length() == 0) {
                    buff.append("<select name='");
                    buff.append(name);
                    buff.append("'>\n");
                    buff.append(printSelectSingleCategories(orderedCategories, selected, locale));
                    buff.append("</select>");

                } else if (RADIO.equals(display)) {
                    buff.append(printRadioCategories(orderedCategories, name, selected, locale));

                } else if (CHECKBOX.equals(display)) {
                    final StringBuffer selected = new StringBuffer();
                    buff.append(printCheckboxCategories(orderedCategories, selected, jParams, locale));
                    request.setAttribute(name, selected.toString());

                } else if (SELECT_BOX_MULTIPLE.equals(display)) {
                    buff.append("<select multiple='multiple' name='");
                    buff.append(name);
                    buff.append("'>\n");

                    final String[] params = jParams.getParameterValues(name);
                    final List<String> paramList = new ArrayList<String>();
                    if (params != null) {
                        paramList.addAll(Arrays.asList(params));
                    }

                    buff.append(printSelectMultipleCategories(orderedCategories, paramList, locale));
                    buff.append("</select>");
                }

                if (cssClassName != null && cssClassName.length() > 0) {
                    buff.append("</div>");
                }

                out.print(buff.toString());

            } else {
                if (messageKey != null && messageKey.length() > 0) {
                    out.println(getI18nMessage(messageKey));
                } else {
                    out.println("N/A");
                }
            }

        } catch (Exception e) {
            logger.error("Error in getCategoryChildrenTag", e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        level = -1;
        display = null;
        startingCategoryKey = null;
        displayRootCategory = true;
        name = null;
        selected = null;
        cssClassName = null;
        messageKey = null;
        return EVAL_PAGE;
    }

    protected void getCategoryChildren(final Category cat, final int currentLevel, final int maxLevel, Set<Category> result,
                                       ProcessingContext jParams) throws JahiaException {
        if (currentLevel == 0 && displayRootCategory) {
            result.add(cat);
        } else if (currentLevel > 0) {
            result.add(cat);
        }
        if (currentLevel < maxLevel) {
            final List children = cat.getChildCategories(jParams.getUser());
            if (children != null && children.size() > 0) {
                for (Object aChildren : children) {
                    getCategoryChildren((Category) aChildren, currentLevel + 1, maxLevel, result, jParams);
                }
            }
        }
    }

    protected String printRadioCategories(final TreeSet<Category> orderedCategories, final String name,
                                          final String selected, final Locale languageCode) {
        final StringBuffer buff = new StringBuffer();
        for (Category ordererCategory : orderedCategories) {
            final String categoryKey = ordererCategory.getKey();
            buff.append("<input type='radio' name='");
            buff.append(name);
            buff.append("' ");
            buff.append("value='");
            buff.append(categoryKey);
            buff.append("' ");
            buff.append("id='");
            buff.append(categoryKey);
            buff.append("' ");
            if (categoryKey.equals(selected)) {
                buff.append("checked=\"checked\"");
            }
            buff.append("/>\n");

            buff.append(printLabel(ordererCategory, languageCode));
        }
        return buff.toString();
    }

    protected String printCheckboxCategories(final TreeSet<Category> orderedCategories,
                                             final StringBuffer selected,
                                             final ProcessingContext jParams,
                                             final Locale languageCode) {
        final StringBuffer buff = new StringBuffer();
        for (Category ordererCategory : orderedCategories) {
            final String categoryKey = ordererCategory.getKey();
            buff.append("<input type='checkbox' name='");
            buff.append(categoryKey);
            buff.append("' ");
            buff.append("value='");
            buff.append(categoryKey);
            buff.append("' ");
            buff.append("id='");
            buff.append(categoryKey);
            buff.append("' ");

            if (jParams.getParameter(categoryKey) != null) {
                buff.append("checked=\"checked\"");
                if (selected.length() > 0) selected.append(",");
                selected.append(categoryKey);
            }
            buff.append("/>\n");

            buff.append(printLabel(ordererCategory, languageCode));
        }
        return buff.toString();
    }

    protected String printSelectSingleCategories(final TreeSet<Category> orderedCategories,
                                                 final String selected, final Locale languageCode) {
        final StringBuffer buff = new StringBuffer();
        for (Category ordererCategory : orderedCategories) {
            final String categoryKey = ordererCategory.getKey();
            buff.append("<option value='");
            buff.append(categoryKey);
            buff.append("' ");
            if (categoryKey.equals(selected)) {
                buff.append("selected=\"selected\"");
            }
            buff.append(">");
            final String title = ordererCategory.getTitle(languageCode);
            if (title != null && title.length() > 0) {
                buff.append(title);
            } else {
                buff.append(categoryKey);
            }
            buff.append("</option>\n");
        }
        return buff.toString();
    }

    protected String printSelectMultipleCategories(final TreeSet<Category> orderedCategories, final List selected,
                                                   final Locale languageCode) {
        final StringBuffer buff = new StringBuffer();
        for (Category ordererCategory : orderedCategories) {
            final String categoryKey = ordererCategory.getKey();
            buff.append("<option value='");
            buff.append(categoryKey);
            buff.append("' ");
            if (selected != null && selected.contains(categoryKey)) {
                buff.append("selected=\"selected\"");
            }
            buff.append(">");
            final String title = ordererCategory.getTitle(languageCode);
            if (title != null && title.length() > 0) {
                buff.append(title);
            } else {
                buff.append(categoryKey);
            }
            buff.append("</option>\n");
        }
        return buff.toString();
    }

    protected String printLabel(final Category category, final Locale languageCode) {
        final StringBuffer buff = new StringBuffer();
        final String categoryKey = category.getKey();
        buff.append("<label for=\"");
        buff.append(categoryKey);
        buff.append("\">");
        final String title = category.getTitle(languageCode);
        if (title != null && title.length() > 0) {
            buff.append(title);
        } else {
            buff.append(categoryKey);
        }
        buff.append("</label>\n");

        return buff.toString();
    }
}
