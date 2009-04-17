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
import org.jahia.ajax.gwt.client.widget.actionmenu.actions.ActionMenuIcon;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.beans.ContainerListBean;
import org.jahia.data.beans.RequestBean;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.gui.GuiBean;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.ContainerHTMLCache;
import org.jahia.services.cache.ContainerHTMLCacheEntry;
import org.jahia.services.cache.GroupCacheKey;
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

@SuppressWarnings("serial")
public class NavigationMenuTag extends AbstractJahiaTag {

    private static transient final Category logger = Logger.getLogger(NavigationMenuTag.class);

    private transient JahiaData jData = null;
    private String kind = null;
    private String containerListName = null;
    private String pageFieldName = null;
    private String separatorName = null;
    private int startLevel = -1;
    private int startPid = -1;
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
    private boolean display = true;
    private String var = "navMenuBeanSet";
    private static final String INTERNAL_CACHE_DEPENDENCIES = "internalCacheDependencies";
    private String mockupClass = null ;

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

    public void setStartPid(int startPid) {
        this.startPid = startPid;
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

    public void setVar(String var) {
        this.var = var;
    }

    public void setMockupClass(String mockupClass) {
        this.mockupClass = mockupClass ;
    }


    public void setDisplay(boolean display) {
        this.display = display;
    }

    public String getVar() {
        return var;
    }

    public boolean isDisplay() {
        return display;
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
            final ContainerHTMLCache<GroupCacheKey, ContainerHTMLCacheEntry> cacheInstance =
                    ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
            final ProcessingContext context = jData.getProcessingContext();
            String cacheKey = "siteId_" + context.getSiteID() + ((usePageIdForCacheKey) ? "_pageId_" + context.getPageID() : "") + "_ctnListName_"
                    + containerListName + "_cssName_" + cssClassName + "_ctnListPostfix_" + containerListNamePostFix + kind;
            StringBuilder advPreviewMode = new StringBuilder();
            if (AdvPreviewSettings.isInUserAliasingMode() || AdvPreviewSettings.isPreviewingAtDefinedDateMode()) {
                advPreviewMode.append(AdvPreviewSettings.getThreadLocaleInstance());
            }
            cacheKey += advPreviewMode.toString();
            boolean debug = "debug".equals(context.getParameter(ProcessingContext.CONTAINERCACHE_MODE_PARAMETER));
            if (debug && !(cacheKey.indexOf("___debug___") > 0)) cacheKey = cacheKey + "___debug___";
            boolean currentCache = true;
            final CacheEntry<ContainerHTMLCacheEntry> htmlCacheEntry = cacheInstance.getCacheEntryFromContainerCache(null, context, cacheKey, false, 0, null, null);
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
                StringBuilder out = new StringBuilder(4096);
                Set<ContentObjectKey> dependencies = new HashSet<ContentObjectKey>();
                Set<NavMenuItemBean> navMenuUItemsBean = new LinkedHashSet<NavMenuItemBean>();
                createContainerList();
                settings();
                StringBuilder cssToUse = new StringBuilder() ;
                if (cssClassName == null) {
                    cssToUse.append("navMenu") ;
                } else {
                    cssToUse.append(cssClassName) ;
                }

                StringBuilder generatedMenu = new StringBuilder() ;
                boolean emptyMenu = false ;
                if (useGwt) {
                    useGwtMenu(generatedMenu);
                } else if (themeMenu) {
                    drawThemeMenu();
                }
                if (startPid > 0) {
                    emptyMenu = !getPageSubTree(startPid, startLevel, navMenuUItemsBean, dependencies, 0);
                } else {
                    emptyMenu = !getPageSubTree(jData.gui().getLevelID(startLevel), startLevel, navMenuUItemsBean, dependencies, 0);
                }
                pageContext.setAttribute(INTERNAL_CACHE_DEPENDENCIES,dependencies);
                if (!display) {
                    pageContext.setAttribute(var, navMenuUItemsBean);
                    return EVAL_BODY_BUFFERED;
                } else {
                    if (emptyMenu && mockupClass != null) {
                        cssToUse.append(" ").append(mockupClass) ;
                    }
                    out.append("<div class=\"").append(cssToUse).append("\">\n");
                    displayMenu(out, navMenuUItemsBean);
                    out.append("</div>\n");
                    bodyContent = pageContext.pushBody();
                    bodyContent.write(out.toString());
                }
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

        if (containerListName == null && pageFieldName == null && separatorName == null) {
            // use default names
            containerListName = "navLink";
            pageFieldName = "navLink";
            separatorName = "separator";

        } else if (containerListName != null ^ pageFieldName != null ^ separatorName !=null) {
            throw new JahiaException("You must either declare container list, page field and separator field or none",
                    "Unable to create the navmenu container list", JahiaException.TEMPLATE_SERVICE_ERROR,
                    JahiaException.ERROR_SEVERITY);
        }
    }

    /**
     * Prepare tag environment depending on some tag attributes.
     *
     * @throws JahiaException if start level was not specified and container list retrieval failed
     * @throws javax.servlet.jsp.JspTagException
     *                        tag exception
     */
    private void settings() throws JahiaException, JspTagException {
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
    private void useGwtMenu(StringBuilder out) {
        // use the gwt module
        out.append("<div id=\"default_sitemap\" class=\"sitemap\"></div>");
        out.append(GWTIncluder.generateGWTImport(pageContext, "org.jahia.ajax.gwt.navmenu.NavmenuManager"));
    }

    /**
     * Method to display a standard menu.
     *
     * @param navMenuItemsBean Set of navMenuItemBean
     * @param out display Stringbuffer
     * @throws IOException            JSP writer exception
     */

    private void displayMenu(StringBuilder out, Set<NavMenuItemBean> navMenuItemsBean) {

        for (NavMenuItemBean navMenuItemBean : navMenuItemsBean) {
            // Generate CSS arguments
            String liCssString = "";
            String aCssString = "";
            if (navMenuItemBean.isFirstInLevel()) {
                out.append("<ul class=\"level_").append(navMenuItemBean.getLevel()).append("\">");
                if (editMode  & !editMenuAtEnd) {
                    out.append("<li>"+navMenuItemBean.getActionMenuList()+"</li>");
                }
                liCssString += " first";
            }
            if (!navMenuItemBean.isFirstInLevel()) {
                out.append("</li>\n");

            }
            if (navMenuItemBean.isLastInLevel()) {
                liCssString += " last";
            }
            if (navMenuItemBean.isInPath()) {
                aCssString += " inpath";
            }
            if (navMenuItemBean.isSelected()) {
                aCssString += " selected";
            }
            if (navMenuItemBean.isMarkedForDelete()) {
                aCssString += "markedForDelete";
            }

            out.append("<li class=\"item_").append(navMenuItemBean.getItemCount());
            if (cssClassName != null) {
                out.append(" ").append(cssClassName);
            }
            out.append(" ").append(liCssString).append("\">");
            if (!navMenuItemBean.isActionMenuOnly()) {
                            if (displayActionMenuBeforeLink && editMode) {
                    out.append(navMenuItemBean.getActionMenu());
                }
                if (navMenuItemBean.getUrl().length() > 0) {
                    out.append("<a class=\"").append(aCssString).append("\" href=\"").append(navMenuItemBean.getUrl()).append("\">");
                }
                out.append("<span>");
                out.append(navMenuItemBean.getDisplayLink());
                out.append("</span>");
                if (navMenuItemBean.getUrl().length() > 0) {
                    out.append("</a>");
                }
                if (!displayActionMenuBeforeLink && editMode) {
                    out.append(navMenuItemBean.getActionMenu());
                }
            }
            if (navMenuItemBean.isLastInLevel()) {
                out.append("</li>\n");
                if (editMode  & editMenuAtEnd) {
                    out.append("<li>"+navMenuItemBean.getActionMenuList()+"</li>");
                }
                out.append("</ul>\n");
            }
        }
    }
    /**
     * Draw a navigation menu displaying the current sibling pages (if any) and the subpages of the current page.
     */
    private void drawThemeMenu() {
        // TODO
    }

    /**
     * Recursive method to go through pages hierarchy using a specific container list (attribute containerListName)
     * to fill a set (attribute navMenuItemsBean) containing all entries of the menu.
     *
     * @param pageId       the page id where to get the container list
     * @param level        the current depth level
     * @param navMenuItemsBean  Set of navMenuItemBean containing informations of current menu item.
     * @param dependencies dependencies for the cache entry
     * @param loopIt       index of current iteration
     * @throws JahiaException         exception retrieving cache or containers
     * @throws IOException            JSP writer exception
     * @throws ClassNotFoundException exception building clist bean
     */
    private boolean getPageSubTree(int pageId, int level, Set<NavMenuItemBean> navMenuItemsBean, Set<ContentObjectKey> dependencies, int loopIt) throws JahiaException, IOException, ClassNotFoundException {

        String mainActionMenu = "";
        if (pageId < 1) {
            logger.error("Incorrect page ID: " + pageId);
            // throw new IllegalArgumentException("attribute pageID cannot be < 1 (is " + pageId + ")");
            return false;
        }

        ProcessingContext jParams = jData.getProcessingContext();
        JahiaContainerList linkContainerList = jData.containers().getAbsoluteContainerList(containerListName, pageId);
        if (linkContainerList == null) {
            logger.warn("Linkcontainer list is null");
            return false;
        }
        dependencies.add(new ContentContainerListKey(linkContainerList.getID()));
        // store ContentBean in page context
        ContainerListBean containerListBean = new ContainerListBean(linkContainerList, jParams);
        pageContext.setAttribute(containerListName, containerListBean);

        Iterator<JahiaContainer> linkContainerEnum = linkContainerList.getContainers();
        boolean begin = true;

        // don't display the links if they are supposed to be hidden (live mode)
        boolean hide = (dispNumber != -1 && level != startLevel && level != reqLevel && level + dispNumber < reqLevel && !editMode);

        /*if (editMode && (level >= reqLevel - 1 || onlyTop)) {
            out.append("<div class=\"subContainerList\">");
        }*/


        if (editMode && !hideActionMenus && ((level == reqLevel -1 || level == reqLevel) || onlyTop)) { // new page can only be created as a sibling or a child to the current or the top page (visual coherence limitation)
            String mainActionMenuDisplay = new ActionMenuOutputter(jParams, pageContext, new ContainerListBean(linkContainerList, jParams), containerListName, linkContainerList.getContentContainerList().getObjectKey().toString(), getResourceBundle(), containerListNamePostFix, labelKey, actionMenuIconStyle).getOutput(false);
            if (mainActionMenuDisplay != null) {
                mainActionMenu = mainActionMenuDisplay;
            }
            if (!linkContainerEnum.hasNext()) {
                NavMenuItemBean navMenuItemBean = new NavMenuItemBean();
                navMenuItemBean.setActionMenuList(mainActionMenu);
                navMenuItemBean.setLevel(level);
                navMenuItemBean.setLastInLevel(true);
                navMenuItemBean.setFirstInLevel(true);
                navMenuItemBean.setActionMenuOnly(true);
                navMenuItemsBean.add(navMenuItemBean);
            }
        }

        boolean isEmpty = true ;
        // if the list empty, add a navMenuItem for the action menu
        if (maxDepth == -1 || level <= startLevel + maxDepth) {
            int itemCount = 0;
            while (linkContainerEnum.hasNext()) {
                NavMenuItemBean navMenuItemBean = new NavMenuItemBean();
                navMenuItemBean.setActionMenuOnly(false);
                navMenuItemBean.setActionMenuList(mainActionMenu);
                itemCount++;
                isEmpty = false;
                navMenuItemBean.setItemCount(itemCount);
                logger.debug("level = " + level);
                JahiaContainer linkContainer = (JahiaContainer) linkContainerEnum.next();
                JahiaPage link = (JahiaPage) linkContainer.getFieldObject(pageFieldName);
                JahiaField separator = (JahiaField) linkContainer.getField(separatorName);
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

                if (!hide) {
                    // Set level
                    navMenuItemBean.setLevel(level);
                    // set class = "selected" on the link
                    // First container
                    if (begin) {
                        navMenuItemBean.setFirstInLevel(true);
                        begin = false;
                    }
                    // Last container
                    if (!linkContainerEnum.hasNext()) {
                        navMenuItemBean.setLastInLevel(true);
                    }
                    if (linkContainer.getContentContainer().isMarkedForDelete()) {
                        navMenuItemBean.setMarkedForDelete(true);
                    }

                    if (!hideActionMenus) {
                        String actionMenuDisplay = new ActionMenuOutputter(jParams, pageContext, new ContainerBean(linkContainer, jParams), containerListName, linkContainer.getContentContainer().getObjectKey().toString(), ActionMenuIcon.CONTAINER_EDIT, getResourceBundle(), "navlink", "navlink", actionMenuIconStyle).getOutput(false);
                        if (actionMenuDisplay != null) {
                            navMenuItemBean.setActionMenu(actionMenuDisplay);
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

                        if (isInPath) {
                            if (level == reqLevel - 1) {
                                navMenuItemBean.setSelected(true);
                            } else {
                                navMenuItemBean.setInPath(true);
                            }
                        }

                        String dispLink = link.getHighLightDiffTitle(jParams);

                        if (dispLink != null && titleLength > 0) {
                            int nbCharToDisp = titleLength - (loopIt * titleIndent);
                            if (nbCharToDisp > 0 && nbCharToDisp < dispLink.length()) {
                                dispLink = new StringBuilder(dispLink.substring(0, nbCharToDisp)).append("...").toString();
                            }
                        }

                        if (dispLink == null || dispLink.length() == 0) {
                            dispLink = getMessage("noPageTitle", "n/d");
                        }
                        if (title == null || title.length() == 0) {
                            title = getMessage("noPageTitle", "n/d");
                        }
                        navMenuItemBean.setUrl(link.getURL(jParams));
                        navMenuItemBean.setTitle(title);
                        navMenuItemBean.setDisplayLink(dispLink);
                        navMenuItemsBean.add(navMenuItemBean);
                        // displays sub links

                        //
                        if (!onlyTop && (!expandOnlyPageInPath || isInPath)) {
                            getPageSubTree(linkID, level + 1, navMenuItemsBean, dependencies, loopIt + 1);
                        }
                    }
                    else {
                        if (separator != null && separator.getValue().length() > 0) {
                            navMenuItemBean.setTitle(separator.getValue());
                            navMenuItemBean.setDisplayLink(separator.getValue());
                        }
                        else {
                        // add an empty navItemBean to show the action menu if needed
                            navMenuItemBean.setTitle(getMessage("noPageSet", "n/d"));
                            navMenuItemBean.setDisplayLink(getMessage("noPageSet", "n/d"));
                        }
                        navMenuItemsBean.add(navMenuItemBean);
                    }
                }
            }
        }
        return !isEmpty ;
    }

    public int doEndTag() throws JspException {
        boolean debug = "debug".equals(jData.getProcessingContext().getParameter(ProcessingContext.CONTAINERCACHE_MODE_PARAMETER));
        final ProcessingContext context = jData.getProcessingContext();
        final JspWriter writer = getPreviousOut();
        final ContainerHTMLCache<GroupCacheKey, ContainerHTMLCacheEntry> cacheInstance;
        try {
            String htmlOutput="";
            if (bodyContent != null && bodyContent.getString().length()>0) {
                htmlOutput = bodyContent.getString();
            }
            cacheInstance = ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
            String cacheKey = "siteId_" + context.getSiteID() + ((usePageIdForCacheKey) ? "_pageId_" + context.getPageID() : "") + "_ctnListName_"
                    + containerListName + "_cssName_" + cssClassName + "_ctnListPostfix_" + containerListNamePostFix + kind;
            StringBuffer advPreviewMode = new StringBuffer();
            if (AdvPreviewSettings.isInUserAliasingMode() || AdvPreviewSettings.isPreviewingAtDefinedDateMode()) {
                advPreviewMode.append(AdvPreviewSettings.getThreadLocaleInstance());
            }
            cacheKey += advPreviewMode.toString();
            final CacheEntry<ContainerHTMLCacheEntry> htmlCacheEntry = cacheInstance.getCacheEntryFromContainerCache(null, context, cacheKey, false, 0, null, null);
            final boolean outputContainerCacheActivated = org.jahia.settings.SettingsBean.getInstance().isOutputContainerCacheActivated();
            boolean currentCache = true;
            Date expireDate = new Date();
            boolean cacheParam2 = (!ProcessingContext.NORMAL.equals(context.getOperationMode()) && Jahia.getSettings().isContainerCacheLiveModeOnly())
                    || (context.getEntryLoadRequest() != null && context.getEntryLoadRequest().isVersioned());
            if (htmlCacheEntry != null && !cacheParam2 && outputContainerCacheActivated) {
                expireDate = htmlCacheEntry.getExpirationDate();
                ContainerHTMLCacheEntry entry = (ContainerHTMLCacheEntry) htmlCacheEntry.getObject();
                htmlOutput = entry.getBodyContent();
                currentCache = false;
            }
            if (debug) {
                if (currentCache) {
                    writer.print("<fieldset><legend align=\"right\">written to cache (at " + dateFormat.format(new Date()) + ")</legend>");
                } else {
                    writer.print("<fieldset><legend align=\"right\">getting from cache (will expire at " + dateFormat.format(expireDate) + ")</legend>");
                }
            }
            if (htmlOutput != null && htmlOutput.length() > 0 && !cacheParam2 && outputContainerCacheActivated) {
                Set<ContentObjectKey> dependencies = (Set<ContentObjectKey>) pageContext.getAttribute(INTERNAL_CACHE_DEPENDENCIES);
                    cacheInstance.writeToContainerCache(null, context, htmlOutput, cacheKey, dependencies, -1);
                }
            writer.print("<!-- cache:include src=\"" + context.getSiteURL(context.getSite(), context.getPageID(), false, true, true) + "?ctnid=0&cacheKey=" + cacheKey + "\" -->");
            writer.print(htmlOutput);
            writer.print("<!-- /cache:include -->\n");
            if (debug) {
                writer.println("</fieldset>");
            }
        } catch (IOException e) {
            logger.error("IOException rendering the menu", e);
        }
        catch (JahiaInitializationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        kind = null;
        expandOnlyPageInPath = true;
        onlyTop = false;
        reqLevel = -1;
        startLevel = -1;
        dispNumber = -1;
        maxDepth = -1;
        startPid = -1;
        jData = null;
        containerListName = null;
        pageFieldName = null;
        separatorName = null;
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
        editMenuAtEnd = true;
        if (display) {
            bodyContent.clearBody();
            pageContext.popBody();
        }
        display = true;
        if (var!=null) {
            pageContext.removeAttribute(var);
        }
        var = "navMenuBeanSet";
        pageContext.removeAttribute(INTERNAL_CACHE_DEPENDENCIES);
        bodyContent = null;
        super.resetState();
        return EVAL_PAGE;
    }

    public class NavMenuItemBean implements Comparable<NavMenuItemBean> {
        private String title="";
        private String url="";
        private String separator="";
        private String actionMenu="";
        private int level=0;
        private boolean firstInLevel=false;
        private boolean lastInLevel=false;
        private String actionMenuList="";
        private boolean inPath=false;
        private boolean selected=false;
        private boolean markedForDelete = false;
        private String displayLink = "";
        private int itemCount=0;
        private boolean actionMenuOnly = false;

        public String getSeparator() {
            return separator;
        }

        public void setSeparator(String separator) {
            this.separator = separator;
        }

        public boolean isActionMenuOnly() {
            return actionMenuOnly;
        }

        public void setActionMenuOnly(boolean actionMenuOnly) {
            this.actionMenuOnly = actionMenuOnly;
        }

        public int getItemCount() {
            return itemCount;
        }

        public void setItemCount(int itemCount) {
            this.itemCount = itemCount;
        }

        public String getActionMenu() {
            return actionMenu;
        }

        public void setActionMenu(String actionMenu) {
            this.actionMenu = actionMenu;
        }

        public String getActionMenuList() {
            return actionMenuList;
        }

        public void setActionMenuList(String actionMenuList) {
            this.actionMenuList = actionMenuList;
        }

        public String getDisplayLink() {
            return displayLink;
        }

        public void setDisplayLink(String displayLink) {
            this.displayLink = displayLink;
        }

        public boolean isFirstInLevel() {
            return firstInLevel;
        }

        public void setFirstInLevel(boolean firstInLevel) {
            this.firstInLevel = firstInLevel;
        }

        public boolean isInPath() {
            return inPath;
        }

        public void setInPath(boolean inPath) {
            this.inPath = inPath;
        }

        public boolean isLastInLevel() {
            return lastInLevel;
        }

        public void setLastInLevel(boolean lastInLevel) {
            this.lastInLevel = lastInLevel;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public boolean isMarkedForDelete() {
            return markedForDelete;
        }

        public void setMarkedForDelete(boolean markedForDelete) {
            this.markedForDelete = markedForDelete;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public NavMenuItemBean() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NavMenuItemBean)) return false;

            NavMenuItemBean that = (NavMenuItemBean) o;

            if (firstInLevel != that.firstInLevel) return false;
            if (inPath != that.inPath) return false;
            if (lastInLevel != that.lastInLevel) return false;
            if (level != that.level) return false;
            if (markedForDelete != that.markedForDelete) return false;
            if (selected != that.selected) return false;
            if (!actionMenu.equals(that.actionMenu)) return false;
            if (!actionMenuList.equals(that.actionMenuList)) return false;
            if (!displayLink.equals(that.displayLink)) return false;
            if (!title.equals(that.title)) return false;
            if (!url.equals(that.url)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = title.hashCode();
            result = 31 * result + url.hashCode();
            result = 31 * result + actionMenu.hashCode();
            result = 31 * result + level;
            result = 31 * result + (firstInLevel ? 1 : 0);
            result = 31 * result + (lastInLevel ? 1 : 0);
            result = 31 * result + actionMenuList.hashCode();
            result = 31 * result + (inPath ? 1 : 0);
            result = 31 * result + (selected ? 1 : 0);
            result = 31 * result + (markedForDelete ? 1 : 0);
            result = 31 * result + displayLink.hashCode();
            return result;
        }

        public int compareTo(NavMenuItemBean navMenuB) throws ClassCastException {
           return getTitle().compareTo(navMenuB.getTitle());
        };
    }
}


