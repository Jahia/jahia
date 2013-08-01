/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.search;

import javax.jcr.Value;
import javax.jcr.query.Row;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.jcr.JahiaExcerptProvider;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract search result item, used as a view object in JSP templates.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class AbstractHit<T> implements Hit<T> {

    private static Logger logger = LoggerFactory.getLogger(AbstractHit.class);    
    
    private String excerpt;
    protected T resource;
    private float score;
    protected RenderContext context;
    private String queryParameter = "";
    private List<Row> rows = null;

    /**
     * Initializes an instance of this class.
     * 
     * @param resource search result item to be wrapped
     * @param context
     */
    public AbstractHit(T resource, RenderContext context) {
        super();
        this.resource = resource;
        this.context = context;
    }

    public String getExcerpt() {
        if (excerpt == null && rows != null) {
            try {
                // this is Jackrabbit specific, so if other implementations
                // throw exceptions, we have to do a check here
<<<<<<< .working
                Value excerptValue = row.getValue("rep:excerpt(.)");
                if (excerptValue != null) {
                    if (excerptValue.getString().contains(
                            "###" + JahiaExcerptProvider.TAG_TYPE + "#")
                            || excerptValue.getString().contains(
                                    "###" + JahiaExcerptProvider.CATEGORY_TYPE
                                            + "#")) {
                        String r = "";
                        String separator = "";
                        String type = "";
                        for (String s : Patterns.COMMA.split(excerptValue
                                .getString())) {
                            String s2 = Messages.getInternal(s
                                    .contains(JahiaExcerptProvider.TAG_TYPE) ? "label.tags"
                                    : "label.category", context.getRequest().getLocale());
                            String s1 = s.substring(s.indexOf("###"),
                                    s.lastIndexOf("###"));
                            String identifier = s1.substring(s1
                                    .lastIndexOf("#") + 1);
                            String v = "";
                            if (identifier.startsWith("<span")) {
                                identifier = identifier.substring(
                                        identifier.indexOf(">") + 1,
                                        identifier.lastIndexOf("</span>"));
                                v = "<span class=\" searchHighlightedText\">"
                                        + getTitle() + "</span>";
                            } else {
                                v = getTitle();
=======
                for (Row row : rows) {
                    Value excerptValue = row.getValue("rep:excerpt(.)");
                    if (excerptValue != null) {
                        if (excerptValue.getString().contains(
                                "###" + JahiaExcerptProvider.TAG_TYPE + "#")
                                || excerptValue.getString().contains(
                                "###" + JahiaExcerptProvider.CATEGORY_TYPE
                                        + "#")) {
                            String r = "";
                            String separator = "";
                            String type = "";
                            for (String s : Patterns.COMMA.split(excerptValue
                                    .getString())) {
                                String s2 = s
                                        .contains(JahiaExcerptProvider.TAG_TYPE) ? JahiaResourceBundle
                                        .getJahiaInternalResource("label.tags",
                                                context.getRequest().getLocale())
                                        : JahiaResourceBundle
                                        .getJahiaInternalResource(
                                                "label.category", context
                                                .getRequest()
                                                .getLocale());
                                String s1 = s.substring(s.indexOf("###"),
                                        s.lastIndexOf("###"));
                                String identifier = s1.substring(s1
                                        .lastIndexOf("#") + 1);
                                String v = "";
                                if (identifier.startsWith("<span")) {
                                    identifier = identifier.substring(
                                            identifier.indexOf(">") + 1,
                                            identifier.lastIndexOf("</span>"));
                                    v = "<span class=\" searchHighlightedText\">"
                                            + getTitle() + "</span>";
                                } else {
                                    v = getTitle();
                                }
                                if (!type.equals(s2)) {
                                    r += s2 + ":";
                                    type = s2;
                                    separator = "";
                                }
                                r += separator + v;
                                separator = ", ";

>>>>>>> .merge-right.r46894
                            }
                            setExcerpt(r);
                            break;
                        } else if (!StringUtils.isEmpty(excerptValue.getString())) {
                            setExcerpt(excerptValue.getString());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Search details cannot be retrieved", e);
            }
        }
        return excerpt;
    }

    public T getRawHit() {
        return resource;
    }

    public float getScore() {
        return score;
    }

    public RenderContext getContext() {
        return context;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("type", this.getType()).append("title",
                this.getTitle()).append("link", this.getLink()).append("score", this.getScore()).append("contentType",
                this.getContentType()).append("created", this.getCreated()).append("createdBy", this.getCreatedBy())
                .append("lastModified", this.getLastModified()).append("lastModifiedBy", this.getLastModifiedBy())
                .append("lastModifiedBy", this.getLastModifiedBy()).toString();
    }

    @Override
    public int hashCode() {
        return resource.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return resource.equals(obj);
    }

    public String getQueryParameter() {
        return queryParameter;
    }

    public void setQueryParameter(String queryParameter) {
        this.queryParameter = queryParameter;
    }

    public void addRow(Row row) {
        if (this.rows == null) {
            this.rows = new ArrayList<Row>();
        }
        rows.add(row);
    }    
}
