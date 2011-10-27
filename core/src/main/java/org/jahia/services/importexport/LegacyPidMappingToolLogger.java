package org.jahia.services.importexport;

import org.jahia.services.content.JCRNodeWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 11/10/11
 * Time: 10:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class LegacyPidMappingToolLogger implements LegacyPidMappingTool {
    private static Logger logger = LoggerFactory.getLogger(LegacyPidMappingToolLogger.class);
    public void defineLegacyMapping(int oldPid, JCRNodeWrapper newPageNode, Locale locale) {
        try {
            logger.info("Mapping pid {} to uuid {}, page accessible through URL {}",new Object[]{oldPid,newPageNode.getIdentifier(),newPageNode.getUrl()});
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
