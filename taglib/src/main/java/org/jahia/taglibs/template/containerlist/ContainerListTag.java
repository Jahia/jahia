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
package org.jahia.taglibs.template.containerlist;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.portlet.PortletConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.widget.actionmenu.actions.ActionMenuIcon;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerListKey;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.ContainerListBean;
import org.jahia.data.beans.PaginationBean;
import org.jahia.data.beans.TemplatePathResolverBean;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerListPagination;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContainerListFactoryProxy;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.fields.ContentFieldTypes;
import org.jahia.services.fields.JahiaFieldService;
import org.jahia.spring.aop.interceptor.SilentJamonPerformanceMonitorInterceptor;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.container.ContainerCache;
import org.jahia.taglibs.template.container.ContainerSupport;
import org.jahia.taglibs.template.container.ContainerTag;
import org.jahia.taglibs.uicomponents.actionmenu.ActionMenuOutputter;
import org.jahia.taglibs.utility.Utils;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Class ContainerListTag : initializes Jahia in order to display a container list.
 * <p/>
 * A container list is a list of containers; container lists can be imbricated
 * The name of the container list must be changed in this case, so that all the
 * container lists of this page have an unique name
 *
 * @author Jerome Tamiotti
 * @jsp:tag name="containerList" body-content="tagdependent"
 * description="initializes Jahia in order to display a container list; typically by looping through all containers in the container list.
 * <p><attriInfo>This tag is used to display all the contents contained in a containers list. The content enclosed within this tag is
 * processed as many times as there are containers in the current container list.
 * <p/>
 * <p>Note that container lists can be nested.
 * <p/>
 * <p><b>Example 1:</b>
 * <p>&lt;content:containerList name=&quot;guestbookContainer&quot; id=&quot;guestbookContainerList&quot;&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;content:container id=&quot;guestbookContainer&quot;&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;h2&gt;&lt;content:textField name=&quot;title&quot;
 * /&gt;&lt;/h2&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;content:textField name=&quot;name&quot;/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;content:textField name=&quot;email&quot;/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;/content:container&gt;<br>
 * &lt;/content:containerList&gt;</p>
 * <p>This example cycles through all the containers guestbookContainer in the container list guestbookContainerList and displays the title,
 * the name and email for each entry in the guestbook.
 * <p><b>Example 2:</b>
 * <p>&lt;content:containerList name='mainContentContainerList' id=&quot;mainContentContainerList&quot;&gt;
 * <br>&nbsp;&nbsp;&lt;jahiaHtml:actionMenu name=&quot;mainContentContainerList&quot; namePostFix=&quot;Text&quot;
 * resourceBundle=&quot;jahiatemplates.Corporate_portal_templates&quot;&gt;
 * <br>&nbsp;&nbsp;&lt;/jahiaHtml:actionMenu&gt;
 * <br>&lt;/content:containerList&gt;
 * </p>
 * <p>With the above code, the action menu is displayed in Edit mode and enables one to add a container and to edit
 * the container list's properties. Note that it will not display its contents, as we haven't yet included the tags that will
 * display the containers and the fields within the container list.
 * </attriInfo>"
 */

@SuppressWarnings("serial")
public class ContainerListTag extends AbstractJahiaTag implements ContainerSupport {

    private static transient final Logger logger = Logger.getLogger(ContainerListTag.class);

    // we have two versions of these properties because we have to respect the
    // JavaBeans design pattern.
    private int windowSize = Jahia.getSettings().getPaginationWindowSize();
    private int windowOffset = -1;
    private int nbStepPerPage = -1;
    private int maxSize = Integer.MAX_VALUE;
    private String displayPagination = PaginationBean.PAGINATION_AT_BOTTOM;

    private boolean displayActionMenu = true;
    private boolean displayActionMenuAtBottom = false;
    private boolean displaySkins = true;
    private boolean displayExtensions = true;
    private String actionMenuNamePostFix;
    private String actionMenuNameLabelKey;
    private String actionMenuCssClassName;
    private String actionMenuIconStyle;
    private String sortByMetaData;
    private String sortByField;
    private JahiaFieldDefinition sortFieldDef = null;
    private String sortOrder;
    private transient JahiaData jData = null;
    private static final transient Logger monitorLogger = Logger.getLogger(SilentJamonPerformanceMonitorInterceptor.class);
    private transient Monitor mon;

    private String listName = "";
    private String parentListName = "";
    private JahiaContainerList containerList = null;

    /**
     * @jsp:attribute name="id" required="false" rtexprvalue="true"
     * description="id attribute for this tag.
     * <p><attriInfo>Inherited from javax.servlet.jsp.tagext.TagSupport</attriInfo>"
     */


    /**
     * @jsp:attribute name="name" required="true" rtexprvalue="true"
     * description="the name of the list.
     * <p><attriInfo>This is an identifier that must be unique within the current parent object (page or containerlist) CHECK</i> [To Be Completed] </i>,
     * Usually it is good practice to avoid using spaces in this name, which makes manipulation easier in general.
     * See <a href='containerList.html' target='tagFrame'>content:containerList</a>.
     * </attriInfo>"
     */
    public void setName(String name) {
        this.listName = name;
    }

    public String getName() {
        return this.listName;
    }

    /**
     * @jsp:attribute name="size" required="false" rtexprvalue="true"
     * description="the number of elements in the list. Returns the size of the container list.
     * <p><attriInfo>This now returns only the size that has been loaded in memory, as opposed to the full -i.e. real- size of the list
     * set in the database. To get the real full size of data set, use @see JahiaContainerList#getFullSize(). The reason for this
     * is due to the introduction of scrollable container lists which load only the set for the view.
     * </attriInfo>"
     */
    public int getSize() {
        if (this.getContainerList() != null) {
            return this.getContainerList().size();
        }
        return 0;
    }

    /**
     * @jsp:attribute name="windowSize" required="false" rtexprvalue="true"
     * description="The pagination window size i.e. the number of objects on each page.
     * <p><attriInfo> It is possible to allow the user to change the number of items per page used in the Pagination of a given Container List.
     * Say, if a container list contains a 1000 containers/fields, you'll want to spread this list across mutilple pages. If you don't, then
     * enjoy the wait...
     * <p>The default value is -1 meaning the functionality is deactivated. This value can be set in during the declaration of the container
     * list with the @see declareContainerList#windowSize
     * </attriInfo>"
     */
    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    /**
     * @jsp:attribute name="windowOffset" required="false" rtexprvalue="true"
     * description="The pagination window offset.
     * <p><attriInfo>This attribute dictates the initial number of pages into the paginated list for this containerList. The default value is
     * 0.
     * <p>Note that if windowOffset is superior to the number of elements in the container list, it defaults back to a zero value.
     * </attriInfo>"
     */
    public void setWindowOffset(int windowOffset) {
        this.windowOffset = windowOffset;
    }

    public JahiaContainerList getContainerList() {
        return this.containerList;
    }

    public ContainerListBean getContainerListBean() {
        if (getContainerList() == null) {
            return null;
        }
        return new ContainerListBean(getContainerList(), jData.getProcessingContext());
    }

    public void setActionMenuCssClassName(String actionMenuCssClassName) {
        this.actionMenuCssClassName = actionMenuCssClassName;
    }

    public void setActionMenuNameLabelKey(String actionMenuNameLabelKey) {
        this.actionMenuNameLabelKey = actionMenuNameLabelKey;
    }

    public void setActionMenuNamePostFix(String actionMenuNamePostFix) {
        this.actionMenuNamePostFix = actionMenuNamePostFix;
    }

    public void setDisplayActionMenu(boolean displayActionMenu) {
        this.displayActionMenu = displayActionMenu;
    }

    public void setDisplayActionMenuAtBottom(boolean displayActionMenuAtBottom) {
        this.displayActionMenuAtBottom = displayActionMenuAtBottom;
    }

    public void setActionMenuIconStyle(String actionMenuIconStyle) {
        this.actionMenuIconStyle = actionMenuIconStyle;
    }

    public void setNbStepPerPage(int nbStepPerPage) {
        this.nbStepPerPage = nbStepPerPage;
    }

    public void setDisplayPagination(String displayPagination) {
        this.displayPagination = displayPagination;
    }

    /**
     * @jsp:attribute name="sortByMetaData" required="false" rtexprvalue="true"
     * description="This allows to specify an automatic sort based on a metadata field. This could still be overriden by
     * the user on per container lists basis. This will allow you to predefine an automatic sort."
     * @param sortByMetaData the metadata name
     */
    public void setSortByMetaData (String sortByMetaData) {
        this.sortByMetaData = sortByMetaData;
    }
    /**
     * @jsp:attribute name="sortByField" required="false" rtexprvalue="true"
     * description="This allows to specify an automatic sort based on a field of your definition. This could still be overriden by
     * the user on per container lists basis. This will allow you to predefine an automatic sort."
     * @param sortByField the field name inside the definition
     */
    public void setSortByField (String sortByField) {
        this.sortByField = sortByField;
    }
    /**
     * @jsp:attribute name="sortOrder" required="false" rtexprvalue="true"
     * description="This allows to specify the sort order ascending or descending. Ascending by default."
     * @param sortOrder the sort order "asc" or "desc"
     */
    public void setSortOrder (String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public int doStartTag() throws JspException {
        
        pushTag();
        if (monitorLogger.isDebugEnabled()) {
            mon = MonitorFactory.start("org.jahia.taglibs.container.ContainerListTag/" + getName());
        } else {
            mon = null;
        }

        ServletRequest request = pageContext.getRequest();
        jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

        if (getName() != null && getName().length() > 0) {
            ContainerTag parentContainerTag = (ContainerTag) findAncestorWithClass(
                    this, ContainerTag.class, request);
            final JahiaContainer parentContainer = parentContainerTag != null ? parentContainerTag
                    .getContainer()
                    : (JahiaContainer) request.getAttribute("parentContainer");
            
            if (parentContainer != null) {
                try {
                    this.parentListName = parentContainer.getDefinition()
                            .getName();
                    if (!retrieveContainerList(parentContainer)) {
                        return SKIP_BODY;
                    }
                } catch (JahiaException je) {
                    logger.error(
                            "Error while retrieving container list for parent container "
                                    + parentContainer.getID(), je);
                }
            } else {
                // we found no parent, let's try to load a top-level container list.
                try {
                    if (!retrieveContainerList()) {
                        return SKIP_BODY;
                    }
                } catch (JahiaException je) {
                    logger.error("Error:", je);
                }
            }
        }
        if (getId() != null) {
            if (getContainerListBean() != null) {
                pageContext.setAttribute(getId(), getContainerListBean());
            } else {
                pageContext.removeAttribute(getId(), PageContext.PAGE_SCOPE);
            }
        }
        if (getContainerList() != null) {
            final ContainerCache ancestor = (ContainerCache) findAncestorWithClass(this, ContainerCache.class, request);
            if (ancestor != null) {
                ancestor.addContainerListDependency(getContainerList().getID());
            }
        }
        if (getContainerList() == null) {
            setContainerList(new JahiaContainerList(-1, -1, -1, -1, 0));
            getContainerList().setFactoryProxy(new ContainerListFactoryProxy(LoadFlags.ALL,
                    jData.getProcessingContext(),
                    jData.getProcessingContext().getEntryLoadRequest(),
                    null,
                    null,
                    null));
        }
        boolean shouldWeDisplayActionMenus = displayActionMenu && ProcessingContext.EDIT.equals(jData.getProcessingContext().getOpMode());
        try {
            final StringBuilder buff = new StringBuilder();
            if (shouldWeDisplayActionMenus
                    && (getContainerList() != null || (this.id != null && this.id
                            .length() > 0))) {
                final StringBuilder menu = new StringBuilder();
                if (actionMenuCssClassName != null) {
                    menu.append("<div class=\"").append(actionMenuCssClassName).append("\">");
                } else {
                    menu.append("<div class=\"" + ActionMenuOutputter.CONTAINERLIST_DEFAULT_CSS + "\">");
                }
                if (!displayActionMenuAtBottom) {
                    if (actionMenuNameLabelKey != null && actionMenuNameLabelKey.length() == 0) {
                        actionMenuNameLabelKey = null;
                    }
                    final String actionMenu;
                    if (this.id != null && this.id.length() > 0) {
                        if (getContainerList().getDefinition().getContainerListType() != JahiaContainerDefinition.SINGLE_TYPE || getContainerList().size() == 0) {
                            actionMenu = new ActionMenuOutputter(jData.getProcessingContext(), pageContext, null, this.id, null, ActionMenuIcon.CONTAINERLIST_UPDATE, getResourceBundle(), actionMenuNamePostFix, actionMenuNameLabelKey, actionMenuIconStyle).getOutput();
                        }
                        else {
                               actionMenu = null;
                        }
                    } else {
                        actionMenu = new ActionMenuOutputter(jData.getProcessingContext(), pageContext, null, null, "ContentContainerList_" + getContainerList().getID(), ActionMenuIcon.CONTAINERLIST_UPDATE, getResourceBundle(), actionMenuNamePostFix, actionMenuNameLabelKey, actionMenuIconStyle).getOutput();
                    }

                    if (actionMenu != null && actionMenu.length() > 0) {
                        menu.append(actionMenu);
                        buff.append(menu);
                    } else {
                        displayActionMenu = false;
                    }
                }
            }
            if (displayPagination.contains(PaginationBean.PAGINATION_ON_TOP)) {
                buff.append(displayPagination());
            }
            final JspWriter out = pageContext.getOut();
            out.println(buff.toString());
        } catch (Exception e) {
            logger.error("Error in containerList tag", e);
        }
        return EVAL_BODY_BUFFERED;
    }
    private void setSizeAndOffsetSettings () {
        getContainerList().setMaxSize(getMaxSize());
        JahiaContainerListPagination pagination = getContainerList()
                .getCtnListPagination(false);
        if (pagination == null || !pagination.isValid()
                || pagination.getWindowSize() != getWindowSize()) {
            if (jData.getProcessingContext().getParameter(getWindowSizeKey()) == null) {
                jData.getProcessingContext().setParameter(getWindowSizeKey(),
                        String.valueOf(getWindowSize()));
                getContainerList().setIsContainersLoaded(false);
            }
        }
        if (pagination == null || !pagination.isValid()
                || pagination.getWindowOffset() != getWindowOffset()) {
            if (jData.getProcessingContext().getParameter(getWindowOffsetKey()) == null) {
                jData.getProcessingContext().setParameter(getWindowOffsetKey(),
                        String.valueOf(getWindowOffset()));
                getContainerList().setIsContainersLoaded(false);
            }
        }
    }
    
    public String getWindowSizeKey () {
        return (getId() != null && getId().length() > 0? getId() + "_": "") + getName() + "_windowsize";
    }

    public String getWindowOffsetKey () {
        return (getId() != null && getId().length() > 0 ? getId() + "_": "") + getName() + "_windowoffset";
    }
    
    public JahiaFieldDefinition getSortFieldDefinition(String sortByField,
            String sortByMetaData) throws JahiaException {
        JahiaFieldDefinition definition = sortFieldDef;
        if (sortFieldDef == null) {
            if (sortByField != null && !"".equals(sortByField.trim())) {
                final JahiaContainerDefinition jahiaContainerDefinition = getContainerList()
                        .getDefinition();
                definition = jahiaContainerDefinition
                        .findFieldInStructure(jahiaContainerDefinition
                                .getName()
                                + "_" + sortByField);

            } else if (sortByMetaData != null
                    && !"".equals(sortByMetaData.trim())) {
                final JahiaFieldService service = ServicesRegistry
                        .getInstance().getJahiaFieldService();
                final List<Integer> integerList = service
                        .loadFieldDefinitionIds(sortByMetaData, true);
                if (integerList.size() == 1) {
                    definition = service
                            .loadFieldDefinition(integerList.get(0));
                }
            }
            sortFieldDef = definition;
        }
        return definition;
    }
    
    private void setSortSettings() throws JahiaException {
        if (getContainerList() != null) {
            try {
                JahiaFieldDefinition definition = getSortFieldDefinition(sortByField, sortByMetaData);
                if (definition != null) {
                    boolean ignoreOptimizedMode = (definition.getType() == FieldTypes.PAGE
                            || definition.getType() == FieldTypes.BIGTEXT || definition
                            .getType() == FieldTypes.FILE);
                    String fieldName = definition.getName();
                    int fieldType = definition.getType();
                    String useOptimizedMode = definition.getIsMetadata() ? Boolean.TRUE
                            .toString()
                            : Boolean.FALSE.toString();
                    boolean isMetaData = definition.getIsMetadata();

                    if (fieldName != null) {
                        String sort = "asc";
                        if (sortOrder != null
                                && sortOrder.trim().toLowerCase().startsWith(
                                        "desc")) {
                            sort = "desc";
                        }
                        StringBuffer buff = new StringBuffer(fieldName);
                        buff.append(";").append(sort);
                        switch (fieldType) {
                            case ContentFieldTypes.DATE:
                            case ContentFieldTypes.INTEGER:
                            case ContentFieldTypes.FLOAT:
                                buff.append(";true");
                                break;
                            default:
                                buff.append(";false");
                        }
                        buff.append(";").append(isMetaData);
                        getContainerList().setProperty(
                                "automatic_sort_ignoreOptimizedMode",
                                String.valueOf(ignoreOptimizedMode));
                        getContainerList().setProperty(
                                "automatic_sort_handler", buff.toString());
                        getContainerList().setProperty(
                                "automatic_sort_useOptimizedMode",
                                useOptimizedMode);
                    }
                }
            } catch (JahiaException e) {
                logger.error("Try to define a sort on a non existing field "
                        + sortByField + " " + sortByMetaData, e);
            }
        }
    }

    private void checkSubdefinition(ProcessingContext processingContext) throws JahiaException {
        boolean isSubdef = ServicesRegistry.getInstance().getJahiaContainersService().hasContainerDefinitionParents(getContainerList().getctndefid());
        if (!isSubdef && ProcessingContext.EDIT.equals(processingContext.getOperationMode())) {
            try {
                pageContext.getOut().append("<font color=\"red\">The container list ").append(listName).append(" is not a subdefiniton of ").append(parentListName).append(" but is use as is, this will lead to import/export/copy/paste issues, please correct your definitions in your template</font>");
            } catch (IOException e) {
                logger.error("IO error during writing of error message in html output", e);
            }
            logger.warn("The container list " + listName + " is not a subdefiniton of " + parentListName + " but is use as is, this will lead to import/export/copy/paste issues, please correct your definitions in your template");
        }
    }

    private boolean retrieveContainerList(JahiaContainer parentContainer) throws JahiaException {
        JahiaContainerList list = parentContainer.getContainerList(getName());
        if (list == null) {
            // might be in a portlet
            PortletConfig portletConfig = (PortletConfig) pageContext.getRequest().getAttribute("javax.portlet.config");
            if (portletConfig != null) {
                String uniqueName = Utils.buildUniqueName(portletConfig.getPortletName(), getName());
                list = parentContainer.getContainerList(uniqueName);
            }
            if (list == null) {
                /** todo FIXME this should be deprecated but is left
                 * here because some databases might be corrupted.
                 */
                String uniqueName = Utils.buildUniqueName(parentListName, getName());
                logger.warn("Using legacy method to find sub-container list " + uniqueName);
                list = parentContainer.getContainerList(uniqueName);
            }
        }
        if (list != null && list.getID() == 0) {
            list = jData.containers().ensureContainerList(list.getDefinition(), parentContainer.getPageID(), parentContainer.getID(), getId());
        }
        if (list != null) {
            list.getFactoryProxy().setListViewId(getId());
            setContainerList(list);
            setSortSettings();
            setSizeAndOffsetSettings();
            // Output a warning in html and in console if this container list is not a subdefintion of its parent
            checkSubdefinition(jData.getProcessingContext());
        } else {
            logger.debug("ContainerList is null: " + getName());
            displayActionMenu = false;
            return false;
        }
        return true;
    }

    // Body is evaluated one time, so just writes it on standard output
    public int doAfterBody() {
        final ProcessingContext jParams = Jahia.getThreadParamBean();
        final boolean shouldWeDisplayActionMenus = displayActionMenu && ProcessingContext.EDIT.equals(jData.getProcessingContext().getOpMode());
        StringBuilder buf = new StringBuilder().append(getBodyContent().getString());
        if (containerList.getID() > 0 && (displaySkins || displayExtensions)) {
            buf = skinnify((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(), buf);
        }

        if (shouldWeDisplayActionMenus) {
            try {
                if (displayActionMenuAtBottom) {
                    if (actionMenuNameLabelKey != null && actionMenuNameLabelKey.length() == 0) {
                        actionMenuNameLabelKey = null;
                    }
                    if (this.id != null && this.id.length() > 0) {
                        buf.append(new ActionMenuOutputter(jParams, pageContext, null, this.id, null, ActionMenuIcon.CONTAINERLIST_UPDATE, getResourceBundle(), actionMenuNamePostFix, actionMenuNameLabelKey, actionMenuIconStyle).getOutput());

                    } else if (this.containerList != null) {
                        buf.append(new ActionMenuOutputter(jParams, pageContext, null, null, "ContentContainerList_" + this.containerList.getID(), ActionMenuIcon.CONTAINERLIST_UPDATE, getResourceBundle(), actionMenuNamePostFix, actionMenuNameLabelKey, actionMenuIconStyle).getOutput());
                    }
                }
            } catch (Exception e) {
                logger.error("Error while generating Action menu", e);
            }

        }
        try {
            if (displayPagination.contains(PaginationBean.PAGINATION_AT_BOTTOM)) {
                buf.append(displayPagination());
            }

            if (shouldWeDisplayActionMenus) buf.append("</div><!-- end display action menu -->");

            final JspWriter out = getPreviousOut();
            out.append(buf.toString());
        } catch (IOException e) {
            logger.error("Error while writing to JSP: " + e.getMessage(), e);
        }

        return SKIP_BODY;
    }

    // reads the container list from a container set
    protected boolean retrieveContainerList() throws JahiaException {
        JahiaContainerList list = getContainerList(jData, getName());
        if (list != null && list.getID() != 0 && list.getFactoryProxy() != null ) {
            list.getFactoryProxy().setListViewId(getId());
            setContainerList(list);
            setSortSettings();
            setSizeAndOffsetSettings();
        } else {
            logger.debug("ContainerList is null: " + getName());
            displayActionMenu = false;
            return false;
        }
        return true;
    }
    
    // reads the container list from a container set
    protected JahiaContainerList getContainerList(JahiaData jData, String listName) throws JahiaException {
        if(listName ==null || "".equals(listName)) return null;
        return jData.containers().getContainerList(listName, getId());
    }    

    public int doEndTag() throws JspException {
        resetState();

        if (mon != null) mon.stop();
        popTag();
        return EVAL_PAGE;
    }


    public int getMaxSize() {
        return maxSize;
    }

    /**
     * @jsp:attribute name="maxSize" required="false" rtexprvalue="true"
     * description="The max size.
     * <p><attriInfo>This attribute can be used to set the maximum size of the container list ( the max containers to load ).
     * The default value is Integer.MAX_VALUE
     * </attriInfo>"
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    protected String getScrollingValueInput(JahiaContainerList containerList,
                                            String currentScrollingValue)
            throws JahiaException {
        StringBuffer buff = new StringBuffer("<input type='hidden' name='");
        buff.append(ProcessingContext.CONTAINER_SCROLL_PREFIX_PARAMETER);
        buff.append(getId() != null ? getId() + "_" : "");
        buff.append(containerList.getDefinition().getName());
        buff.append("' value='");
        buff.append(currentScrollingValue);
        buff.append("'>\n");
        buff.append("<input type='hidden' name='");
        buff.append("ctnlistpagination_");
        buff.append(containerList.getDefinition().getName());
        buff.append("' value='false'>");
        return buff.toString();
    }

    protected String getNextRangeOfPages(int pageNumber,
                                         int startPageIndex,
                                         int stopPageIndex,
                                         int currentPageIndex,
                                         int nbPages,
                                         int nbStepPerPage,
                                         int windowSize) throws JahiaException {

        if (stopPageIndex < nbPages) {
            final StringBuffer buf = new StringBuffer();

            final String url = jData.gui().drawContainerListNextWindowPageURL(
                    getContainerList(),
                    (startPageIndex > 1 ? stopPageIndex : nbStepPerPage) + 1
                            - currentPageIndex, windowSize, false, getId());

            if (url != null && url.length() > 0) {
                buf.append("<a class=\"nextRangeOfPages\" href=\"");
                buf.append(url);
                buf.append("\">");
                buf.append("...");
                buf.append("</a>");
            }
            return buf.toString();
        } else {
            return "";
        }
    }

    protected String getPreviousRangeOfPages(int pageNumber,
                                             int startPageIndex,
                                             int currentPageIndex,
                                             int windowSize) throws JahiaException {
        if (startPageIndex > 1) {
            final StringBuffer buf = new StringBuffer();
            final String url = jData.gui().drawContainerListPreviousWindowPageURL(getContainerList(),
                    currentPageIndex - startPageIndex + 1, windowSize, false, getId());
            if (url != null && url.length() > 0) {
                buf.append("<a class=\"previousRangeOfPages\" href=\"");
                buf.append(url);
                buf.append("\">");
                buf.append("...");
                buf.append("</a>");
            }
            return buf.toString();
        } else {
            return "";
        }
    }

    protected String getPreviousButtonLink(int windowSize) throws JahiaException {
        if (getContainerList() == null) return "";
        final Iterator<JahiaContainer> containers = getContainerList().getContainers();
        if (containers.hasNext()) {
            final String url = jData.gui().drawContainerListPreviousWindowPageURL(getContainerList(), 1, windowSize, false, getId());
            final StringBuffer buf = new StringBuffer();
            if (url != null && url.length() > 0) {
                buf.append("<a class=\"previousLink\" href=\"");
                buf.append(url);
                buf.append("\">");
                final String title = getMessage("pagination.previousButton", "Previous"
                );
                buf.append(title);
                buf.append("</a>");
            }
            return buf.toString();
        } else {
            return "";
        }
    }

    protected String getNextButtonLink(int windowSize) throws JahiaException {
        if (getContainerList() == null) return "";
        final Iterator<JahiaContainer> containers = getContainerList().getContainers();
        if (containers.hasNext()) {
            final String url = jData.gui().drawContainerListNextWindowPageURL(getContainerList(), 1, windowSize, false, getId());
            final StringBuffer buf = new StringBuffer();
            if (url != null && url.length() > 0) {
                buf.append("<a class=\"nextLink\" href=\"");
                buf.append(url);
                buf.append("\">");
                final String title = getMessage("pagination.nextButton", "Next"
                );
                buf.append(title);
                buf.append("</a>");
            }
            return buf.toString();
        } else {
            return "";
        }
    }

    protected String getPaginationPageUrl(int pageNumber, int currentPageIndex) throws JahiaException {
        if (pageNumber == currentPageIndex) {
            return String.valueOf(pageNumber);
        }
        final String url = jData.gui().drawContainerListWindowPageURL(getContainerList(), pageNumber, false, getId());
        final StringBuffer buff = new StringBuffer();
        if (url != null && url.length() > 0) {
            buff.append("<a class=\"paginationPageUrl\" href=\"");
            buff.append(url);
            buff.append("\">");
            buff.append(pageNumber);
            buff.append("</a>");
        }
        return buff.toString();
    }

    protected String displayPagination() {
        // Pagination support
        if (getContainerList() == null) return "";
        final JahiaContainerListPagination pagination = getContainerList().getCtnListPagination();
        int pageNumber = 0;
        if (pagination != null && pagination.isValid() && pagination.getNbPages() > 1) {
            try {
                final int startPageIndex;
                final int stopPageIndex;
                if (this.nbStepPerPage <= 0) {
                    startPageIndex = 1;
                    stopPageIndex = pagination.getNbPages();
                    pageNumber = 1;
                } else {
                    if (pagination.getCurrentPageIndex() <= this.nbStepPerPage) {
                        startPageIndex = 1;
                        stopPageIndex = this.nbStepPerPage;
                    } else {
                        if ((pagination.getCurrentPageIndex() % this.nbStepPerPage) == 0) {
                            startPageIndex = (pagination.getCurrentPageIndex() - this.nbStepPerPage) + 1;
                        } else {
                            startPageIndex = pagination.getCurrentPageIndex() -
                                    (pagination.getCurrentPageIndex() % this.nbStepPerPage) + 1;
                        }
                        stopPageIndex = startPageIndex + this.nbStepPerPage - 1;
                    }
                }
                Iterator<Integer> iterator = pagination.getPages().iterator();
                while (iterator.hasNext() && (pageNumber < startPageIndex - 1)) {
                    iterator.next();
                    pageNumber += 1;
                }
                if (iterator.hasNext()) {
                    pageNumber = (Integer) iterator.next();
                }

                final String previousLink = getPreviousButtonLink(pagination.getWindowSize());
                final String nextLink = getNextButtonLink(pagination.getWindowSize());
                //    final String currentScrollingValue = pagination.getScrollingValue(pagination.getCurrentPageIndex());
                //    final String currentScrollingValueInput = getScrollingValueInput(containerList, currentScrollingValue);
                final String previousRangeOfPages = getPreviousRangeOfPages(pageNumber, startPageIndex,
                        pagination.getCurrentPageIndex(), pagination.getWindowSize());
                final String nextRangeOfPages = getNextRangeOfPages(pageNumber, startPageIndex, stopPageIndex,
                        pagination.getCurrentPageIndex(), pagination.getNbPages(), nbStepPerPage, pagination.getWindowSize());

                final StringBuffer buff = new StringBuffer();
                buff.append("<div class=\"pagination\">\n");
                buff.append("<div class=\"paginationPosition\">\n");
                buff.append("<span>");
                buff.append(pagination.getCurrentPageIndex());
                buff.append(" of ");
                buff.append(pagination.getNbPages());
                buff.append("</span>\n");
                buff.append("</div><!-- end pagination position -->\n");

                buff.append("<div class=\"paginationNavigation\">\n");
                //     buff.append(currentScrollingValueInput);
                buff.append("\n");
                buff.append(previousLink);
                buff.append("\n");
                buff.append(previousRangeOfPages);
                buff.append("\n");
                final List<Integer> pages = startPageIndex == 1 && stopPageIndex == pagination.getNbPages() ? pagination.getPages() : pagination.getPages().subList(startPageIndex-1, stopPageIndex);
                for (Integer pageNb : pages) {
                    if (pageNb == pagination.getCurrentPageIndex()) {
                        buff.append("<span class=\"currentPage\">");
                    } else {
                        buff.append("<span>");
                    }
                    buff.append(getPaginationPageUrl(pageNb, pagination.getCurrentPageIndex()));
                    buff.append("</span>\n");
                }
                buff.append(nextRangeOfPages);
                buff.append("\n");
                buff.append(nextLink);
                buff.append("\n");

                buff.append("</div><!-- end pagination navigation -->");
                buff.append("</div><!-- end pagination -->");
                return buff.toString();

            } catch (final Exception e) {
                logger.error("Error in containerList tag pagination support", e);
            }
        }
        return "";
    }

    public void setDisplaySkins(boolean displaySkins) {
        this.displaySkins = displaySkins;
    }

    public void setDisplayExtensions(boolean displayExtensions) {
        this.displayExtensions = displayExtensions;
    }

    @Override
    protected void resetState() {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        listName = "";
        containerList = null;

        windowSize = Jahia.getSettings().getPaginationWindowSize();
        windowOffset = -1;
        displayPagination = PaginationBean.PAGINATION_AT_BOTTOM;

        jData = null;

        parentListName = "";
        displayActionMenu = true;
        displaySkins = true;
        displayExtensions = true;
        actionMenuNamePostFix = null;
        actionMenuCssClassName = null;
        actionMenuNameLabelKey = null;
        displayActionMenuAtBottom = false;
        sortByField = null;
        sortByMetaData = null;
        sortOrder = null;
        sortFieldDef = null;
        super.resetState();
    }

    private StringBuilder skinnify(HttpServletRequest request,
                                   HttpServletResponse resp, StringBuilder body) {
        String skin = "noskin";
        Map<String, String> extensions = new HashMap<String, String>();

        TemplatePathResolverBean templatePath = getJahiaBean().getIncludes()
                .getTemplatePath();
        if (displayExtensions) {
            try {
                String containerListType = getContainerList().getDefinition()
                        .getContainerListNodeType();
                if (containerListType == null) {
                    return body;
                }
                ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(
                        containerListType);
                List<ExtendedNodeType> superTypes = getContainerList().getDefinition().getContainerListMixinNodeTypes();
                for (ExtendedNodeType superType : superTypes) {
                    if (superType.isNodeType("jmix:containerExtension")
                            && !superType.getName().equals(
                            "jmix:containerExtension")) {
                        String n = StringUtils.substringAfter(superType.getName(),
                                ":");
                        String resolvedPath = templatePath.lookup("extensions/" + n
                                + "/"
                                + JCRContentUtils.cleanUpNodeName(nt.getName())
                                + "/" + n + ".jsp", "extensions/" + n + "/" + n
                                + ".jsp");
    
                        extensions.put(superType.getName(), resolvedPath);
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
        request.setAttribute("extensionsPages", extensions);
        request.setAttribute("containerListID", getContainerList().getID());
        request.setAttribute("contentObjectKey", new ContentContainerListKey(getContainerList().getID()).toString());
        try {
            request.setAttribute("definitionName", getContainerList().getDefinition().getName());
        } catch (JahiaException e) {
            logger.error("Unable to retrieve container list definition: "
                    + e.getMessage(), e);
        }
        request.setAttribute("skinned", Boolean.TRUE);
        String path = "skins/" + skin + "/" + skin + ".jsp";
        String resolvedPath = templatePath.lookup(path);

        final StringWriter stringWriter = new StringWriter();

        RequestDispatcher rd = request.getRequestDispatcher(resolvedPath);
        try {
            if (rd != null) {
                rd.include(request, new HttpServletResponseWrapper(resp) {
                    public PrintWriter getWriter() throws IOException {
                        return new PrintWriter(stringWriter);
                    }
                });
            }
        } catch (ServletException e) {
            logger.error("Cannot include skin: " + e.getMessage(), e);
            return body;
        } catch (IOException e) {
            logger.error("Cannot include skin: " + e.getMessage(), e);
            return body;
        }
        request.removeAttribute("skinned");
        return new StringBuilder(stringWriter.getBuffer().toString());
    }
    
    public int getWindowSize() {
        return windowSize;
    }

    public int getWindowOffset() {
        return windowOffset;
    }
    
    private void setContainerList(JahiaContainerList containerList) {
        this.containerList = containerList;
    }

    public String getSortByMetaData() {
        return sortByMetaData;
    }

    public String getSortByField() {
        return sortByField;
    }

    public String getSortOrder() {
        return sortOrder;
    }
}
