/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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

    protected T resource;
    private float score;
    protected RenderContext context;
    private String queryParameter = "";
    private String linkTemplateType = "html";

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

    public T getRawHit() {
        return resource;
    }

    public float getScore() {
        return score;
    }

    public RenderContext getContext() {
        return context;
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

}
