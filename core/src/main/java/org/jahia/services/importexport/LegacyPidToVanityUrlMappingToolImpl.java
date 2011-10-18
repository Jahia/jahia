package org.jahia.services.importexport;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.seo.VanityUrl;
import org.jahia.services.seo.jcr.VanityUrlManager;
import org.jahia.services.seo.jcr.VanityUrlService;
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
