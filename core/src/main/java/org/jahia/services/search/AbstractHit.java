/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.search;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.jcr.JahiaExcerptProvider;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Value;
import javax.jcr.query.Row;
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
    private String linkTemplateType = "html";
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
                for (Row row : rows) {
                    Value excerptValue = row.getValue("rep:excerpt(.)");
                    if (excerptValue != null) {
                        if (excerptValue.getString().contains(
                                "###" + JahiaExcerptProvider.TAG_TYPE + "#")
                                || excerptValue.getString().contains(
                                "###" + JahiaExcerptProvider.CATEGORY_TYPE
                                        + "#")) {
                            StringBuilder r = new StringBuilder();
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
                                }
                                if (!type.equals(s2)) {
                                    r.append(s2).append(":");
                                    type = s2;
                                    separator = "";
                                }
                                r.append(separator).append(v);
                                separator = ", ";

                            }
                            setExcerpt(r.toString());
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

    public String getLinkTemplateType() {
        return linkTemplateType;
    }

    public void setLinkTemplateType(String linkTemplateType) {
        this.linkTemplateType = linkTemplateType;
    }

    public void addRow(Row row) {
        if (this.rows == null) {
            this.rows = new ArrayList<Row>();
        }
        rows.add(row);
    }

    /**
     * Returns the row objects from the query/search linked to this hit. Multiple query results (row) can be linked to a
     * hit, because some nodes cannot be displayed on its own as they have no template, so the hit's link URL points to a
     * parent node having a template, which can aggregate several sub-nodes.
     *
     * @return list of Row objects
     */
    public List<Row> getRows() {
        return rows;
    }

    /**
     * Returns the list of hits that use the current hit
     * @return list of AbstractHit objects
     */
    public abstract List<AbstractHit<?>> getUsages();
}
