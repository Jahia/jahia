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

package org.jahia.services.search;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jahia.services.render.RenderContext;

/**
 * Abstract search result item, used as a view object in JSP templates.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class AbstractHit<T> implements Hit<T> {

    private String excerpt;
    protected T resource;
    private float score;
    protected RenderContext context;
    private String queryParameter = "";

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

}
