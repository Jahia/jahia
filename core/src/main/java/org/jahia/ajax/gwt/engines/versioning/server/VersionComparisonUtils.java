/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.engines.versioning.server;

import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.textdiff.HunkTextDiffVisitor;

import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 6 mai 2008
 * Time: 09:28:47
 * To change this template use File | Settings | File Templates.
 */
public class VersionComparisonUtils {

    /**
     * Returns a title assert in the form of :
     * "Difference between {version1} and {version2} of {content}"
     *
     * @param version1Assert
     * @param version2Assert
     * @param contentAssert
     * @param context
     * @param locale
     * @return
     */
    public static String getTitleAssert(String version1Assert, String version2Assert, String contentAssert,
                                        ProcessingContext context, Locale locale){
        String titleAssert = JahiaResourceBundle.getJahiaInternalResource(
                "org.jahia.engines.version.versioningComparisonTitleAssert",
                locale,"Difference between {version1} and {version2} of {content}");
        titleAssert = JahiaTools.replacePattern(titleAssert,"{version1}",version1Assert);
        titleAssert = JahiaTools.replacePattern(titleAssert,"{version2}",version2Assert);
        titleAssert = JahiaTools.replacePattern(titleAssert,"{content}",contentAssert);
        return titleAssert;
    }

    public static String getAddedDiffLegendAssert(ProcessingContext context, Locale locale){
        String addedDiffLegend = HunkTextDiffVisitor.getAddedText(JahiaResourceBundle
            .getJahiaInternalResource("org.jahia.engines.version.added",  locale,"added"));
        return addedDiffLegend;
    }

    public static String getRemovedDiffLegendAssert(ProcessingContext context, Locale locale){
        String addedDiffLegend = HunkTextDiffVisitor.getDeletedText(JahiaResourceBundle
            .getJahiaInternalResource("org.jahia.engines.version.deleted",  locale,"deleted"));
        return addedDiffLegend;
    }

    public static String getChangedDiffLegendAssert(ProcessingContext context, Locale locale){
        String addedDiffLegend = HunkTextDiffVisitor.getChangedText(JahiaResourceBundle
            .getJahiaInternalResource("org.jahia.engines.version.changed",  locale,"changed"));
        return addedDiffLegend;
    }
    
}
