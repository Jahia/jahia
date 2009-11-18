/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
//
//  GuiBean
//  EV      03.11.2000
//	DJ		02.02.2001 - added ACL check for link display
//  JB      16.05.2001
//

package org.jahia.gui;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.engines.JahiaEngine;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.EnginesRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.lock.LockKey;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.JahiaTools;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Modified and cleaned by Xavier Lawrence
 */
public class GuiBean {

    private static final transient Logger logger = Logger.getLogger(GuiBean.class);

    private final ProcessingContext processingContext;
    private final HTMLToolBox html;

    protected GuiBean() {
        processingContext = null;
        html = null;
    }

    /**
     * constructor
     * EV    03.11.2000
     */
    public GuiBean(final ProcessingContext jContext) {
        this.processingContext = jContext;
        this.html = new HTMLToolBox(this, processingContext);
    } // end constructor

    public HTMLToolBox html() {
        return html;
    }

    public ProcessingContext params() {
        return processingContext;
    }

    /**
     * Build an URL containing the language code the displayed page should
     * switch to.
     *
     * @param code The iso639 language code
     * @return The URL string composed
     * @throws JahiaException
     */
    public String drawPageLanguageSwitch(final String code) throws JahiaException {
        String result = "";
        if (processingContext != null) {
            // Get the current page
            final JahiaPage currentPage = processingContext.getPage();
            if (currentPage != null) {
                result = processingContext.composeLanguageURL(code);
            }
        }
        return result;
    }

    /**
     * Build an URL containing the language code the given page should
     * switch to.
     *
     * @param code The iso639 language code
     * @return The URL string composed
     * @throws JahiaException
     */
    public String drawPageLanguageSwitch(final String code, final int pid) throws JahiaException {
        String result = "";
        if (processingContext != null) {
            result = processingContext.composeLanguageURL(code, pid);
        }
        return result;
    }

    public String drawPopupLoginUrl() throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        return drawUrl("login", buff.append("/pid/").append(processingContext.getPageID()).toString());
    }

    public String drawPopupLoginUrl(final int destinationPageID)
            throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        return drawUrl("login", buff.append("/pid/").append(destinationPageID).toString());
    }

    public String drawLoginUrl() throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        return drawUrl("login", buff.append("/pid/").append(processingContext.getPageID()).
                append("?screen=save").toString());
    }

    public String drawLoginUrl(final int destinationPageID)
            throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        return drawUrl("login", buff.append("/pid/").append(destinationPageID).
                append("?screen=save").toString());
    }
    // end drawLoginUrl

    // this draw logout is called by the other ones too

    public String drawPopupLogoutUrl(final int destinationPageID)
            throws JahiaException {
        if (!processingContext.getUser().getUsername().equals("guest")) {
            return processingContext.getContextPath()+"/cms/logout";
        } else {
            return "";
        }
    }

    public String drawPopupLogoutUrl() throws JahiaException {
        return drawPopupLogoutUrl(processingContext.getPageID());
    }

    public String drawLogoutUrl(final int destinationPageID)
            throws JahiaException {
        return drawPopupLogoutUrl(destinationPageID);
    }

    public String drawLogoutUrl() throws JahiaException {
        return drawPopupLogoutUrl(processingContext.getPageID());
    }


    public String drawWorkflowUrl(final String key)
            throws JahiaException {

        return drawUrl("workflow", key);
    }

    public String drawLockUrl(final LockKey lockKey)
            throws JahiaException {
        return drawUrl("lock", lockKey);
    }

    public String drawUpdateCategoryUrl(final Category category)
            throws JahiaException {
        return drawUrl("categoryEdit", category);
    }

    public String drawAddSubCategoryUrl(final String parentCategoryKey)
            throws JahiaException {
        return drawUrl("categoryEdit", parentCategoryKey);
    }

    public String drawUpdateCategoryUrl(final Category category, final int nodeIndex)
            throws JahiaException {
        return drawUrl("categoryEdit", category);
    }

    public String drawAddSubCategoryUrl(final String parentCategoryKey, final int nodeIndex)
            throws JahiaException {
        return drawUrl("categoryEdit", parentCategoryKey);
    }


    public String drawSearchUrl() throws JahiaException {
        return drawUrl("search", null);
    }
    // end drawSearchUrl

    public String drawMySettingsUrl() throws JahiaException {
        return drawUrl("mysettings", null);
    }

    public String drawMySettingsUrl(Object theObj) throws JahiaException {
        return drawUrl("mysettings", theObj);
    }

    public String drawNewUserRegistrationUrl() throws JahiaException {
        return drawUrl("newuserregistration", null);
    }

    public String drawNewUserRegistrationUrl(Object theObj) throws JahiaException {
        return drawUrl("newuserregistration", theObj);
    }

    public String drawSiteMapUrl() throws JahiaException {
        return drawUrl("sitemap", null);
    }
    // end drawSiteMapUrl

    /**
     * drawAdministrationLauncher
     * MJ    21.03.2001
     */
    public String drawAdministrationLauncher() throws JahiaException {
        final StringBuffer url = new StringBuffer();
        url.append(processingContext.getContextPath()).append(Jahia.getInitAdminServletPath()).
                append("?do=passthru");
        return url.toString();
    }
    // end drawAdministrationLauncher


    /**
     * drawUrl
     * EV    15.12.2000
     */
    private String drawUrl(final String engineName, final Object theObj)
            throws JahiaException {
        String htmlResult = "";
        final JahiaEngine theEngine = (JahiaEngine) EnginesRegistry.getInstance().
                getEngine(engineName);
        if (theEngine.authoriseRender(processingContext)) {
            htmlResult = theEngine.renderLink(processingContext, theObj);
        }
        return htmlResult;
    }
    // end drawUrl
    /**
     * returns the current user name.
     * JB   25.04.2001
     */
    public String drawUsername() {
        return drawUsername(true);
    } // end drawUsername

    /**
     * @param allowUserAliasing if true, return the username of the aliased user when applicable ( in preview mode and
     *                          aliased user enabled )
     * @return
     */
    public String drawUsername(boolean allowUserAliasing) {
        if (processingContext != null) {
            if (!allowUserAliasing) {
                return getUser().getUsername();
            } else {
                String s = getUser().getUsername();
                return s;
            }
        } else {
            return "";
        }
    } // end drawUsername

    /**
     * tests if Jahia is in edition mode
     * JB   25.04.2001
     */
    public boolean isEditMode() {
        if (processingContext != null) {
            return processingContext.getOperationMode().equals(ProcessingContext.EDIT);
        } else {
            return false;
        }
    } // end isEditMode

    public boolean isNormalMode() {
        if (processingContext != null) {
            return processingContext.getOperationMode().equals(ProcessingContext.NORMAL);
        } else {
            return false;
        }
    }

    public boolean isCompareMode() {
        if (processingContext != null) {
            return processingContext.getOperationMode().equals(ProcessingContext.COMPARE);
        } else {
            return false;
        }
    }

    public boolean isPreviewMode() {
        if (processingContext != null) {
            return processingContext.getOperationMode().equals(ProcessingContext.PREVIEW);
        } else {
            return false;
        }
    }

    /**
     * tests if the current user is logged
     * JB   25.04.2001
     */
    public boolean isLogged() {
        if (processingContext != null) {
            final String theUserName = getUser().getUsername();
            if (!theUserName.equals("guest")) {
                return true;
            }
        }
        return false;
    } // end isLogged


    /**
     * tests if a page belongs to the current page path
     * JB   25.04.2001
     */
    public boolean isPageInPath(final int destPageID)
            throws JahiaException {
        if (processingContext != null) {

            final Iterator<ContentPage> thePath = getContentPage().getContentPagePath(
                    processingContext.getEntryLoadRequest(), processingContext.getOperationMode(), getUser());
            boolean foundTarget = false;
            while (thePath.hasNext()) {
                final ContentPage aPage = thePath.next();
                if (!foundTarget) {
                    foundTarget = (aPage.getID() == getPage().getID());
                }
                if (aPage.getID() == destPageID) {
                    return true;
                }
                if (foundTarget) {
                    break;
                }
            }
        }
        return false;
    } // end isPageInPath

    /**
     * isPageInPath
     * JB   25.04.2001
     */
    public boolean isPageInPath(final int destPageID, final int levels)
            throws JahiaException {
        if (processingContext != null) {
            final Iterator<ContentPage> thePath = getContentPage()
                    .getContentPagePath(levels, processingContext.getEntryLoadRequest(),
                            processingContext.getOperationMode(), getUser(),
                            JahiaPageService.PAGEPATH_SHOW_ALL);
            boolean foundTarget = false;
            while (thePath.hasNext()) {
                final ContentPage aPage = thePath.next();
                if (!foundTarget) {
                    foundTarget = (aPage.getID() == getPage().getID());
                }
                if (aPage.getID() == destPageID) {
                    return true;
                }
                if (foundTarget) {
                    break;
                }
            }
        }
        return false;
    } // end isPageInPath


    /**
     * tests if the browser is Netscape
     * JB   16.05.2001
     */
    public boolean isNS() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Mozilla") != -1) {
                if (userAgent.indexOf("MSIE") == -1) {
                    return true;
                }
            }
        }
        return false;
    } // end isNS


    /**
     * tests if the browser is Netscape 4.x.
     * JB   16.05.2001
     */
    public boolean isNS4() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Mozilla/4") != -1) {
                if (userAgent.indexOf("MSIE") == -1) {
                    return true;
                }
            }
        }
        return false;
    } // end isNS4


    /**
     * tests if the browser is Netscape 6.x.
     * JB   16.05.2001
     */
    public boolean isNS6() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Mozilla/5") != -1) {
                return true;
            }
        }
        return false;
    } // end isNS6

    /**
     * tests if the browser is Internet Explorer
     * JB   16.05.2001
     */
    public boolean isIE() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE

    /**
     * tests if the browser is Internet Explorer
     * JB   16.05.2001
     */
    public static boolean isIe(final HttpServletRequest req) {
        final String userAgent;
        final Enumeration<?> userAgentValues = req.getHeaders("user-agent");
        if (userAgentValues.hasMoreElements()) {
            // we only take the first value.
            userAgent = (String) userAgentValues.nextElement();
        } else {
            userAgent = null;
        }

        if (userAgent != null) {
            if (userAgent.indexOf("MSIE") != -1) {
                return true;
            }
        }
        return false;
    } // end isIe


    /**
     * tests if the browser is Internet Explorer 4.x.
     * JB   16.05.2001
     */
    public boolean isIE4() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE 4") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE4


    /**
     * tests if the browser is Internet Explorer 5.x.
     * JB   16.05.2001
     */
    public boolean isIE5() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE 5") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE5


    /**
     * isIE6
     * JB   16.05.2001
     */
    public boolean isIE6() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE 6") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE6

    public boolean isIE7() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("MSIE 7") != -1) {
                return true;
            }
        }
        return false;
    } // end isIE7

    /**
     * isOpera
     */
    public boolean isOpera() {
        String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            if (userAgent.indexOf("opera") != -1) {
                return true;
            }
        }
        return false;
    } // end isOpera

    /**
     * isWindow
     * JB   13.11.2001
     */
    public boolean isWindow() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Win") != -1) {
                return true;
            }
        }
        return false;
    } // end isWindow


    /**
     * isUnix
     * JB   13.11.2001
     */
    public boolean isUnix() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("X11") != -1) {
                return true;
            }
        }
        return false;
    } // end isUnix


    /**
     * isMac
     * JB   13.11.2001
     */
    public boolean isMac() {
        final String userAgent = processingContext.getUserAgent();
        if (userAgent != null) {
            if (userAgent.indexOf("Mac") != -1) {
                return true;
            }
        }
        return false;
    } // end isMac


    /**
     * checks if the current user has write access on the current page
     * JB   25.04.2001
     */
    public boolean checkWriteAccess() {
        return getPage().checkWriteAccess(getUser());
    } // end checkWriteAccess

    /**
     * Cuts and prepares a String for display at a specified length, by
     * appending "..." characters at the end and encoding the string for
     * HTML output (by replacing all non ISO-8859-1 characters with &#XX;
     * encoding).
     *
     * @param in  the String to cut and prepare for output.
     * @param len the length at which to cut it. If the string is shorter than
     *            the length then the string will be returned unmodified.
     * @return the cut string with "..." at the end if it was cut, and encoded
     *         for HTML output.
     */
    public static String glueTitle(String in, final int len) {
        if (in == null) {
            return null;
        }
        in = JahiaTools.html2text(in);
        if ((in.length()) > len && (len > 2)) {
            in = in.substring(0, len - 3) + "...";
        }
        return in;
    } // end glueTitle


    /**
     * Returns the page ID of the page that is at the specific level in the
     * page page of the current page. So if we have the following path :
     * page1 -> page2 -> page3 (current page)
     * <p/>
     * level 0 = -1 (too low, will always return -1)
     * level 1 = page1 (root page ID)
     * level 2 = page2
     * level 3 = page3
     * level 4 = -1 (doesn't exist)
     *
     * @param level the offset from the root page, specifying the number of
     *              levels to go down in the tree, 1-based.
     * @return an integer specifying the page ID of the desired level in the
     *         page path, or -1 if level was too big.
     * @throws JahiaException thrown if we have trouble retrieving the page's
     *                        path.
     */
    public int getLevelID(final int level)
            throws JahiaException {
        final Iterator<ContentPage> thePath = getContentPage().getContentPagePath(
                processingContext.getEntryLoadRequest(), processingContext.getOperationMode(), getUser());
        int count_loop = 0;
        while (thePath.hasNext()) {
            final ContentPage aPage = (ContentPage) thePath.next();
            count_loop++;
            if (count_loop == (level)) {
                return aPage.getID();
            }
        }
        return -1;
    } // end getLevelID


    /**
     * Returns the depth of the current page.
     *
     * @return an integer specifying the depth in the page tree of the current
     *         page, or -1 in the case of an error.
     * @throws JahiaException thrown if we had trouble retrieving the page's
     *                        path.
     */
    public int getLevel() throws JahiaException {
        final Iterator<ContentPage> thePath = getContentPage().getContentPagePath(
                processingContext.getEntryLoadRequest(), processingContext.getOperationMode(), getUser());
        int count_loop = 0;
        while (thePath.hasNext()) {
            final ContentPage aPage = (ContentPage) thePath.next();
            count_loop++;
            if (aPage.getID() == getPage().getID()) {
                return count_loop;
            }
        }
        return -1;
    } // end getLevel


    /**
     * getHomePage, return the site's home page
     *
     * @deprecated, use getContentHomePage
     */
    public JahiaPage getHomePage() throws JahiaException {
        if (processingContext.getSite() == null)
            return null;

        final JahiaPageService pageService = ServicesRegistry.getInstance().
                getJahiaPageService();

        // finds origin page
        return pageService.lookupPage(processingContext.getSite().getHomePageID(),
                processingContext.getEntryLoadRequest(), processingContext.getOperationMode(), processingContext.getUser(), false);

    } // end getHomePage

    /**
     * getHomePage, return the site's home page as ContentPage
     */
    public ContentPage getContentHomePage() throws JahiaException {
        if (processingContext.getSite() == null)
            return null;
        return ContentPage.getPage(processingContext.getSite().getHomePageID());

    } // end getHomePage


    private JahiaUser getUser() {
        return processingContext != null ? processingContext.getUser() : null;
    }

    private JahiaPage getPage() {
        return processingContext != null ? processingContext.getPage() : null;
    }

    private ContentPage getContentPage() {
        return processingContext != null ? processingContext.getContentPage() : null;
    }
}