/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
