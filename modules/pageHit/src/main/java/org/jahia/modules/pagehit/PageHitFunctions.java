package org.jahia.modules.pagehit;

import org.apache.log4j.Logger;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;

/**
 * Created by IntelliJ IDEA.
 * User: Dorth
 * Date: 26 août 2010
 * Time: 17:32:58
 * To change this template use File | Settings | File Templates.
 */
public class PageHitFunctions {
    private static transient Logger logger = Logger.getLogger(PageHitFunctions.class);

    public static long getNumberOFHits(JCRNodeWrapper node){
        try{
            PageHitService pageHitService = (PageHitService) SpringContextSingleton.getInstance().getModuleContext().getBean("pageHitService");
            return pageHitService.getNumberOfHits(node);
        } catch (Exception e){
            return 0;
        }
    }
}
