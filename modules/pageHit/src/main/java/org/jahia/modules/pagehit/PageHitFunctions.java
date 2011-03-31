package org.jahia.modules.pagehit;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;

/**
 * User: Dorth
 */
public class PageHitFunctions {

    public static long getNumberOFHits(JCRNodeWrapper node){
        try{
            PageHitService pageHitService = (PageHitService) SpringContextSingleton.getInstance().getModuleContext().getBean("pageHitService");
            return pageHitService.getNumberOfHits(node);
        } catch (Exception e){
            return 0;
        }
    }
}
