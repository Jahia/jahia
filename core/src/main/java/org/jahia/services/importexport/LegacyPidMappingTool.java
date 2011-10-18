package org.jahia.services.importexport;

import org.jahia.services.content.JCRNodeWrapper;

import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 11/10/11
 * Time: 9:57 AM
 * To change this template use File | Settings | File Templates.
 */
public interface LegacyPidMappingTool {
    /**
     * Defines a new page IDs mapping
     *
     * @param oldPid  The former pageID of the page, before the migration
     * @param newPageNode  The node object of the newly created page
     * @param locale
     */
    public void defineLegacyMapping(int oldPid, JCRNodeWrapper newPageNode, Locale locale);
}

