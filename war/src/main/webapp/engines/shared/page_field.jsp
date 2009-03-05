<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="org.jahia.content.ContentObject" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.data.beans.JahiaBean" %>
<%@ page import="org.jahia.data.containers.JahiaContainer" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.engines.selectpage.SelectPage_Engine" %>
<%@ page import="org.jahia.engines.shared.JahiaPageEngineTempBean" %>
<%@ page import="org.jahia.engines.shared.Page_Field" %>
<%@ page import="org.jahia.engines.validation.EngineValidationHelper" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<%@ page import="org.jahia.services.acl.JahiaACLManagerService" %>
<%@ page import="org.jahia.services.acl.JahiaBaseACL" %>
<%@ page import="org.jahia.services.content.nodetypes.ExtendedNodeDefinition" %>
<%@ page import="org.jahia.services.lock.LockPrerequisites" %>
<%@ page import="org.jahia.services.pages.*" %>
<%@ page import="org.jahia.services.sites.SiteLanguageSettings" %>
<%@ page import="org.jahia.taglibs.utility.Utils" %>
<%@ page import="org.jahia.utils.LanguageCodeConverters" %>
<%@ page import="javax.servlet.jsp.JspWriter" %>
<%@ page import="javax.servlet.jsp.PageContext" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.bin.Jahia" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%!
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("jsp.jahia.engines.shared.Page_Field");

    private void drawTimeBasedPublishingStatusIcon(ContentObject contentObject, ProcessingContext jParams, PageContext pageContext)
            throws IOException {
        if (contentObject == null) {
            return;
        }
        JspWriter out = pageContext.getOut();
        final String contextPath = Jahia.getContextPath();
        final String actionURL = contextPath + "/ajaxaction/GetTimeBasedPublishingState?params=/op/edit/pid/" +
                jParams.getPageID() + "&key=" + contentObject.getObjectKey();

        String serverURL = actionURL + "&displayDialog=true";
        String dialogTitle = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.dialogTitle",
                jParams.getLocale(), "Informational");
        StringBuffer cmdBuffer = new StringBuffer("handleTimeBasedPublishing(event,'");
        cmdBuffer.append(serverURL).append("','");
        cmdBuffer.append(contentObject.getObjectKey()).append("',").append("'/op/edit/pid/")
                .append(jParams.getPageID()).append("','").append(dialogTitle).append("')");
        out.print("<img class=\"timeBasedPublishingState\" id=\"");
        out.print("img_");
        out.print(contentObject.getObjectKey());
        out.print("\" border=\"0\" src=\"");
        out.print(actionURL);
        out.print("\" onclick=\"");
        out.print(cmdBuffer.toString());
        out.print("\" />\n");
    }


    public boolean areValuesTheSameInAllActiveLanguages(final JahiaPageEngineTempBean pageBean,
                                                        final String langCode) {
        if (pageBean.getTitles().size() > 1) {
            final Iterator titles = pageBean.getTitles().values().iterator();
            final String theTitle = pageBean.getTitle(langCode);
            if (theTitle == null || theTitle.length() == 0 || theTitle.startsWith("<jahia")) {
                return false;
            }
            String oldValue = null;
            while (titles.hasNext()) {
                final String title = (String) titles.next();
                if (oldValue != null && !oldValue.equals(title)) {
                    return false;
                }
                oldValue = title;
            }
            return true;
        }
        return false;
    }
%>

<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }
    Utils.getJahiaBean(pageContext, true);
    final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
    final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + ".theField");
    final Boolean templateNotFound = (Boolean) engineMap.get("templateNotFound");

    Map pageBeans = (Map) session.getAttribute("Page_Field.PageBeans");
    if (pageBeans == null) {
        pageBeans = new HashMap();
    }
    final JahiaPageEngineTempBean pageBean =
            (JahiaPageEngineTempBean) pageBeans.get(theField.getDefinition().getName());

    final JahiaPageBaseService jahiaPageBaseService = JahiaPageBaseService.getInstance();
    final Locale processingLocale = (Locale) engineMap.get(JahiaEngine.PROCESSING_LOCALE);
    String title = pageBean.getTitle(processingLocale.toString());
    if (title == null) {
        title = "";
    }
    List<String> tVlist = new ArrayList<String>();// List of templates constraint requested

// Set the flags corresponding to the page options to display
    final boolean isNewPage = theField.getObject() == null;

// added to support specific behaviors of engine addPage actions
    boolean isDirectPage = true;//default
    boolean isExternalLink = true; //default
    boolean isInternalLink = true; //default

    final ContentPage contentPage = jahiaPageBaseService.lookupContentPage(theField.getPageID(), false);
    if (contentPage != null) {
        ExtendedNodeDefinition def = theField.getDefinition().getNodeDefinition();

        String types = def.getSelectorOptions().get("type");
        if (types != null) {
            if (!types.contains("direct")) isDirectPage = false;
            if (!types.contains("external")) isExternalLink = false;
            if (!types.contains("internal")) isInternalLink = false;
        }

        String templates = def.getSelectorOptions().get("templates");
        if (templates != null) {
            tVlist = new ArrayList<String>(Arrays.asList(templates.split(",")));
        }

        // field type definition always prevails over the current value.
        // added to support new feature: check of defaultvalue in declarations
        // syntax accepted: move, internal or external, pageonly and linkonly. the 2 last are exclusive
        // the webdesigner can also limit the template choice by a list of templates names surrounded by brackets

        logger.debug("external=" + isExternalLink);
        logger.debug("internal=" + isInternalLink);
        logger.debug("direct=" + isDirectPage);
    }

    String pageURLKey = pageBean.getUrlKey();
    final boolean hideFromNavigationMenu = pageBean.isHideFromNavigationMenu();
    String remoteURL = pageBean.getRemoteURL(processingLocale.toString());
    if (remoteURL == null) {
        remoteURL = "http://";
    }
// Get titles for the selected page engine.
    int sourcePageID = pageBean.getPageLinkID();
    String sourceTitle = null;
    if (sourcePageID != -1) {
        final ContentPage sourceContentPage = jahiaPageBaseService.lookupContentPage(sourcePageID, false);
        Map titles = sourceContentPage.getTitles(ContentPage.LAST_UPDATED_TITLES);
        if (titles != null)
            sourceTitle = (String) titles.get(processingLocale.toString());
        if (!isInternalLink) pageURLKey = sourceContentPage.getProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
    }

    if (pageURLKey == null) {
        pageURLKey = "";
    }

    boolean allSameTitles = areValuesTheSameInAllActiveLanguages(pageBean, jParams.getLocale().toString());

    if (sourceTitle != null && "".equals(title)) {
        title = sourceTitle;
    }

    logger.debug("sourcePageID: " + sourcePageID);

/* originally the test allowed for changing the page type in UPDATE
final boolean canChangeType = pageBean.getID() != jParams.getPageID() ||
        !Page_Field.UPDATE_PAGE.equals(pageBean.getOperation());
*/

    boolean canDisplayTemplateSelection = true;
    if (!isNewPage && !Page_Field.UPDATE_PAGE.equals(pageBean.getOperation())) {
        canDisplayTemplateSelection = false;
    }

    final boolean canChangeType = !Page_Field.UPDATE_PAGE.equals(pageBean.getOperation());

// A change type page warning is displayed if it is not a new page, a page of
// direct type and if the user has change the type in the engine GUI.
    final boolean hasDirectTypeChange = !isNewPage &&
            ((JahiaPage) theField.getObject()).getPageType() == ContentPage.TYPE_DIRECT &&
            !Page_Field.UPDATE_PAGE.equals(pageBean.getOperation()) ? true : false;

    ContentObject timeBasedPublishingObject = (ContentObject) request.getAttribute("Page_Field.enableTimeBasedPublishingStatus");
    boolean enableTimeBasedPublishing = (timeBasedPublishingObject != null);

    boolean displayURLKeyInput = true;
    final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
    if (aclService.getSiteActionPermission(LockPrerequisites.URLKEY, jParams.getUser(), JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) <= 0) {
        displayURLKeyInput = false;
    }

    boolean displayHideFromNavigationMenuInput = true;
    if (aclService.getSiteActionPermission(LockPrerequisites.HIDE_FROM_NAVIGATION_MENU, jParams.getUser(), JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) <= 0) {
        displayHideFromNavigationMenuInput = false;
    }

%>
<utility:setBundle basename="JahiaInternalResources"/>
<script type="text/javascript">
    var operation = "link";

    function setPid(pid) {
        handleActionChanges("edit&operation=" + operation + "&shouldSetPageLinkID=true&pageSelected=" + pid, pid);
    }
    function getPageOffsetLeft(el) {
        var x;
        // Return the x coordinate of an element relative to the page.
        x = el.offsetLeft - 5;
        if (el.offsetParent != null)
            x += getPageOffsetLeft(el.offsetParent);
        return x;
    }

    function callSelectPageMove() {
        // Inhibate the engine pop up close
        document.mainForm.pageURLKey.disabled = false;
        operation = "<%=SelectPage_Engine.MOVE_OPERATION%>";
    <%=jData.gui().html().drawSelectPageLauncher(SelectPage_Engine.MOVE_OPERATION,
pageBean.getParentID(), pageBean.getID(), "setPid", jParams.getSiteID(), -1)%>
    }

    function callSelectPageLink() {
        // Inhibate the engine pop up close
        if (typeof document.mainForm.pageURLKey != 'undefined') {
            document.mainForm.pageURLKey.disabled = "disabled";
        }
        operation = "<%=SelectPage_Engine.LINK_OPERATION%>";
    <%=jData.gui().html().drawSelectPageLauncher(SelectPage_Engine.LINK_OPERATION, pageBean.getParentID(), pageBean.getID(), "setPid", -1, -1)%>
    }

    function titleInputEvent() {
        if (document.getElementById('noValueRadio')) {
            if (document.getElementById('noValueRadio').checked) {
                document.mainForm.operation[0].checked = true;
            }
        }
    }

    function setPageURL() {
        document.mainForm.pageURLKey.disabled = "disabled";
        var token = "?";
        if (document.mainForm.action.indexOf("?") > -1) {
            token = "&";
        }
        document.mainForm.action += token + "loadedFromSession=true" + "&operation=<%=Page_Field.LINK_URL%>";
    }

    function check() {

        // Test if checked
        checked = false;
        if (document.mainForm.operation.length > 1) {
            for (var i = 0; i < document.mainForm.operation.length; i++) {
                if (document.mainForm.operation[i].checked) {
                    checked = true;
                }
            }
        } else {
            // if operation == 1 then we have to test differently.
            if (document.mainForm.operation.checked) {
                checked = true;
            }
        }
        if (!checked) return false;

        // now here we will test if the move page or the link page
        // target page IDs are valid or not.

        // first we test for any of the cases, when both options
        // are available to the user
        if ((document.getElementById('movePageRadio') != null) &&
            (document.getElementById('linkPageRadio') != null)) {
            if (document.getElementById('movePageRadio').checked || document.getElementById('linkPageRadio').checked) {
            <% if ((Page_Field.LINK_JAHIA_PAGE.equals(pageBean.getOperation()) || Page_Field.MOVE_PAGE.equals(pageBean.getOperation())) && sourcePageID <= 0) { %>
                return false;
            <% } else { %>
                return true;
            <% } %>
            }
        }

        // from now on we have either the move or the link
        // option present on the display, but not both
        // (that case was already covered in the previous
        // test)

        // now let's test only the move case if it is present.
        if (document.getElementById('movePageRadio') != null) {
            if (document.getElementById('movePageRadio').checked) {
            <% if ((Page_Field.LINK_JAHIA_PAGE.equals(pageBean.getOperation()) || Page_Field.MOVE_PAGE.equals(pageBean.getOperation())) && sourcePageID <= 0) { %>
                return false;
            <%} else { %>
                return true;
            <% } %>
            }
        }

        if (document.getElementById('linkPageRadio') != null) {
            if (document.getElementById('linkPageRadio').checked) {
            <% if ((Page_Field.LINK_JAHIA_PAGE.equals(pageBean.getOperation()) || Page_Field.MOVE_PAGE.equals(pageBean.getOperation())) && sourcePageID <= 0) { %>
                return false;
            <% } else { %>
                return true;
            <% } %>
            }
        }
        return true;
    }

    function handleActionChanges(param) {
        saveContent();
        var sep = "?";
        if (document.mainForm.action.indexOf("?") > 0) {
            sep = "&";
        }
        document.mainForm.action += sep + "screen=" + param;
        teleportCaptainFlam(document.mainForm);
    }

    function switchIcons(callingElementID, inputElementID) {
        var element = document.getElementById(inputElementID);
        var callingElement = document.getElementById(callingElementID);
        if (element) {
            var theValue = element.value;
            if (theValue == "true") {
                theValue = "false";
                callingElement.className = "sharedLanguageNo";
                callingElement.title = '<fmt:message key="org.jahia.applyToSingleLanguage.label"/>';
            } else {
                theValue = "true";
                callingElement.className = "sharedLanguageYes";
                callingElement.title = '<fmt:message key="org.jahia.applyToAllLanguages.label"/>';
            }
            element.value = theValue;
        }
    }
</script>
<div class="head">
    <table cellpadding="0" cellspacing="0" border="0" width="100%" class="object-title">
        <tr>
            <th width="100%">
                <% if (isNewPage) { %>
                <% if (!isDirectPage) { %>
                <fmt:message key="org.jahia.engines.shared.Page_Field.jahiaLinkCreation.label"/>
                <% } else { %>
                <fmt:message key="org.jahia.engines.shared.Page_Field.jahiaPageCreation.label"/>
                <% } %>
                <% } else { %>
                <% if (!isDirectPage) { %>
                <fmt:message key="org.jahia.engines.shared.Page_Field.jahiaLinkEdition.label"/>
                <% } else { %>
                <fmt:message key="org.jahia.engines.shared.Page_Field.jahiaPageEdition.label"/>
                <% } %>
                <% } %>
            </th>
            <% if (jParams.getSite().getLanguageSettings(true).size() > 1 && ServicesRegistry.getInstance().getJahiaACLManagerService().hasWriteAccesOnAllLangs(jParams)) { %>
            <td nowrap="nowrap">
                <fmt:message key="org.jahia.applyToAllLanguages.label"/>&nbsp;:&nbsp;
            </td>
            <td>
                <% if (allSameTitles) { %>
                <a id="switchIcons_<%=pageBean.getID()%>"
                   href="javascript:switchIcons('switchIcons_<%=pageBean.getID()%>', 'shared_title');"
                   title='<fmt:message key="org.jahia.applyToAllLanguages.label"/>'
                   class="sharedLanguageYes">&nbsp;</a>
                <% } else { %>
                <a id="switchIcons_<%=pageBean.getID()%>"
                   href="javascript:switchIcons('switchIcons_<%=pageBean.getID()%>', 'shared_title');"
                   title='<fmt:message key="org.jahia.applyToSingleLanguage.label"/>'
                   class="sharedLanguageNo">&nbsp;</a>
                <% } %>
                <input id="shared_title" type="hidden" name="shared_title"
                       value="<%=allSameTitles || pageBean.isSharedTitle()%>"/>
            </td>
            <% } %>
        </tr>
    </table>
</div>

<% if (hasDirectTypeChange) { %>
<p class="error">
    <fmt:message key="org.jahia.warning.label"/> !!
    <fmt:message key="org.jahia.engines.shared.Page_Field.ifThisPageIsChanged.label"/>.
</p>
<% } %>
<logic:present name="engineMessages">
    <p class="errorbold">
        <fmt:message key="org.jahia.engines.shared.BigText_Field.error.label"/>:
    </p>
    <ul>
        <logic:iterate name="engineMessages" property="messages" id="curMessage">
            <li class="error"><internal:message name="curMessage"/></li>
        </logic:iterate>
    </ul>
</logic:present>
<table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
<tr>
    <th width="100">
        <% if (pageBean == null) { %>
        <fmt:message key="org.jahia.engines.shared.Page_Field.noTempPageBean.label"/>.
        <% } %>
        <% if (!isDirectPage) { %>
        <fmt:message key="org.jahia.engines.shared.Page_Field.jahiaLinkTitle.label"/>
        <% } else { %>
        <fmt:message key="org.jahia.engines.shared.Page_Field.jahiaPageTitle.label"/>
        <% } %>
    </th>
    <td>
        <%
            final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");
            if (EngineValidationHelper.isFieldMandatory(theContainer, theField, jParams)) {
        %>
        <span class="errorbold">(*)</span>
        <input id="go" type="hidden" name="go"/>
        <% } %>
        <div id="errorMsg"></div>
        <input type="text" size="80" name="page_title" onkeyup="titleInputEvent();" onchange="titleInputEvent();"
               value="<%=title%>" maxlength="250">
    </td>
</tr>
<%
    final List<Locale> localeList = new ArrayList<Locale>();
    final List<SiteLanguageSettings> siteLanguageSettings = jParams.getSite().getLanguageSettings();
    if (siteLanguageSettings != null) {
        for (SiteLanguageSettings curSetting : siteLanguageSettings) {
            if (curSetting.isActivated()) {
                final Locale tempLocale = LanguageCodeConverters.languageCodeToLocale(curSetting.getCode());
                final boolean canEdit = ServicesRegistry.getInstance().getJahiaACLManagerService().getSiteActionPermission("engines.languages." +
                        tempLocale.toString(),
                        jParams.getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        jParams.getSiteID()) > 0;
                if (canEdit) localeList.add(tempLocale);
            }
        }
    }

    if (displayURLKeyInput && isDirectPage) { %>
<tr>
    <th>
        <%if (localeList.size() > 1) { %>
        <fmt:message key="org.jahia.engines.pages.PageProperties_Engine.pageURLKeyShared.label"/><br/>
        <% } else { %>
        <fmt:message key="org.jahia.engines.pages.PageProperties_Engine.pageURLKey.label"/><br/>
        <% } %>
    </th>
    <td>
        <input type="text" size="80" name="pageURLKey" value="<%=pageURLKey%>"
               <% if (!canDisplayTemplateSelection || Page_Field.LINK_JAHIA_PAGE.equals(pageBean.getOperation())) { %>disabled="disabled" <% } %>/>
    </td>
</tr>
<% } %>
<% if (displayHideFromNavigationMenuInput) { %>
<tr>
    <th>
        <fmt:message key="org.jahia.engines.shared.Page_Field.hideFromNavigationMenu.label"/><br/>
    </th>
    <td>
        <input type="checkbox" name="hideFromNavigationMenu" <% if (hideFromNavigationMenu) { %>
               checked="checked" <% } %> />
    </td>
</tr>
<% } %>
<tr>
<th valign="top">
    <fmt:message key="org.jahia.engines.shared.Page_Field.pageType.label"/>
</th>
<td>
<table>
<%
    // Is it a new page field ?
    // Is it not external or internal link neither moving

    if (isDirectPage) {
        final Iterator templateList;

        //check for templates requested
        boolean checkedTemplates = false;
        if (tVlist.size() > 0) {
            Iterator tlist = (Iterator) engineMap.get("templateList");
            List<JahiaPageDefinition> all = new ArrayList<JahiaPageDefinition>();
            List<JahiaPageDefinition> constraint = new ArrayList<JahiaPageDefinition>();
            int checkcount = tVlist.size();
            while (tlist.hasNext()) {
                JahiaPageDefinition t = (JahiaPageDefinition) tlist.next();
                all.add(t);
                String templatename = t.getName();
                if (tVlist.contains(templatename)) {
                    logger.debug("found requested templates:" + templatename);
                    checkcount--;
                    constraint.add(t);
                }
            }
            if (checkcount == 0) {
                checkedTemplates = true;
                logger.debug("all templates contraints checked");
                templateList = constraint.iterator();
            } else if (checkcount > 0 && constraint.size() > 0) {
                logger.warn("some templates requested in constraint are not present!");
                templateList = constraint.iterator();
            } else {
                templateList = all.iterator();
            }
        } else {
            templateList = (Iterator) engineMap.get("templateList");
        }
%>
<tr>
    <%
        if (canDisplayTemplateSelection) {
            if (!templateList.hasNext()) {
    %>
    <td colspan="2">
        &nbsp;The&nbsp;user&nbsp;<%=jParams.getUser().getUsername()%>&nbsp;has&nbsp;no&nbsp;templates&nbsp;access&nbsp;
        Cannot create a new page or change template.
    </td>
    <% } else { %>
    <td valign="top">
        <% if (isNewPage) { %>
        <!-- Create a new page -->
        <input id="directPageRadio" type="radio" name="operation"
               value="<%=Page_Field.CREATE_PAGE%>"<% if (Page_Field.CREATE_PAGE.equals(pageBean.getOperation())) { %>
               checked="checked"<% } %> onfocus="document.mainForm.pageURLKey.disabled = false;">&nbsp;
        <% } else { %>
        <input id="directPageRadio" type="radio" name="operation"
               value="<%=Page_Field.UPDATE_PAGE%>"<% if (Page_Field.UPDATE_PAGE.equals(pageBean.getOperation())) { %>
               checked="checked"<% } %> onfocus="document.mainForm.pageURLKey.disabled = false;">&nbsp;
        <% } %>
    </td>
    <td>
        <label>
            <% if (isNewPage) { %>
            <fmt:message key="org.jahia.engines.shared.Page_Field.createPageTemplate.label"/>
            <% } else { %>
            <fmt:message key="org.jahia.engines.shared.Page_Field.changePageTemplate.label"/>
            <% } %>
        </label>
        <br/>
        <select name="template_id" onfocus="if (operation[0]) operation[0].checked = true;">
            <% while (templateList.hasNext()) {
                JahiaPageDefinition theTemplate = (JahiaPageDefinition) templateList.next();
                pageContext.setAttribute("pageTemplate", theTemplate);
                if (!checkedTemplates || tVlist.contains(theTemplate.getName())) {
            %>
            <option value="<%=theTemplate.getID()%>"<% if (theTemplate.getID() == pageBean.getPageTemplateID()) { %>
                    selected="selected"<% } %>
                    title="<fmt:message key='${pageTemplate.description}'/>"><fmt:message key="${pageTemplate.displayName}"/></option>
            <% }
            }
                if (templateNotFound) { %>
            <option value="-1" selected="selected">Template not found !!! Deleted ?</option>
            <% } %>
        </select>
    </td>
    <%
            }
        }%>
</tr>
<%if (isNewPage) {%>
<tr>
    <td valign="top">
        <!-- Move an existing a new page -->
        <input id="movePageRadio" type="radio" name="operation" value="<%=Page_Field.MOVE_PAGE%>"
               onclick="callSelectPageMove();"<% if (Page_Field.MOVE_PAGE.equals(pageBean.getOperation())) { %>
               checked="checked"<% } %>>&nbsp;
    </td>
    <td>
        <label><fmt:message key="org.jahia.engines.shared.Page_Field.moveExistingPage.label"/></label>
        <br/>
        <a href="javascript:callSelectPageMove()"><fmt:message key="org.jahia.engines.shared.Page_Field.selectPageToMove.label"/></a>
        <input type="hidden" name="moveSourcePageID" value="<%=sourcePageID%>"/>
        <% if (Page_Field.MOVE_PAGE.equals(pageBean.getOperation())) {
            if (sourcePageID != -1) { %>
        <ul>
            <li>
                <%=sourceTitle%>&nbsp;(<fmt:message key="org.jahia.pageId.label"/>:&nbsp;<%=sourcePageID%>
                )
            </li>
        </ul>
        <% } else { %>
        <fmt:message key="org.jahia.engines.shared.Page_Field.noPageSelected.label"/>.
        <%
                }
            }
        %>
    </td>
</tr>
<%
        }
    }

    // Prevent changes if the actual page displayed in core engine is the same as
    // the actual edited page field
    if (canChangeType) {
        // test internal condition
        if (isInternalLink) { %>
<tr>
    <td valign="top">
        <input id="linkPageRadio" type="radio" name="operation" value="<%=Page_Field.LINK_JAHIA_PAGE%>"
               onclick="callSelectPageLink();"<% if (Page_Field.LINK_JAHIA_PAGE.equals(pageBean.getOperation())) { %>
               checked="checked"<% } %>>&nbsp;
    </td>
    <td>
        <label><fmt:message key="org.jahia.engines.shared.Page_Field.linkExistingPage.label"/></label>
        <br/>
        <a href="javascript:callSelectPageLink()"><fmt:message key="org.jahia.engines.shared.Page_Field.selectPageToLink.label"/></a>
        <input type="hidden" name="linkSourcePageID" value="<%=sourcePageID%>"/>
        <%
            if (Page_Field.LINK_JAHIA_PAGE.equals(pageBean.getOperation())) {
                if (sourcePageID != -1) { %>
        <ul>
            <li>
                <%drawTimeBasedPublishingStatusIcon(timeBasedPublishingObject, jParams, pageContext);%>
                &nbsp;<%=sourceTitle%>&nbsp;(<fmt:message key="org.jahia.pageId.label"/>:&nbsp;<%=sourcePageID%>
                )
            </li>
        </ul>
        <% } else { %>
        <fmt:message key="org.jahia.engines.shared.Page_Field.noPageSelected.label"/>.
        <% } %>
        <% } %>
    </td>
</tr>
<% } %>
<% if (isExternalLink) { %>
<!-- Create a link to an external web site -->
<tr>
    <td valign="top">
        <input id="remoteURLRadio" type="radio" name="operation"
               value="<%=Page_Field.LINK_URL%>" <% if (Page_Field.LINK_URL.equals(pageBean.getOperation())) { %>
               checked="checked"<% } %> <% if (!isNewPage) { %> onfocus="setPageURL();" <% } %>
               onclick="document.mainForm.pageURLKey.disabled = 'disabled';">&nbsp;
    </td>
    <td>
        <label><fmt:message key="org.jahia.engines.shared.Page_Field.createLinkToExternalSite.label"/></label>
        <br/>
        <!-- option 1 if linkonly page, 3 if page does not exist. -->
        <input <% if (isNewPage) { %>
                onfocus="operation[<%= !isDirectPage ? 1 : 3 %>].checked = true;"<% } %> type="text"
                name="remote_url" size="50" value="<%=remoteURL%>" maxlength="250">
        <!-- Reset the link -->
    </td>
</tr>
<% } %>
<% if (isNewPage || ((JahiaPage) theField.getObject()).getPageType() != ContentPage.TYPE_DIRECT) { %>
<tr>
    <td valign="top">
        <input id="noValueRadio" type="radio" name="operation"
               onfocus="document.mainForm.page_title.value = '';document.mainForm.pageURLKey.disabled = 'disabled';"
               value="<%=Page_Field.RESET_LINK%>"<%if (Page_Field.RESET_LINK.equals(pageBean.getOperation())) {%>
               checked="checked"<% } %>>
    </td>
    <td>
        <label>
            <% if (isNewPage) { %>
            <fmt:message key="org.jahia.engines.shared.Page_Field.differPageCreation.label"/>
            <% } else { %>
            <fmt:message key="org.jahia.engines.shared.Page_Field.removePageLink.label"/>
            <% } %>
        </label>
        <br/>
        <% if (isNewPage) { %>
        <fmt:message key="org.jahia.engines.shared.Page_Field.differPageCreationBody.label"/>.
        <% } else { %>
        <fmt:message key="org.jahia.engines.shared.Page_Field.removePageLinkBody.label"/>.
        <% } %>
    </td>
</tr>
<% } %>
<% } %>
</table>
</td>
</tr>
</table>

<script type="text/javascript">
    function setfocus() {
    <% if (engineMap.containsKey("focus")) { %>
        document.mainForm.elements["pageURLKey"].select();
    <% } else { %>
        document.mainForm.elements["page_title"].select();
    <% } %>
    }
    setfocus();
</script>

<% if (!isDirectPage && Page_Field.LINK_JAHIA_PAGE.equals(pageBean.getOperation()) && sourcePageID == -1) {
    pageBean.setOperation(Page_Field.RESET_LINK);
%>
<script type="text/javascript">
    document.getElementById("noValueRadio").checked = true;
    document.getElementById("noValueRadio").select();
</script>
<% } %>