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

package org.jahia.services.importexport;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 11/10/11
 * Time: 12:08 PM
 * To change this template use File | Settings | File Templates.
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
