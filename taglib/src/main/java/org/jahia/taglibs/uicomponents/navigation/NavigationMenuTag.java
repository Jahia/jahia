/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.uicomponents.navigation;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.templates.components.actionmenus.client.ui.actions.ActionMenuIcon;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.beans.ContainerListBean;
import org.jahia.data.beans.RequestBean;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.gui.GuiBean;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.resourcebundle.ResourceBundleMarker;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.ContainerHTMLCache;
import org.jahia.services.cache.ContainerHTMLCacheEntry;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.PageProperty;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.internal.gwt.GWTIncluder;
import org.jahia.taglibs.template.container.ContainerTag;
import org.jahia.taglibs.uicomponents.actionmenu.ActionMenuOutputter;
import org.jahia.taglibs.utility.Utils;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NavigationMenuTag extends AbstractJahiaTag {

    private static transient final Category logger = Logger.getLogger(NavigationMenuTag.class);

    private transient JahiaData jData = null;
    private String kind = null;
    private String containerListName = null;
    private String pageFieldName = null;
    private int startLevel = -1;
    private int maxDepth = -1;
    private int dispNumber = -1;
    private boolean expandOnlyPageInPath = true;
    private boolean onlyTop = false;
    private String containerListNamePostFix = null;
    private String containerNamePostFix = null;
    private String containerListLabelKey = null;
    private String pageFieldLabelKey = null;
    private int reqLevel = -1;
    private boolean editMode;
    private int[] pagePath;
    private boolean useGwt = false;
    private boolean themeMenu = false;
    private String labelKey;
    private boolean usePageIdForCacheKey = false;
    private boolean hideActionMenus = false;
    private boolean displayActionMenuBeforeLink = false;
    private boolean requiredTitle = false;
    private String actionMenuIconStyle;
    private int titleLength = 0;
    private int titleIndent = 0;
    private boolean editMenuAtEnd = true ;

    public void setTitleLength(int length) {
        this.titleLength = length;
    }

    public void setTitleIndent(int indent) {
        this.titleIndent = indent;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setContainerListName(String containerListName) {
        this.containerListName = containerListName;
    }

    public void setPageFieldName(String pageFieldName) {
        this.pageFieldName = pageFieldName;
    }

    public void setHideActionMenus(boolean hideActionMenus) {
        this.hideActionMenus = hideActionMenus;
    }

    public void setStartLevel(int startLevel) {
        this.startLevel = startLevel;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setDispNumber(int dispNumber) {
        this.dispNumber = dispNumber;
    }

    public void setExpandOnlyPageInPath(boolean expandOnlyPageInPath) {
        this.expandOnlyPageInPath = expandOnlyPageInPath;
    }

    public void setOnlyTop(boolean onlyTop) {
        this.onlyTop = onlyTop;
    }

    public void setContainerListNamePostFix(String containerListNamePostFix) {
        this.containerListNamePostFix = containerListNamePostFix;
    }

    public void setContainerNamePostFix(String containerNamePostFix) {
        this.containerNamePostFix = containerNamePostFix;
    }

    public void setContainerListLabelKey(String containerListLabelKey) {
        this.containerListLabelKey = containerListLabelKey;
    }

    public void setPageFieldLabelKey(String pageFieldLabelKey) {
        this.pageFieldLabelKey = pageFieldLabelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public void setUsePageIdForCacheKey(boolean usePageIdForCacheKey) {
        this.usePageIdForCacheKey = usePageIdForCacheKey;
    }

    public void setRequiredTitle(boolean requiredTitle) {
        this.requiredTitle = requiredTitle;
    }

    public void setActionMenuIconStyle(String actionMenuIconStyle) {
        this.actionMenuIconStyle = actionMenuIconStyle;
    }

    public void setDisplayActionMenuBeforeLink(boolean displayActionMenuBeforeLink) {
        this.displayActionMenuBeforeLink = displayActionMenuBeforeLink;
    }

    public void setEditMenuAtEnd(boolean editMenuAtEnd) {
        this.editMenuAtEnd = editMenuAtEnd;
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT);

    public int doStartTag() throws JspException {
        final RequestBean requestBean = (RequestBean) pageContext.findAttribute("currentRequest");
        ServletRequest request = pageContext.getRequest();
        jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

        // set various useful parameters
        editMode = requestBean.isEditMode();
        /**String cacheParam = jData.getProcessingContext().getParameter(ProcessingContext.CONTAINERCACHE_MODE_PARAMETER) ;
         if (cacheParam !=  null) {
         if ("debug".equals(cacheParam)) {
         debug = true ;
         } else if ("off".equals(cacheParam)) {
         cache = false ;
         }
         }**/

        try {
            final ContainerHTMLCache cacheInstance =
                    ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
            final ProcessingContext context = jData.getProcessingContext();
            String cacheKey = "siteId_" + context.getSiteID() + ((usePageIdForCacheKey) ? "_pageId_" + context.getPageID() : "") + "_ctnListName_"
                    + containerListName + "_cssName_" + cssClassName + "_ctnListPostfix_" + containerListNamePostFix + kind;
            StringBuffer advPreviewMode = new StringBuffer();
            if (AdvPreviewSettings.isInUserAliasingMode() || AdvPreviewSettings.isPreviewingAtDefinedDateMode()) {
                advPreviewMode.append(AdvPreviewSettings.getThreadLocaleInstance());
            }
            cacheKey += advPreviewMode.toString();
            boolean debug = "debug".equals(context.getParameter(ProcessingContext.CONTAINERCACHE_MODE_PARAMETER));
            if (debug && !(cacheKey.indexOf("___debug___") > 0)) cacheKey = cacheKey + "___debug___";
            boolean currentCache = true;
            final CacheEntry htmlCacheEntry = cacheInstance.getCacheEntryFromContainerCache(null, context, cacheKey, false, 0, null, null);
            String htmlOutput;
            Date expireDate = new Date();
            boolean cacheParam2 = (!ProcessingContext.NORMAL.equals(context.getOperationMode()) && Jahia.getSettings().isContainerCacheLiveModeOnly())
                    || (context.getEntryLoadRequest() != null && context.getEntryLoadRequest().isVersioned());
            final boolean outputContainerCacheActivated = org.jahia.settings.SettingsBean.getInstance().isOutputContainerCacheActivated();
            if (htmlCacheEntry != null && !cacheParam2 && outputContainerCacheActivated) {
                expireDate = htmlCacheEntry.getExpirationDate();
                ContainerHTMLCacheEntry entry = (ContainerHTMLCacheEntry) htmlCacheEntry.getObject();
                htmlOutput = entry.getBodyContent();
                currentCache = false;
            } else {
                StringBuffer out = new StringBuffer(4096);
                Set<ContentObjectKey> dependencies = new HashSet<ContentObjectKey>();
                createContainerList();
                settings(jData);
                if (cssClassName == null) {
                    out.append("<div class=\"navMenu\">\n");
                } else {
                    out.append("<div class=\"").append(cssClassName).append("\">\n");
                }
                if (useGwt) {
                    useGwtMenu(out);
                } else if (themeMenu) {
                    drawThemeMenu();
                } else {
                    getPageSubTree(jData, jData.gui().getLevelID(startLevel), startLevel, out, dependencies, 0);
                }
                out.append("</div>\n");
                htmlOutput = out.toString();
                if (htmlOutput != null && htmlOutput.length() > 0 && !cacheParam2 && outputContainerCacheActivated) {
                    cacheInstance.writeToContainerCache(null, context, htmlOutput, cacheKey, dependencies, -1);
                }
            }
            final JspWriter writer = pageContext.getOut();
            if (debug) {
                if (currentCache) {
                    writer.print("<fieldset><legend align=\"right\">written to cache (at " + dateFormat.format(new Date()) + ")</legend>");
                } else {
                    writer.print("<fieldset><legend align=\"right\">getting from cache (will expire at " + dateFormat.format(expireDate) + ")</legend>");
                }
            }
            writer.print("<!-- cache:include src=\"" + context.getSiteURL(context.getSite(), context.getPageID(), false, true, true) + "?ctnid=0&cacheKey=" + cacheKey + "\" -->");
            writer.print(htmlOutput);
            writer.print("<!-- /cache:include -->\n");
            if (debug) {
                writer.println("</fieldset>");
            }
        } catch (JahiaException e) {
            logger.error("JahiaException rendering the menu", e);
        } catch (IOException e) {
            logger.error("IOException rendering the menu", e);
        } catch (ClassNotFoundException e) {
            logger.error("Class cast exception", e);
        }
        return SKIP_BODY;
    }

    /**
     * Create the container list and the field if they don't exist
     *
     * @throws JahiaException if only the list or the field is specified
     */
    private void createContainerList() throws JahiaException {
//        final Locale locale = jData.getProcessingContext().getLocale();
        if (containerListLabelKey == null) {
            containerListLabelKey = "navmenu.containerlistname";
        }
        if (pageFieldLabelKey == null) {
            pageFieldLabelKey = "navmenu.containername";
        }

        if (containerListName == null && pageFieldName == null) {
            // use default names
            containerListName = "navLink";
            pageFieldName = "navLink";

        } else if (containerListName != null ^ pageFieldName != null) {
            throw new JahiaException("You must either declare both container list and page field or none",
                    "Unable to create the navmenu container list", JahiaException.TEMPLATE_SERVICE_ERROR,
                    JahiaException.ERROR_SEVERITY);
        }
    }

    /**
     * Prepare tag environment depending on some tag attributes.
     *
     * @param jData JahiaData
     * @throws JahiaException if start level was not specified and container list retrieval failed
     * @throws javax.servlet.jsp.JspTagException
     *                        tag exception
     */
    private void settings(JahiaData jData) throws JahiaException, JspTagException {
        // set the current page path
        pagePath = ServicesRegistry.getInstance().getJahiaPageService().getCurrentPagePathAsPIDs(jData.getProcessingContext());

        // set the resource names
        if (containerListNamePostFix == null) {
            containerListNamePostFix = "navmenu";
        }
        if (containerNamePostFix == null) {
            containerNamePostFix = "navlink";
        }

        // set various parameters according to the menu kind
        if (kind != null) {

            // top tabs configuration
            if ("topTabs".equals(kind)) {
                if (cssClassName == null) {
                    cssClassName = "topTabs"; // default name
                }
                onlyTop = true;
                if (startLevel == -1) {
                    startLevel = 1; // default start level
                }
            } else if ("sideMenu".equals(kind)) {
                if (cssClassName == null) {
                    cssClassName = "sideMenu";
                }
                if (startLevel == -1) {
                    startLevel = 2; // default start level
                }
            } else if ("dropDownMenu".equals(kind)) {
                if (cssClassName == null) {
                    cssClassName = "dropDownMenu";
                }
                if (startLevel == -1) {
                    startLevel = 1; // default start level
                }
                useGwt = true;
            } else if ("themeMenu".equals(kind)) {
                themeMenu = true;

            } else {
                // throw new JspTagException("Unknowm value for attribute kind '" + kind +
                //         "'. Allowed values are 'topTabs', 'sideMenu', 'dropDownMenu' or 'themeMenu'");
            }
        }

        GuiBean gui = jData.gui();

        // set the level of the page where the request is coming from
        reqLevel = gui.getLevel();
        logger.debug("reqLevel = " + reqLevel);
        // if the startLevel parameter is not specified, try to find a similar
        // containerList in the parent page
        // SHOULD BE AVOIDED TO IMPROVE PERFORMANCE -> user manual + specs
        if (startLevel == -1) {
            if (reqLevel == 1) {
                startLevel = reqLevel;
            } else {
                JahiaContainerList linkContainerList = jData.containers().getAbsoluteContainerList(containerListName, gui.getLevelID(reqLevel - 1));
                if (linkContainerList != null) {
                    startLevel = reqLevel - 1;
                } else {
                    startLevel = reqLevel;
                }
            }
        }
    }

    /**
     * This is the GWT menu if needed.
     *
     * @param out the JSP writer
     */
    private void useGwtMenu(StringBuffer out) {
        // use the gwt module
        out.append("<div id=\"default_sitemap\" class=\"sitemap\"></div>");
        out.append(GWTIncluder.generateGWTImport(pageContext, "org.jahia.ajax.gwt.navmenu.NavmenuManager"));
    }

    /**
     * Recursive method to go through pages hierarchy using a specific container list (attribute containerListName).
     *
     * @param jData        JahiaData
     * @param pageId       the page id where to get the container list
     * @param level        the current depth level
     * @param out          the stringbuffer to write output into
     * @param dependencies dependencies for the cache entry
     * @param loopIt       index of current iteration
     * @throws JahiaException         exception retrieving cache or containers
     * @throws IOException            JSP writer exception
     * @throws ClassNotFoundException exception building clist bean
     */
    private void getPageSubTree(JahiaData jData, int pageId, int level, StringBuffer out, Set<ContentObjectKey> dependencies, int loopIt) throws JahiaException, IOException, ClassNotFoundException {
        if (pageId < 1) {
            logger.error("Incorrect page ID: " + pageId);
            // throw new IllegalArgumentException("attribute pageID cannot be < 1 (is " + pageId + ")");
            return;
        }

        ProcessingContext jParams = jData.getProcessingContext();
        JahiaContainerList linkContainerList = jData.containers().getAbsoluteContainerList(containerListName, pageId);
        if (linkContainerList == null) {
            logger.warn("Linkcontainer list is null");
            return;
        }
        dependencies.add(new ContentContainerListKey(linkContainerList.getID()));
        // store ContentBean in page context
        ContainerListBean containerListBean = new ContainerListBean(linkContainerList, jParams);
        pageContext.setAttribute(containerListName, containerListBean);

        Iterator linkContainerEnum = linkContainerList.getContainers();
        boolean begin = true;

        // don't display the links if they are supposed to be hidden (live mode)
        boolean hide = (dispNumber != -1 && level != startLevel && level != reqLevel && level + dispNumber < reqLevel && !editMode);

        /*if (editMode && (level >= reqLevel - 1 || onlyTop)) {
            out.append("<div class=\"subContainerList\">");
        }*/

        if (!editMenuAtEnd) {
            if (editMode && !hideActionMenus && ((level == reqLevel -1 || level == reqLevel) || onlyTop)) { // new page can only be created as a sibling or a child to the current or the top page (visual coherence limitation)
                String actionMenuDisplay = new ActionMenuOutputter(jParams, pageContext, new ContainerListBean(linkContainerList, jParams), containerListName, linkContainerList.getContentContainerList().getObjectKey().toString(), getResourceBundle(), containerListNamePostFix, labelKey, actionMenuIconStyle).getOutput(false);
                if (actionMenuDisplay != null) {
                    if (begin) {
                        if (cssClassName == null) {
                            out.append("<ul class=\"level_").append(level).append("\">");
                        } else {
                            out.append("<ul class=\"level_").append(level).append(" ").append(cssClassName).append("\">");
                        }

                    }
                    out.append("<li class=\"item_action standard");
                    // first
                    if (begin) {
                        out.append(" first");
                        begin = false;
                    }
                    // last
                    if (!linkContainerEnum.hasNext()) {
                        out.append(" last");
                    }
                    out.append("\">");
                    out.append(actionMenuDisplay);
                    out.append("</li>\n");
                }
            }
        }

        if (maxDepth == -1 || level <= startLevel + maxDepth) {
            int itemCount = 0;
            while (linkContainerEnum.hasNext()) {
                itemCount++;
                logger.debug("level = " + level);
                JahiaContainer linkContainer = (JahiaContainer) linkContainerEnum.next();
                JahiaPage link = (JahiaPage) linkContainer.getFieldObject(pageFieldName);
                dependencies.add(new ContentContainerKey(linkContainer.getID()));
                if ((jParams.getOperationMode().equals(ProcessingContext.NORMAL) ||
                        jParams.getOperationMode().equals(ProcessingContext.PREVIEW)) && link != null) {
                    final PageProperty hideFromMenuProp = link.getPageLocalProperty(PageProperty.HIDE_FROM_NAVIGATION_MENU);
                    if (hideFromMenuProp != null && Boolean.valueOf(hideFromMenuProp.getValue())) {
                        continue;
                    }
                }
                if (requiredTitle) {
                    if (!ContainerTag.hasContainerAllRequiredFields(linkContainer, pageFieldName, jParams)) {
                        continue;
                    }
                }
                if (link != null) {
                    String title = link.getTitle();
                    // don't display the link if undisplayable and in non edit mode
                    if (!editMode && (title == null || title.equals(""))) {
                        continue;
                    }
                    int linkID = link.getID();

                    boolean isInPath = Utils.isContainedInArray(pagePath, linkID);

                    if (!hide) {
                        if (begin) {
                            if (cssClassName == null) {
                                out.append("<ul class=\"level_").append(level).append("\">");
                            } else {
                                out.append("<ul class=\"level_").append(level).append(" ").append(cssClassName).append("\">");
                            }
                        }

                        // set class = "selected" on the link
                        final StringBuffer selected = new StringBuffer();
                        out.append("<li class=\"item_").append(itemCount);
                        if (isInPath) {
                            if (level == reqLevel - 1) {
                                out.append(" selected");
                                selected.append("selected");
                            } else {
                                out.append(" inpath");
                                selected.append("inpath");
                            }
                        } else {
                            out.append(" standard");
                        }
                        // Class on last and first item
                        // First container
                        if (begin) {
                            out.append(" first");
                            begin = false;
                        }
                        // Last container
                        if (!linkContainerEnum.hasNext()) {
                            out.append(" last");
                        }
                        if (linkContainer.getContentContainer().isMarkedForDelete()) {
                            selected.append(" markedForDelete");
                        }
                        out.append("\"");
                        out.append(">\n");

                        if (!hideActionMenus && displayActionMenuBeforeLink) {
                            String actionMenuDisplay = new ActionMenuOutputter(jParams, pageContext, new ContainerBean(linkContainer, jParams), containerListName, linkContainer.getContentContainer().getObjectKey().toString(), ActionMenuIcon.CONTAINER_EDIT, getResourceBundle(), "navlink", "navlink", actionMenuIconStyle).getOutput(false);
                            if (actionMenuDisplay != null) {
                                out.append(actionMenuDisplay);
                            }
                        }

                        String dispLink = link.getHighLightDiffTitle(jParams);

                        if (dispLink != null && titleLength > 0) {
                            int nbCharToDisp = titleLength - (loopIt * titleIndent);
                            if (nbCharToDisp > 0 && nbCharToDisp < dispLink.length()) {
                                dispLink = new StringBuilder(dispLink.substring(0, nbCharToDisp)).append("...").toString();
                            }
                        }

                        if (dispLink == null) {
                            dispLink = JahiaResourceBundle.getEngineResource("org.jahia.engines.workflow.display.notitle", jParams, jParams.getLocale(), "n/d");
                        }
                        if (title == null) {
                            title = JahiaResourceBundle.getEngineResource("org.jahia.engines.workflow.display.notitle", jParams, jParams.getLocale(), "n/d");
                        }

                        out.append("<a href=\"").append(link.getURL(jParams)).append("\"").append(" class=\"");
                        out.append(selected);
                        out.append("\"");
                        out.append(" title=\"").append(title).append("\">");
                        out.append(dispLink).append("</a>");

                        if (!hideActionMenus && !displayActionMenuBeforeLink) {
                            String actionMenuDisplay = new ActionMenuOutputter(jParams, pageContext, new ContainerBean(linkContainer, jParams), containerListName, linkContainer.getContentContainer().getObjectKey().toString(), ActionMenuIcon.CONTAINER_EDIT, getResourceBundle(), "navlink", "navlink", actionMenuIconStyle).getOutput(false);
                            if (actionMenuDisplay != null) {
                                out.append(actionMenuDisplay);
                            }
                        }

                        out.append("\n");
                    }

                    // displays sub links
                    if (!onlyTop && (!expandOnlyPageInPath || isInPath)) {
                        getPageSubTree(jData, linkID, level + 1, out, dependencies, loopIt + 1);
                    }
                    if (!hide) out.append("</li>\n");

                } else {
                    // no navlink
                    if (editMode && !hideActionMenus) {
                        String actionMenuDisplay = new ActionMenuOutputter(jParams, pageContext, new ContainerBean(linkContainer, jParams), containerListName, linkContainer.getContentContainer().getObjectKey().toString(), ActionMenuIcon.CONTAINER_EDIT, getResourceBundle(), "navlink", "navlink", actionMenuIconStyle).getOutput(false);
                        if (actionMenuDisplay != null) {
                            if (begin) {
                                if (cssClassName == null) {
                                    out.append("<ul class=\"level_").append(level).append("\">");
                                } else {
                                    out.append("<ul class=\"level_").append(level).append(" ").append(cssClassName).append("\">");
                                }
                                begin = false;
                            }

                            out.append("<li>\n");
                            if (displayActionMenuBeforeLink) {
                                out.append(actionMenuDisplay);
                                out.append("<a><span>n.d.</span></a>");
                            } else {
                                out.append("<a><span>n.d.</span></a>");
                                out.append(actionMenuDisplay);
                            }
                            out.append("</li>\n");
                        }
                    }
                }
            }
        }

        if (editMenuAtEnd) {
            if (editMode && !hideActionMenus && ((level == reqLevel -1 || level == reqLevel) || onlyTop)) { // new page can only be created as a sibling or a child to the current or the top page (visual coherence limitation)
                String actionMenuDisplay = new ActionMenuOutputter(jParams, pageContext, new ContainerListBean(linkContainerList, jParams), containerListName, linkContainerList.getContentContainerList().getObjectKey().toString(), getResourceBundle(), containerListNamePostFix, labelKey, actionMenuIconStyle).getOutput(false);
                if (actionMenuDisplay != null) {
                    if (begin) {
                        if (cssClassName == null) {
                            out.append("<ul class=\"level_").append(level).append("\">");
                        } else {
                            out.append("<ul class=\"level_").append(level).append(" ").append(cssClassName).append("\">");
                        }

                    }
                    out.append("<li class=\"item_action standard");
                    // first
                    if (begin) {
                        out.append(" first");
                        begin = false;
                    }
                    // last
                    if (!linkContainerEnum.hasNext()) {
                        out.append(" last");
                    }
                    out.append("\">");
                    out.append(actionMenuDisplay);
                    out.append("</li>\n");
                }
            }
        }

        if (!begin && !hide) {
            out.append("</ul>\n");
        }

        /*if (editMode && !hideActionMenus && (level >= reqLevel - 1 || onlyTop)) {
            out.append("</div>\n");
        }*/
    }

    protected String resolveTitle(final String title,
                                  final String bundleKey,
                                  final String titleKey,
                                  final Locale locale) {
        if ((titleKey != null) && (bundleKey != null)) {
            String tmp = ResourceBundleMarker.getValue(title, bundleKey, titleKey, locale);
            if (tmp == null || tmp.equals(title)) {
                return ResourceBundleMarker.drawMarker(AbstractJahiaTag.COMMON_TAG_BUNDLE, titleKey, title);
            }
            return ResourceBundleMarker.drawMarker(bundleKey, titleKey, title);
        } else {
            return title;
        }
    }

    /**
     * Draw a navigation menu displaying the current sibling pages (if any) and the subpages of the current page.
     */
    private void drawThemeMenu() {
        // TODO
    }


    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        kind = null;
        expandOnlyPageInPath = true;
        onlyTop = false;
        reqLevel = -1;
        startLevel = -1;
        dispNumber = -1;
        maxDepth = -1;
        jData = null;
        containerListName = null;
        pageFieldName = null;
        containerListLabelKey = null;
        pageFieldLabelKey = null;
        containerListNamePostFix = null;
        containerNamePostFix = null;
        pagePath = null;
        themeMenu = false;
        labelKey = null;
        hideActionMenus = false;
        requiredTitle = false;
        displayActionMenuBeforeLink = false;
        editMenuAtEnd = true ;
        super.resetState();
        return EVAL_PAGE;
    }
}