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
<%@ page import="org.jahia.data.containers.JahiaContainer" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="org.jahia.data.files.JahiaFileField" %>
<%@ page import="org.jahia.engines.deletecontainer.HardcodedLinkSourceInfo" %>
<%@ page import="org.jahia.gui.GuiBean" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="org.jahia.services.pages.JahiaPage" %>
<%@ page import="org.jahia.services.webdav.*" %>
<%@ page import="org.jahia.services.content.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="org.jahia.services.fields.ContentField" %>
<%@ page import="org.jahia.content.ObjectKey" %>
<%@ page import="org.jahia.content.JahiaObject" %>
<%@ page import="org.jahia.services.acl.JahiaBaseACL" %>
<%@page import="org.jahia.services.containers.ContentContainer"%>
<%@page import="org.jahia.engines.deletecontainer.DeleteContainer_Engine"%>
<%@ page import="org.jahia.content.ContentObject" %>
<%@ page import="org.jahia.exceptions.JahiaException" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>

<%
final Logger logger = Logger.getLogger("deletecontainer.jsp") ;
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
final JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");
final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");

final List deletedPages = (List) engineMap.get("deletedPages");
final List deletedLinks = (List) engineMap.get("deletedLinks");
final List futureBrokenLinkObjects = (List) engineMap.get("futureBrokenLinkObjects");
final Set<ContentObject> ctnPickers = (Set<ContentObject>) engineMap.get("ctnPickers") ;
final boolean stagingWarning = ((Boolean) engineMap.get("stagingWarning")).booleanValue();
final boolean warning = ((Boolean) engineMap.get("warning")).booleanValue();
final boolean error = ((Boolean) engineMap.get("errorMessage")).booleanValue();
final int pageDefID = jParams.getPage().getPageTemplateID();
final boolean showEditMenu = false;
List deletedFiles = new ArrayList();
request.setAttribute("showEditMenu", Boolean.valueOf(showEditMenu));
%>

<script type="text/javascript">
  function refreshEngineWindow(theUrl, winName) {
    window.name = winName;
    window.location.href = theUrl;
    return false;
  }
</script>

<div id="header">
  <h1>Jahia</h1>
  <h2 class="delete"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.deleteContainer.label"/></h2>
</div>
<div id="mainContent">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom-full">
            <div class="tabContent">
              <div id="content" class="full">
                <% if (!error) {%>
                <div class="head">
                  <div class="object-title"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.confirm.label"/></div>
                </div>
                <%}%>
                <div>
                  <% if (error) { %>
                    <p class="errorbold">
                       <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.errorMessage"/>
                    </p>
                  <%} else {%>
                    <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
                      <%
                      final Iterator fieldList = theContainer.getFields();
                      int tot = 0;
                      while (fieldList.hasNext() && tot++ < 5) {
                          final JahiaField aField = (JahiaField) fieldList.next();
                          int fieldType = aField.getDefinition().getType();
                					if(fieldType == org.jahia.data.fields.FieldTypes.FILE)
                					{
                					  //deletedFiles.add(aField);
                   					JahiaFileField fileField = (JahiaFileField)aField.getObject();
                   					List usages = ServicesRegistry.getInstance().getJCRStoreService().findUsages(fileField.getStorageName(), jParams, false);

                   					if(usages != null && usages.size() == 1)
                     					deletedFiles.add(aField);
                					}
                          
                          try { %>
                            <tr>
                              <th width="150">
                                <%=aField.getDefinition().getTitle(jParams.getLocale())%>
                              </th>
                              <% if (fieldType < 5) { // smalltext, bigtext, undefined %>
                                <td>
                                  <%=GuiBean.glueTitle(aField.getValue(), 30)%>
                                </td>
                              <%} else if (fieldType == 5) { // page
                                  final JahiaPage thePage = (JahiaPage) aField.getObject();
                                  if ( thePage != null ){%>
                                    <td>
                                    <%
                                        List<String> localizedTitles = thePage.getDisplayableLocalizedTitle(jParams) ;
                                        StringBuilder titleToDisplay = new StringBuilder(localizedTitles.get(0)) ;
                                        if (localizedTitles.size() > 1) {
                                            titleToDisplay.append("&nbsp;").append(localizedTitles.get(1)) ;
                                        }
                                    %>
                                    <%=titleToDisplay%>
                                    </td>
                                  <%} else {%>
                                    <td>&nbsp;</td>
                                  <%}
                              } else if (fieldType < 10) { // file
                                final JahiaFileField fField = (JahiaFileField) aField.getObject(); %>
                                <td>
                                    <%=fField.getRealName()%>
                                </td>
                              <% } else { %>
                                <td>
                                  <%=GuiBean.glueTitle(aField.getValue(), 30)%>
                                </td>
                              <%}%>
                            </tr>
                        <%} catch (Throwable t) {
                            t.printStackTrace();
                          }
                        } %>
                    </table>
                  <%}%>
                </div>
                <% //check if files to delete
    						if(!deletedFiles.isEmpty())
    						{ %>
    									<input type="checkbox" name="deleteFile" /> 
    									<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.deleteFiles"/>
    									<span  style="color: red;">(<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.deleteFiles.warning"/>)</span>
    
    						<%
      
    						}
    
                if (warning) {
                  String currentLocale = jParams.getCurrentLocale().getLanguage() ;
                  EngineLanguageHelper elh = (EngineLanguageHelper)engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER) ;
                  if (elh != null) {
                      currentLocale = elh.getCurrentLanguageCode() ;
                  }%>
                  <p class="errorbold">
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.warning.label"/>:
                  </p>
                  <% if (stagingWarning) { %>
                         <p><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.stagingWarning.label"/></p>
                  <% } %>
                  <% if (!deletedPages.isEmpty()) {
                    if (deletedPages.size() > DeleteContainer_Engine.PAGE_LIST_SIZE_LIMIT) {%>
                      <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.delPagesPartialList.label"/>:
                    <%} else {%>
                      <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.delPages.label"/>:
                    <%}%>
                    <ul>
                      <%
                      for (Iterator it = deletedPages.iterator(); it.hasNext(); ) {
                        final JahiaPage aPage = (JahiaPage) it.next();
                        if (aPage != null && aPage.getACL().getPermission(jParams.getUser(), JahiaBaseACL.READ_RIGHTS, true)) {
                            List<String> titles = aPage.getDisplayableLocalizedTitle(jParams) ;

                            %>
                            <li><a target="_blank" href="<%=aPage.getURL(jParams)%>"><%=titles.get(0)%></a><%if (titles.size() > 1) {%>&nbsp;<%=titles.get(1)%><%}%></li>
                      <%}
                      }%>
                    </ul>
                  <%}
                  if (ctnPickers != null && ctnPickers.size() > 0) { %>
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.brokenPickers.label"/>:
                    <ul>
                      <%
                      for (ContentObject contentObject : ctnPickers) {
                          int id = contentObject.getPageID() ;
                          try {
                              logger.debug("Displaying picker : " + contentObject.getID()) ;
                              JahiaPage parentPage = ServicesRegistry.getInstance().getJahiaPageService().lookupPage(id, jParams) ;
                              String dispPicker = parentPage.getContentPage().getTitles(true).get(currentLocale) ;
                              %>
                              <li><a target="_blank" href="<%=parentPage.getURL(jParams)%>"><%=dispPicker%></a></li>
                              <%
                          } catch (JahiaException e) {
                              logger.error("Cannot retrieve parent page for content object " + contentObject.getObjectKey().getKey(), e) ;
                          }
                      }%>
                    </ul>
                  <%}
                  if (!deletedLinks.isEmpty()) {%>
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.delLinks.label"/>:
                    <ul>
                      <%
                      for (Iterator it = deletedLinks.iterator(); it.hasNext(); ) {
                        final JahiaPage aPage = (JahiaPage) it.next();
                        if (aPage != null && aPage.getACL().getPermission(jParams.getUser(), JahiaBaseACL.READ_RIGHTS, true)) { %>
                          <li>
                            <a target="_blank" href="<%=aPage.getURL(jParams)%>"><%=aPage.getTitle()%></a>&nbsp;
                            (<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.onPageId.label"/>:
                            <a target="_blank" href="<%=ContentPage.getPage(aPage.getParentID()).getURL(jParams)%>"><%=aPage.getParentID()%></a>)
                          </li>
                        <%}
                      }%>
                    </ul>
                  <%}
                  if (futureBrokenLinkObjects.size() > 0) { %>
                    <p><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.futureBrokenLinks.label"/>:<p>
                    <div class="inMiddle">
                      <table id="hardLinks">
                        <tr class="header">
                          <td>
                            <strong><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.type.label"/></strong>
                          </td>
                          <td>
                            <strong><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.subType.label"/></strong>
                          </td>
                          <td>
                            <strong><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.id.label"/></strong>
                          </td>
                          <td>
                            <strong><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.name.label"/></strong></td>
                          <td>
                            <strong><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.title.label"/></strong>
                          </td>
                          <td>
                            <strong><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.pageId.label"/></strong>
                          </td>
                          <td>
                            <strong><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.deletecontainer.DeleteContainer_Engine.pageTitle.label"/></strong>
                          </td>
                        </tr>
                        <%
                        final Iterator brokenLinkObjectIter = futureBrokenLinkObjects.iterator();
                        final GuiBean gui = new GuiBean(jParams);
                        while (brokenLinkObjectIter.hasNext()) {
                          final HardcodedLinkSourceInfo curObject = (HardcodedLinkSourceInfo) brokenLinkObjectIter.next();
                          final ObjectKey objectKey = ObjectKey.getInstance(curObject.getObjectType() + "_" + curObject.getID());
                          final JahiaObject jahiaObject = JahiaObject.getInstance(objectKey);
                          final ContentField field = (ContentField) jahiaObject;
                          final ContentPage targetPage = ContentPage.getPage(curObject.getPageID(), false);
                          final boolean hasReadAccessToTargetPage = targetPage.checkReadAccess(jParams.getTheUser());
                          final String targetPageUrl = targetPage.getURL(jParams);
                          final String targetPageTitle = targetPage.getTitle(jParams.getEntryLoadRequest());
                          String updateFieldUrl = gui.html().drawUpdateFieldLauncher(field);
                          if (field.getContainerID() > 0) {
                            ContentContainer cnt = ContentContainer.getContainer(field.getContainerID());
                            if (cnt != null) {
                              updateFieldUrl = gui.html().drawUpdateContainerLauncher(cnt, field.getID());
                            }
                          } %>
                          <tr>
                            <td><%=curObject.getObjectType()%>&nbsp;</td>
                            <td><%=curObject.getObjectSubType()%>&nbsp;</td>
                            <td><%=curObject.getID()%>&nbsp;</td>
                            <td>
                              <% if (updateFieldUrl.length() != 0) {%>
                                <a href="javascript:<%= updateFieldUrl %>"><%=curObject.getName()%></a>
                              <%}else{%>
                                <%=curObject.getName()%>
                              <%}%>&nbsp;
                            </td>
                            <td>
                              <%=curObject.getTitle()%>&nbsp;
                            </td>
                            <% if (hasReadAccessToTargetPage) {%>
                              <td><a target="_blank" href="<%=targetPageUrl%>"><%=curObject.getPageID()%></a>&nbsp;</td>
                              <td><a target="_blank" href="<%=targetPageUrl%>"><%= targetPageTitle %></a>&nbsp;</td>
                            <% } else { %>
                              <td><%=curObject.getPageID()%>&nbsp;</td>
                              <td><%= targetPageTitle %>&nbsp;</td>
                            <% } %>
                          </tr>
                        <% } %>
                      </table>
                    </div>
                  <%}%>
                <% } %>
              </div>
            </div>
          </div>
        </td>
      </tr>
    </tbody>
  </table>
  <jsp:include page="../buttons.jsp" flush="true" />
</div>