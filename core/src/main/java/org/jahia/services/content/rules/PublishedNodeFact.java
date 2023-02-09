/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.rules;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jahia.services.content.JCRNodeWrapper;

public class PublishedNodeFact extends AbstractNodeFact {
    boolean unpublished;
    String language;

    public PublishedNodeFact(JCRNodeWrapper node, String language, boolean unpublished) throws RepositoryException {
        super(node);
        this.language = language;
        this.unpublished = unpublished;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    public String toString() {
        return (isUnpublished() ? "un" : "") + "published " + node.getPath() + (isTranslation() ? " in `" + getLanguage() + "`" : "");
    }

    public boolean isTranslation() {
        return language != null;
    }

    public boolean isUnpublished() {
        return unpublished;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        PublishedNodeFact that = (PublishedNodeFact)o;
        return new EqualsBuilder().appendSuper(super.equals(o)).append(this.getLanguage(), that.getLanguage())
                .append(this.isUnpublished(), that.isUnpublished()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(node).append(workspace).append(
                operationType).append(language).append(unpublished).toHashCode();
    }
}
