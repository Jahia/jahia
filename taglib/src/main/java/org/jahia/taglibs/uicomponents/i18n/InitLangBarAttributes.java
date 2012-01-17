/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.taglibs.uicomponents.i18n;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.comparator.LanguageCodesComparator;

import javax.jcr.Node;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import java.util.*;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class InitLangBarAttributes extends AbstractJahiaTag {

    public static final String CURRENT_LANGUAGES_CODES = "languageCodes";


    public static final String GO_TO_HOME_PAGE = "goToHomePage";
    // order attribute authorized values 
    public static final String JAHIA_ADMIN_RANKING = "<jahia_admin_ranking>";

    private static final LanguageCodesComparator languageCodesComparator = new LanguageCodesComparator();
    private static final transient org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(
            InitLangBarAttributes.class);


    // Default Jahia CSS class name when mode "goToHomePage" is activated
    public static final String REDIRECT_DEFAULT_STYLE = "redirectJahiaStyle";

    // onLanguageSwitch attribute authorized values
    public static final String STAY_ON_CURRENT_PAGE = "stayOnCurrentPage";
    
    private boolean activeLanguagesOnly;
    
    private String order;

    public int doEndTag() throws JspException {
        final ServletRequest request = pageContext.getRequest();


        final RenderContext renderContext = (RenderContext) request.getAttribute("renderContext");
        final JCRSiteNode currentSite = renderContext.getSite();
        final Set<String> languageSettings = activeLanguagesOnly ? currentSite.getActiveLanguages() : currentSite.getLanguages();

        if (languageSettings.size() < 2) {
            return EVAL_PAGE;
        }
        final boolean mixLanguageActive = currentSite.isMixLanguagesActive();
        final List<String> currentCodes = new ArrayList<String>(languageSettings.size());
        if (order == null || order.length() == 0 || JAHIA_ADMIN_RANKING.equals(order)) {
            final TreeSet<String> orderedLangs = new TreeSet<String>();
            orderedLangs.addAll(languageSettings);
            for (final String settings : orderedLangs) {

                if (mixLanguageActive || isCurrentLangAllowed(renderContext, settings)) {
                    currentCodes.add(settings);
                }
            }

        } else {
            final List<String> languageCodes = toListOfTokens(order, ",");
            languageCodesComparator.setPattern(languageCodes);
            final TreeSet<String> orderedLangs = new TreeSet<String>(languageCodesComparator);
            final Set<String> codes = new HashSet<String>(languageSettings.size());
            for (final String lang : languageSettings) {
                // Only add the language in Live/Preview mode if the current page has an active verison in live mode for that language
                if (mixLanguageActive || isCurrentLangAllowed(renderContext, lang)) {
                    codes.add(lang);
                }
            }
            orderedLangs.addAll(codes);
            for (String code : orderedLangs) {
                currentCodes.add(code);
            }
        }

        request.setAttribute(CURRENT_LANGUAGES_CODES, currentCodes);
        resetState();
        return EVAL_PAGE;
    }


    /**
     * Return true if the current node is published in the specified languageCode
     *
     * @param jData
     * @param languageCode
     * @return
     */
    private boolean isCurrentLangAllowed(RenderContext renderContext, String languageCode) {
        if (renderContext.isLiveMode()) {
            JCRNodeWrapper node = renderContext.getMainResource().getNode();
            if (node != null) {
                try {
                    final Node localizedNode = node.getI18N(LanguageCodeConverters.languageCodeToLocale(languageCode));
                    return localizedNode.getProperty("jcr:language").getString().equals(languageCode);
                } catch (javax.jcr.RepositoryException e) {
                    logger.debug("lang[" + languageCode + "] not published");
                    return false;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return false;
                }
            } else {
                return false;
            }
        }
        // not in live or preview mode
        return true;
    }

    @Override
    protected void resetState() {
        order = null;
        activeLanguagesOnly = false;
        super.resetState();
    }

    public void setActiveLanguagesOnly(boolean activeLanguagesOnly) {
        this.activeLanguagesOnly = activeLanguagesOnly;
    }
    
    public void setOrder(String order) {
        this.order = order;
    }


    protected List<String> toListOfTokens(final String value, final String separator) {
        final StringTokenizer tokenizer = new StringTokenizer(value, separator);
        final List<String> result = new ArrayList<String>(tokenizer.countTokens());
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken().trim();
            result.add(token);
        }
        return result;
    }
}
