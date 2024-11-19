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
package org.jahia.services.importexport;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Locale;

/**
 * User: rincevent
 */
public class LegacyPidToVanityUrlMappingToolImpl implements LegacyPidMappingTool {
    private static Logger logger = LoggerFactory.getLogger(LegacyPidToVanityUrlMappingToolImpl.class);

    private VanityUrlManager urlMgr;

    public void setUrlMgr(VanityUrlManager urlMgr) {
        this.urlMgr = urlMgr;
    }

    public void defineLegacyMapping(int oldPid, JCRNodeWrapper newPageNode, Locale locale) {
        try {
            String url = "/lang/" + locale.toString() + "/pid/" + oldPid;
            String site = newPageNode.getResolveSite().getSiteKey();
            if(urlMgr.findExistingVanityUrls(url,site,newPageNode.getSession()).isEmpty()) {
                VanityUrl vanityUrl = new VanityUrl(url, site,locale.toString(),false,true);
                urlMgr.saveVanityUrlMapping(newPageNode,vanityUrl,newPageNode.getSession());
            }
        } catch (RepositoryException e) {
            logger.error("Issue while creating legacy pid mapping for page "+oldPid,e);
        }
    }
}
