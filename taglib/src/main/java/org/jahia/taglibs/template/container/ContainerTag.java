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
package org.jahia.taglibs.template.container;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Category;
import org.apache.struts.taglib.TagUtils;
import org.jahia.ajax.gwt.client.widget.actionmenu.actions.ActionMenuIcon;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.beans.TemplatePathResolverBean;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.gui.HTMLToolBox;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.ContainerHTMLCache;
import org.jahia.services.cache.ContainerHTMLCacheEntry;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.pages.JahiaPage;
import org.jahia.settings.SettingsBean;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.containerlist.AbsoluteContainerListTag;
import org.jahia.taglibs.template.containerlist.ContainerListTag;
import org.jahia.taglibs.uicomponents.actionmenu.ActionMenuOutputter;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.LoopTagStatus;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.util.*;


/**
 * Class ContainerTag : retrieves the container list which it belongs to, loops through the containers and displays the
 * fields of each container
 *
 * @author Jerome Tamiotti
 * @jsp:tag name="container" body-content="tagdependent" description="retrieves the container list which it belongs to,
 * loops through the containers and displays the fields of each container. <p><attriInfo>In order to display containers,
 * we use the container tag, whose content will be processed as many times as there are containers in the container
 * list. We also insert the current container as a bean with an ID, so that we can list the actions buttons using the <a
 * href='actionMenu.html' target='tagFrame'>jahiaHtml:actionMenu</a> tag for example. </attriInfo>"
 */

@SuppressWarnings("serial")
public class ContainerTag extends AbstractJahiaTag implements ContainerCache {
    private static transient final Category logger = org.apache.log4j.Logger.getLogger(ContainerTag.class);
    public static final String CACHETAG = "cachetag";

    private Iterator<JahiaContainer> containers;

    private boolean first = true;
    private boolean last = true;
    private JahiaContainer container = null;
    private JahiaContainer firstContainer = null;
    private JahiaContainerList containerList = null;
    private Set<ContentObjectKey> dependencies = null;
    private int counter = 1;
    private transient JahiaData jData = null;
    private ContainerCache oldCacheTag;
    private boolean currentCache = false;
    private boolean debug = false;
    private boolean initLoop = true;
    private boolean random = false;
    private int randomCounter = 0;
    private Date expirDate;
    private boolean display = true;

    private String cache = Boolean.toString(SettingsBean.getInstance().isOutputContainerCacheActivated());
    private String cacheKey = "";
    private String cacheKeyName = null;
    private String cacheKeyProperty = null;
    private String cacheKeyScope = null;
    private String srcBeanId = null;
    private String requiredFields;
    private boolean displayActionMenu = true;
    private boolean displayActionMenuAtBottom = false;
    private String actionMenuNamePostFix;
    private String actionMenuNameLabelKey;
    private String actionMenuCssClassName;
    private String actionMenuIconStyle;
    private String encapsulatingDivCssClassName = null;
    private boolean displayContainerAnchor = true;
    private boolean displaySkins = true;
    private boolean displayExtensions = true;
    private String expiration;
    private String emptyContainerDivCssClassName = null;

    private String varStatus;

    public boolean displayBody() {
        return display;
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cache) {
        if (!"true".equals(cache) && !"on".equals(cache)) {
            this.cache = "false";
            disableCache();
        } else {
            this.cache = "true";
        }
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public void setCacheKey(Object cacheKey) {
        this.cacheKey = cacheKey.toString();
    }

    public String getCacheKeyName() {
        return cacheKeyName;
    }

    public void setCacheKeyName(String cacheKeyName) {
        this.cacheKeyName = cacheKeyName;
    }

    public String getCacheKeyProperty() {
        return cacheKeyProperty;
    }

    public void setCacheKeyProperty(String cacheKeyProperty) {
        this.cacheKeyProperty = cacheKeyProperty;
    }

    public String getCacheKeyScope() {
        return cacheKeyScope;
    }

    public void setCacheKeyScope(String cacheKeyScope) {
        this.cacheKeyScope = cacheKeyScope;
    }

    public String getSrcBeanId() {
        return srcBeanId;
    }

    public void setSrcBeanId(String srcBeanId) {
        this.srcBeanId = srcBeanId;
    }

    public JahiaContainer getContainer() {
        return this.container;
    }

    public ContainerBean getContainerBean() {
        if (this.container == null) {
            return null;
        }
        return new ContainerBean(this.container, jData.getProcessingContext());
    }

    public void setRequiredFields(String requiredFields) {
        this.requiredFields = requiredFields;
    }

    public void setDisplayActionMenu(boolean displayActionMenu) {
        this.displayActionMenu = displayActionMenu;
    }

    public void setDisplayActionMenuAtBottom(boolean displayActionMenuAtBottom) {
        this.displayActionMenuAtBottom = displayActionMenuAtBottom;
    }

    public void setActionMenuNamePostFix(String actionMenuNamePostFix) {
        this.actionMenuNamePostFix = actionMenuNamePostFix;
    }

    public void setActionMenuCssClassName(String actionMenuCssClassName) {
        this.actionMenuCssClassName = actionMenuCssClassName;
    }

    public void setEncapsulatingDivCssClassName(String encapsulatingDivCssClassName) {
        this.encapsulatingDivCssClassName = encapsulatingDivCssClassName;
    }

    public void setActionMenuNameLabelKey(String actionMenuNameLabelKey) {
        this.actionMenuNameLabelKey = actionMenuNameLabelKey;
    }

    public void setDisplayContainerAnchor(boolean displayContainerAnchor) {
        this.displayContainerAnchor = displayContainerAnchor;
    }

    public void setActionMenuIconStyle(String actionMenuIconStyle) {
        this.actionMenuIconStyle = actionMenuIconStyle;
    }

    public void setDisplaySkins(boolean displaySkins) {
        this.displaySkins = displaySkins;
    }

    public void setDisplayExtensions(boolean displayExtensions) {
        this.displayExtensions = displayExtensions;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public void setVarStatus(String varStatus) {
        this.varStatus = varStatus;
    }

    public void setEmptyContainerDivCssClassName(String emptyContainerDivCssClassName) {
        this.emptyContainerDivCssClassName = emptyContainerDivCssClassName;
    }

    public int doStartTag() throws JspException {
        pushTag();
        if (cacheKeyName != null) {
            cacheKey = TagUtils.getInstance().lookup(pageContext, cacheKeyName, cacheKeyProperty, cacheKeyScope).toString();
        }
        oldCacheTag = (ContainerCache) pageContext.getAttribute(CACHETAG);
        pageContext.setAttribute(CACHETAG, this);
        initLoop = true;
        dependencies = new HashSet<ContentObjectKey>();

        ServletRequest request = pageContext.getRequest();
        jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

        // format the cacheKey to include AES settings
        final ProcessingContext context = jData.getProcessingContext();
        cacheKey = ContainerHTMLCache.appendAESMode(context, cacheKey);
        ContainerTag containerTag = (ContainerTag) findAncestorWithClass(this, ContainerTag.class);
        if (containerTag != null) {
            cache = "false";
        }
        currentCache = "true".equals(cache);

        ContainerSupport containerSupport = (ContainerSupport) findAncestorWithClass(this, ContainerSupport.class, pageContext.getRequest());
        if (containerSupport instanceof GetContainerTag) {
            setSrcBeanId(((GetContainerTag) containerSupport).getVar());
        }
        if (getSrcBeanId() == null && containerSupport instanceof ContainerListTag) {
            ContainerListTag cListTag = (ContainerListTag) containerSupport;

            if (cListTag.getParent() != null &&
                    cListTag.getParent() instanceof RandomContainerTag) {
                random = true;            
            }
            if (cListTag instanceof AbsoluteContainerListTag) {
                cacheKey = cacheKey + "___absolute___";
            }
            containerList = cListTag.getContainerList();
            if (containerList == null) {
                return SKIP_BODY;
            }

            if (initLoop) {
                // here is stuff we need to do only once...
                // reads the containers of this list
                containers = containerList.getContainers();
                if (containers.hasNext()) {
                    this.container = (JahiaContainer) containers.next();
                    addPageDependency(container.getPageID());
                    if (getId() != null) {
                        pageContext.setAttribute(getId(), getContainerBean());
                        pageContext.setAttribute(getId(), getContainerBean(), PageContext.REQUEST_SCOPE);
                    }
                    this.firstContainer = this.container;
                    if (containers.hasNext()) {
                        this.last = false;
                    }
                } else {
                    this.counter = 0;
                    if (!cacheKey.endsWith("___absolute___")) {
                        this.display = false;
                    } else {
                        addContainerListDependency(containerList.getID());
                    }
                    // If no container in containerList, and emptyContainerDivCssClassName is set, add a <div></div> to show a sample of content using css.
                    if (ProcessingContext.EDIT.equals(context.getOperationMode()) && emptyContainerDivCssClassName != null && emptyContainerDivCssClassName.length() > 0) {
                        try {
                            pageContext.getOut().print("<div class=\"" + emptyContainerDivCssClassName + "\"></div>");
                        } catch (IOException e) {
                            logger.error(e, e);
                        }
                    }
                }
                containers = containerList.getContainers();
                initLoop = false;
                if (random && !ProcessingContext.EDIT.equals(context.getOperationMode())) {
                    try {
                        pageContext.getOut().print("<div style=\"display:none\" id=\"randomC" + this.hashCode() + "1\">");
                    } catch (IOException e) {
                        logger.error(e, e);
                    }
                }
                return processNextContainer(pageContext.getOut());
            }
        } else {
            if (getSrcBeanId() != null) {
                ContainerBean containerBean = (ContainerBean) pageContext.getAttribute(getSrcBeanId());
                if (containerBean != null) {
                    this.container = containerBean.getJahiaContainer();
                    addContainerListDependency(container.getListID());
                    addPageDependency(container.getPageID());
                    if (getId() != null) {
                        pageContext.setAttribute(getId(), containerBean);
                        pageContext.setAttribute(getId(), containerBean, PageContext.REQUEST_SCOPE);
                    }
                }
            }
        }
        return EVAL_BODY_BUFFERED;
    }

    private int processNextContainer(JspWriter out) throws JspTagException {
        while (containers.hasNext()) {
            this.container = (JahiaContainer) containers.next();

            if (getId() != null) {
                pageContext.setAttribute(getId(), getContainerBean());
                pageContext.setAttribute(getId(), getContainerBean(), PageContext.REQUEST_SCOPE);
            }
            // maybe not the first one anymore
            this.first = firstContainer == container;
            // checks if it's the last one
            if (!containers.hasNext()) {
                this.last = true;
            }

            if (varStatus != null && varStatus.length() > 0) {
                pageContext.setAttribute(varStatus, new LoopTagStatusImpl());
            }

            ServletRequest request = pageContext.getRequest();
            jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

            final ProcessingContext context = jData.getProcessingContext();
            boolean cacheOff = (!ProcessingContext.NORMAL.equals(context.getOperationMode()) && Jahia.getSettings().isContainerCacheLiveModeOnly())
                    || (context.getEntryLoadRequest() != null && context.getEntryLoadRequest().isVersioned());

            currentCache = "true".equals(cache) && !cacheOff;

            debug = "debug".equals(context.getParameter(ProcessingContext.CONTAINERCACHE_MODE_PARAMETER));
            if (debug && !(cacheKey.indexOf("___debug___") > 0)) cacheKey = cacheKey + "___debug___";

            if (currentCache) {
                try {
                    String containerContent = getFromContainerCache(container, jData);
                    if (containerContent != null) {
                        if ((this.display)) {
                            try {
                                ContainerCacheTag.writeOutFromCache(out, containerContent, debug, container, cacheKey,
                                        context.getSiteURL(context.getSite(), context.getPageID(), false, true, true),
                                        pageContext, expirDate);
                            } catch (IOException ioe) {
                                logger.error("Error displaying container output", ioe);
                                throw new JspTagException();
                            }
                        }
                        continue;
                    }

                } catch (JahiaInitializationException jie) {
                    logger.error("Error initializing container rendering", jie);
                    throw new JspTagException();
                }
            }
            return EVAL_BODY_BUFFERED;
        }
        if (cacheKey.endsWith("___absolute___") && container == null) {
            ProcessingContext context = jData.getProcessingContext();
            try {
                writeToContainerCache(container, jData, "");
                ContainerCacheTag.writeOut(out, "", currentCache, debug, container, cacheKey,
                        context.getSiteURL(context.getSite(), context.getPageID(), false, true, true),
                        pageContext);
            } catch (IOException e) {
                logger.error(e, e); //To change body of catch statement use File | Settings | File Templates.
            } catch (JahiaInitializationException e) {
                logger.error(e, e);  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return SKIP_BODY;
    }

    // loops through the next elements
    public int doAfterBody() throws JspException {
        if (this.display) {
            try {
                final String bodyContent = getBodyContent().getString();
                if (bodyContent != null && bodyContent.length() > 0 && hasContainerAllRequiredFields(this.container,
                        requiredFields, jData.getProcessingContext())) {

                    StringBuffer buf = new StringBuffer();
                    if (this.container.getContentContainer().isMarkedForDelete()) {
                        if (encapsulatingDivCssClassName != null && encapsulatingDivCssClassName.length() > 0) {
                            encapsulatingDivCssClassName += " markedForDelete";
                        } else {
                            encapsulatingDivCssClassName = "markedForDelete";
                        }
                    }
                    if (encapsulatingDivCssClassName != null && encapsulatingDivCssClassName.length() > 0) {
                        buf.append("<div class=\"");
                        buf.append(encapsulatingDivCssClassName);
                        buf.append("\">");
                    }
                    ProcessingContext context = jData.getProcessingContext();
                    boolean shouldWeDisplayActionMenus = displayActionMenu &&
                            ProcessingContext.EDIT.equals(context.getOpMode()) &&
                            ((this.id != null && this.id.length() > 0) || this.container != null);


                    if (displayContainerAnchor) {
                        buf.append(HTMLToolBox.drawAnchor(container, false));
                    }

                    if (shouldWeDisplayActionMenus) {
                        final StringBuffer menu = new StringBuffer();
                        if (actionMenuCssClassName != null) {
                            menu.append("<div class=\"").append(actionMenuCssClassName).append("\">");
                        } else {
                            menu.append("<div class=\"" + ActionMenuOutputter.CONTAINER_DEFAULT_CSS + "\">");
                        }
                        StringBuffer bodyContentTmp = new StringBuffer(bodyContent);

                        if (displaySkins || displayExtensions) {
                            bodyContentTmp = skinnify((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(), bodyContentTmp);
                        }

                        if (displayActionMenuAtBottom) {
                            menu.append(bodyContentTmp);
                        }

                        try {
                            if (actionMenuNameLabelKey != null && actionMenuNameLabelKey.length() == 0) {
                                actionMenuNameLabelKey = null;
                            }
                            final String actionMenu;
                            if (this.id != null && this.id.length() > 0) {
                                actionMenu = new ActionMenuOutputter(context, pageContext, null, this.id,
                                        null, ActionMenuIcon.CONTAINER_EDIT, getResourceBundle(), actionMenuNamePostFix, actionMenuNameLabelKey, actionMenuIconStyle).
                                        getOutput();
                            } else {
                                actionMenu = new ActionMenuOutputter(context, pageContext, null, null,
                                        "ContentContainer_" + this.container.getID(), ActionMenuIcon.CONTAINER_EDIT, getResourceBundle(),
                                        actionMenuNamePostFix, actionMenuNameLabelKey, actionMenuIconStyle).getOutput();
                            }
                            if (actionMenu != null && actionMenu.length() > 0) {
                                menu.append(actionMenu);
                                if (!displayActionMenuAtBottom) {
                                    menu.append(bodyContentTmp);
                                }
                                buf.append(menu);
                            } else {
                                shouldWeDisplayActionMenus = false;
                            }

                        } catch (Exception e) {
                            logger.error("Error while generating Action menu", e);
                        }
                    }
                    if (!shouldWeDisplayActionMenus) {
                        buf.append(bodyContent);
                        if (displaySkins || displayExtensions) {
                            buf = skinnify((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(), buf);
                        }
                    }

                    if (shouldWeDisplayActionMenus) {
                        buf.append("</div> <!-- action menus -->");
                    }
                    if (encapsulatingDivCssClassName != null && encapsulatingDivCssClassName.length() > 0) {
                        buf.append("</div><!-- css user defined -->");
                    }

                    String content = buf.toString().trim();
                    if (currentCache) {
                        writeToContainerCache(container, jData, content);
                    }
                    if (random && !ProcessingContext.EDIT.equals(context.getOperationMode())) {
                        if (randomCounter == 0)
                            randomCounter = 1;
                        buf.append("</div> <!-- random count -->");
                        if (this.getSrcBeanId() == null && containers.hasNext()) {
                            randomCounter++;
                            buf.append("<div style=\"display:none\" id=\"randomC").append(this.hashCode()).append(randomCounter).append("\">");


                        }
                    }
                    ContainerCacheTag.writeOut(getPreviousOut(), content, currentCache, debug, container, cacheKey,
                            context.getSiteURL(context.getSite(), context.getPageID(), false, true, true),
                            pageContext);
                }
                getBodyContent().clear();
                this.counter++;
            } catch (IOException ioe) {
                logger.error("Error displaying container output", ioe);
                throw new JspTagException();
            } catch (JahiaInitializationException jie) {
                logger.error("Error displaying container output", jie);
                throw new JspTagException();
            } catch (JahiaException e) {
                logger.error("Error displaying container output", e);
                throw new JspTagException();
            }
            if (this.getSrcBeanId() == null && containers.hasNext()) {
                return processNextContainer(getPreviousOut());
            } else {
                this.display = false;
            }
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        super.doEndTag();
        containers = null;
        srcBeanId = null;
        requiredFields = null;
        final ProcessingContext jParams = Jahia.getThreadParamBean();
        if (random && !ProcessingContext.EDIT.equals(jParams.getOpMode())) {
            pageContext.getRequest().setAttribute("randomCounter", randomCounter);
            pageContext.getRequest().setAttribute("randomName", String.valueOf(this.hashCode()));
        }
        try {
            if (random && !ProcessingContext.EDIT.equals(jParams.getOpMode()) && randomCounter == 0)
                pageContext.getOut().print("</div><!-- random count (final) -->");
        } catch (IOException e) {
            logger.error(e, e);
        }
        if (getId() != null) {
            pageContext.removeAttribute(getId(), PageContext.PAGE_SCOPE);
            pageContext.removeAttribute(getId(), PageContext.REQUEST_SCOPE);
        }
        resetState();
        popTag();
        return EVAL_PAGE;
    }

    public static void addContainerListDependency(PageContext pageContext, int listId) {
        ContainerCache oldCacheTag = (ContainerCache) pageContext.getAttribute(CACHETAG);
        if (oldCacheTag != null) {
            oldCacheTag.addContainerListDependency(listId);
        }
    }

    public void addContainerListDependency(int listId) {
        dependencies.add(new ContentContainerListKey(listId));
        final ContainerCache ancestor = (ContainerCache) findAncestorWithClass(this, ContainerCache.class, pageContext.getRequest());
        if (ancestor != null) ancestor.addContainerListDependency(listId);
    }

    public void addPageDependency(int pageId) {
        dependencies.add(new ContentPageKey(pageId));
        final ContainerCache ancestor = (ContainerCache) findAncestorWithClass(this, ContainerCache.class, pageContext.getRequest());
        if (ancestor != null) ancestor.addPageDependency(pageId);
    }

    public static void addContainerDependency(PageContext pageContext, int containerId) {
        ContainerCache oldCacheTag = (ContainerCache) pageContext.getAttribute(CACHETAG);
        if (oldCacheTag != null) {
            oldCacheTag.addContainerListDependency(containerId);
        }
    }

    public void addContainerDependency(int containerId) {
        dependencies.add(new ContentContainerKey(containerId));
        final ContainerCache ancestor = (ContainerCache) findAncestorWithClass(this, ContainerCache.class, pageContext.getRequest());
        if (ancestor != null) ancestor.addContainerDependency(containerId);
    }

    public static void disableCache(PageContext pageContext) {
        ContainerCache oldCacheTag = (ContainerCache) pageContext.getAttribute(CACHETAG);
        if (oldCacheTag != null) {
            oldCacheTag.disableCache();
        }
    }

    public void disableCache() {
        currentCache = false;

        ContainerCache ancestor = (ContainerCache) findAncestorWithClass(this, ContainerCache.class, pageContext.getRequest());
        if (ancestor != null) ancestor.disableCache();
//        JahiaData data = (JahiaData) pageContext.getRequest().getAttribute("org.jahia.data.JahiaData");
//        data.getProcessingContext().setCacheExpirationDelay(0);
    }

    private void writeToContainerCache(JahiaContainer jahiaContainer,
                                       JahiaData jahiaData,
                                       String bodyContent) throws JahiaInitializationException {
        if (bodyContent.contains("<!-- cache:include src=")) return;
        ContainerHTMLCache<GroupCacheKey, ContainerHTMLCacheEntry> containerHTMLCache =
                ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
        ProcessingContext processingContext = jahiaData.getProcessingContext();
        String mode = jahiaData.getProcessingContext().getOperationMode();
        // Get the language code
        String curLanguageCode = processingContext.getLocale().toString();

        GroupCacheKey containerKey =
                ServicesRegistry.getInstance().getCacheKeyGeneratorService().computeContainerEntryKeyWithGroups(jahiaContainer,
                        cacheKey,
                        processingContext.getUser(),
                        curLanguageCode,
                        mode,
                        processingContext.getScheme(),
                        dependencies);
        ContainerHTMLCacheEntry containerHTMLCacheEntry = new ContainerHTMLCacheEntry(bodyContent);
        long expirI;
        if (expiration != null && !"".equals(expiration.trim())) {
            try {
                expirI = Long.parseLong(expiration);
            } catch (NumberFormatException e) {
                expirI = org.jahia.settings.SettingsBean.getInstance().getContainerCacheDefaultExpirationDelay();
            }
        } else {
            expirI = org.jahia.settings.SettingsBean.getInstance().getContainerCacheDefaultExpirationDelay();
        }
        if (expirI > 0l) {
            containerHTMLCache.put(containerKey, containerHTMLCacheEntry);

            try {
                containerHTMLCache.getCacheEntry(containerKey).setExpirationDate(new Date(System.currentTimeMillis() + (expirI * 1000)));
            } catch (NumberFormatException e) {
                logger.error("The argument expiration of your tag is not a number", e);
            }
        }
    }

    private String getFromContainerCache(JahiaContainer jahiaContainer,
                                         JahiaData jahiaData
    ) throws JahiaInitializationException, JspTagException {
        ContainerHTMLCache<GroupCacheKey, ContainerHTMLCacheEntry> containerHTMLCache =
                ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
        ProcessingContext processingContext = jahiaData.getProcessingContext();
        if (processingContext.getEntryLoadRequest() != null && processingContext.getEntryLoadRequest().isVersioned()) {
            // we don't cache versioned content
            return null;
        }
        String mode = jahiaData.getProcessingContext().getOperationMode();
        // Get the language code
        String curLanguageCode = processingContext.getLocale().toString();

        GroupCacheKey containerKey =
                ServicesRegistry.getInstance().getCacheKeyGeneratorService().computeContainerEntryKey(jahiaContainer,
                        cacheKey,
                        processingContext.getUser(),
                        curLanguageCode,
                        mode,
                        processingContext.getScheme());
        CacheEntry<ContainerHTMLCacheEntry> cacheEntry = containerHTMLCache.getCacheEntry(containerKey);
        if (cacheEntry == null)
            return null;
        expirDate = cacheEntry.getExpirationDate();
        ContainerHTMLCacheEntry entry = (ContainerHTMLCacheEntry) cacheEntry.getObject();
        return entry.getBodyContent();
    }

    public static boolean hasContainerAllRequiredFields(final JahiaContainer container,
                                                        final String required,
                                                        final ProcessingContext jParams)
            throws JahiaException {
        if (required == null || required.length() == 0) return true;
        if (jParams.getOpMode().equals(ProcessingContext.EDIT) ||
                jParams.getOpMode().equals(ProcessingContext.COMPARE)) {
            return true;
        }
        final StringTokenizer tokenizer = new StringTokenizer(required, ",");
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken().trim();
            final JahiaField theField = container.getField(token);
            if (theField == null) {
                continue;
            }
            final int fieldType = theField.getType();
            final String value = theField.getValue();
            final Object object = theField.getObject();
            switch (fieldType) {
                case FieldTypes.BIGTEXT:
                case FieldTypes.SMALLTEXT:
                case FieldTypes.SMALLTEXT_SHARED_LANG:
                case FieldTypes.BOOLEAN:
                case FieldTypes.INTEGER:
                case FieldTypes.APPLICATION:
                case FieldTypes.FLOAT:
                    if (value == null || value.length() == 0) {
                        final String defaultValue = theField.getDefinition().getDefaultValue();
                        if (defaultValue == null || defaultValue.length() == 0) {
                            return false;
                        }
                    }
                    break;

                case FieldTypes.DATE:
                    if (value == null || value.length() == 0) {
                        return false;
                    }
                    break;

                case FieldTypes.FILE:
                    if (object == null) {
                        final String defaultValue = theField.getDefinition().getDefaultValue();
                        if (defaultValue == null || defaultValue.length() == 0) {
                            return false;
                        }
                    }
                    break;

                case FieldTypes.PAGE:
                    if (object == null) {
                        return false;
                    }
                    final JahiaPage thePage = (JahiaPage) object;
                    final String pageTitle = thePage.getTitle();
                    if (pageTitle == null || pageTitle.length() == 0) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private StringBuffer skinnify(HttpServletRequest request, HttpServletResponse resp, StringBuffer body)
            throws IOException, JahiaException {
        String skin = "noskin";
        Map<String, String> extensions = new HashMap<String, String>();

        TemplatePathResolverBean templatePath = getJahiaBean().getIncludes().getTemplatePath();
        if (displayExtensions || displaySkins) {
            try {
                String containerType = container.getDefinition().getContainerType();
                if (containerType == null) {
                    return body;
                }
                ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(containerType);
                List<ExtendedNodeType> superTypes = new ArrayList<ExtendedNodeType>(Arrays.asList(nt.getSupertypes()));
                superTypes.addAll(container.getDefinition().getMixinNodeTypes());
                for (ExtendedNodeType superType : superTypes) {
                    if (displayExtensions && superType.isNodeType("jmix:containerExtension") && !superType.getName().equals("jmix:containerExtension")) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("supertype name:" + superType.getName());
                        }
                        String n = StringUtils.substringAfter(superType.getName(), ":");
                        String resolvedPath = templatePath.lookup("extensions/" + n
                                + "/"
                                + JCRContentUtils.cleanUpNodeName(nt.getName())
                                + "/" + n + ".jsp", "extensions/" + n + "/" + n
                                + ".jsp");
    
                        extensions.put(superType.getName(), resolvedPath);
                    }
                    if (displaySkins && !Boolean.TRUE.equals(request.getAttribute("skinned")) && superType.getName().equals("jmix:skinnable")) {
                        JahiaField field = container.getField("skin");
                        if (field != null) {
                            skin = field.getValue();
                            ResourceBundleMarker marker = ResourceBundleMarker.parseMarkerValue(skin);
                            if (marker != null) {
                                skin = marker.getDefaultValue();
                            }
                        }
                    }
                }
            } catch (NoSuchNodeTypeException e) {
                logger.error("NoSuchNodeTypeException ", e);
            } catch (JahiaException e) {
                logger.error("JahiaException in skinnify", e);
            }
        }
        if ("noskin".equals(skin) && extensions.isEmpty()) {
            return body;
        }

        request.setAttribute("includedBody", body.toString());
        request.setAttribute("firstContainer", first);
        request.setAttribute("lastContainer", last);
        request.setAttribute("extensionsPages", extensions);
        request.setAttribute("containerID", container.getID());
        request.setAttribute("definitionName", container.getDefinition().getName());
        if (containerList != null) {
            request.setAttribute("containerListID", containerList.getID());
        }
        request.setAttribute("contentObjectKey", new ContentContainerKey(container.getID()).toString());
        request.setAttribute("skinned", Boolean.TRUE);
        String path = "skins/" + skin + "/" + skin + ".jsp";
        String resolvedPath = templatePath.lookup(path);
        if (resolvedPath == null) {
            return body;
        }

        final StringWriter stringWriter = new StringWriter();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        final boolean[] isWriter = new boolean[1];
        RequestDispatcher rd = request.getRequestDispatcher(resolvedPath);
        try {
            if (rd != null) {
                rd.include(request, new HttpServletResponseWrapper(resp) {
                    @Override
                    public ServletOutputStream getOutputStream() throws IOException {
                        return new ServletOutputStream() {
                            @Override
                            public void write(int i) throws IOException {
                                outputStream.write(i);
                            }
                        };
                    }

                    public PrintWriter getWriter() throws IOException {
                        isWriter[0] = true;
                        return new PrintWriter(stringWriter);
                    }
                });
            }
        } catch (ServletException e) {
            logger.error("Cannot include skin", e);
            return body;
        }
        request.removeAttribute("skinned");
        if(isWriter[0]) {
            return stringWriter.getBuffer();
        }
        else {
            String s = outputStream.toString("UTF-8");
            return new StringBuffer(s);
        }
    }

    @Override
    protected void resetState() {
        random = false;
        randomCounter = 0;

        first = true;
        last = true;
        container = null;
        containerList = null;
        firstContainer = null;
        counter = 1;
        dependencies = null;
        cache = Boolean.toString(SettingsBean.getInstance().isOutputContainerCacheActivated());

        cacheKey = "";

        cacheKeyName = null;
        cacheKeyProperty = null;
        cacheKeyScope = null;

        initLoop = true;
        jData = null;

        pageContext.setAttribute(CACHETAG, oldCacheTag);
        oldCacheTag = null;
        displayActionMenu = true;
        actionMenuNamePostFix = null;
        actionMenuCssClassName = null;
        actionMenuNameLabelKey = null;
        encapsulatingDivCssClassName = null;
        displayContainerAnchor = true;
        displaySkins = true;
        displayExtensions = true;

        display = true;

        expirDate = null;
        if (varStatus != null && varStatus.length() > 0) {
            pageContext.removeAttribute(varStatus);
            varStatus = null;
        }

        super.resetState();
    }

    /*  Loop Tag status implementation */
    class LoopTagStatusImpl implements LoopTagStatus {

        public Object getCurrent() {
            return new ContainerBean(container, getProcessingContext());
        }

        public int getIndex() {
            return counter - 1;
        }

        public int getCount() {
            return counter;
        }

        public boolean isFirst() {
            return first;
        }

        public boolean isLast() {
            return last;
        }

        public Integer getBegin() {
            return null;  //Not supported
        }

        public Integer getEnd() {
            return null;  //Not supported
        }

        public Integer getStep() {
            return null;  //Not supported
        }
    }

}
