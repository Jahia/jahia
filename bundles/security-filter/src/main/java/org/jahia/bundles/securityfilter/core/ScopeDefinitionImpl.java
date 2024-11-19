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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.securityfilter.core;

import org.jahia.bundles.securityfilter.core.apply.AutoApply;
import org.jahia.bundles.securityfilter.core.constraint.Constraint;
import org.jahia.bundles.securityfilter.core.grant.Grant;
import org.jahia.services.securityfilter.ScopeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;

public class ScopeDefinitionImpl implements ScopeDefinition {
    private static final Logger logger = LoggerFactory.getLogger(ScopeDefinitionImpl.class);

    private String pid;
    private String scopeName;
    private String description;
    private Collection<AutoApply> apply;
    private Collection<Constraint> constraints;
    private Collection<Grant> grants;
    private Map<String, String> metadata;

    public ScopeDefinitionImpl(String pid, String scopeName, String description, Collection<AutoApply> apply, Collection<Constraint> constraints, Collection<Grant> grants, Map<String, String> metadata) {
        this.pid = pid;
        this.scopeName = scopeName;
        this.description = description;
        this.apply = apply;
        this.constraints = constraints;
        this.grants = grants;
        this.metadata = metadata;
    }

    public String getPid() {
        return pid;
    }

    public String getScopeName() {
        return scopeName;
    }

    public String getDescription() {
        return description;
    }

    public Collection<AutoApply> getApply() {
        return apply;
    }

    public Collection<Constraint> getConstraints() {
        return constraints;
    }

    public Collection<Grant> getGrants() {
        return grants;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public boolean shouldAutoApply(HttpServletRequest request) {
        for (AutoApply autoApply : apply) {
            if (autoApply.shouldApply(request)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValid(HttpServletRequest request) {
        for (Constraint constraint : constraints) {
            if (!constraint.isValid(request)) {
                return false;
            }
        }

        return true;
    }

    public boolean isGrantAccess(Map<String, Object> query) {
        for (Grant grant : grants) {
            if (grant.matches(query)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Grant {}: GRANTED", grant);
                }
                return true;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Grant {}: DENIED", grant);
            }
        }
        return false;
    }

}
