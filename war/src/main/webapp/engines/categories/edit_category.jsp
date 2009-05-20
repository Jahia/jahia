<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.jahia.engines.categories.CategoriesEdit_Engine"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="org.jahia.utils.LanguageCodeConverters"%>
<%@ page import="java.util.Locale"%>
<%@ page import="org.jahia.services.categories.Category"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");

final CategoriesEdit_Engine.CategoryTemporaryBean categoryTemporaryBean = (CategoriesEdit_Engine.CategoryTemporaryBean) engineMap.get(CategoriesEdit_Engine.TEMPORARY_CATEGORY_SESSION_NAME);

final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
final String fieldForm = (String) engineMap.get(fieldsEditCallingEngineName + "." + "fieldForm");
final String logForm = (String) engineMap.get("logForm");
final String theScreen = (String) engineMap.get("screen");
final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
final JahiaData jData = (JahiaData) jParams.getRequest().getAttribute("org.jahia.data.JahiaData");

final boolean showEditMenu = (theScreen.equals("edit") || theScreen.equals("rightsMgmt"));

String parentCategoryKey = (String) engineMap.get("parentCategoryKey");
request.setAttribute("showEditMenu", Boolean.valueOf(showEditMenu));
%>
<script type="text/javascript">
<!--
  function deleteProperty(propertyName) {
      document.mainForm.propertyToDelete.value = propertyName;
      sendFormApply();
  }
//-->
</script>
<div id="header">
  <h1>Jahia</h1>
  <h2><fmt:message key="org.jahia.admin.categories.ManageCategories.editOrAddCategory.label"/></h2>
</div>
<div id="mainContent">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left" width="100%">
          <div class="dex-TabBar">
            <jsp:include page="../menuBar.jsp" flush="true" />
          </div>
        </td>
        <td style="vertical-align: top;" align="right" nowrap="nowrap">
          <jsp:include page="../multilanguage_links.jsp" flush="true" />
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%" colspan="2">
        <% if (theScreen.equals("edit")) { %>
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
              <% if (showEditMenu) { %>
                <%@ include file="../menu.inc" %>
              <% } else { %>
                <%@ include file="../tools.inc" %>
              <% } %>
              <div id="content" class="fit w2">
        
                <input type="hidden" name="propertyToDelete" value="" />
                 <table cellpadding="5" cellspacing="0" border="0">
                    <tr>
                    <td>
                        <b><fmt:message key="org.jahia.admin.categories.ManageCategories.editCategory.key.label"/>&nbsp;:&nbsp;</b>
                    </td>
                    <td>
                      <!-- we can only edit a category key for a new category -->
                <% if (categoryTemporaryBean.getKey() == null) { %>
                  <input type="text" name="newCategoryKey" > <br>
                <% } else { %>
                  <%= categoryTemporaryBean.getKey() %> <br>
                <% } %>
                    </td>
                    </tr>
                    <tr>
                    <td><b><fmt:message key="org.jahia.admin.categories.ManageCategories.editCategory.titles.label"/></b></td>
                    <td><%if (categoryTemporaryBean.getTitles().size() > 0) {%>
                    <table cellpadding="3" cellspacing="0" border="0">
                    <%
                    Iterator titleMapEntryIter = categoryTemporaryBean.getTitles().entrySet().iterator();
                    while (titleMapEntryIter.hasNext()) {
                      Map.Entry curEntry = (Map.Entry) titleMapEntryIter.next();
                      String curLanguageCode = (String) curEntry.getKey();
                      String curTitleInLanguage = (String) curEntry.getValue();
                      if (curTitleInLanguage == null) {
                        curTitleInLanguage = "";
                      }
                      Locale curLocale = LanguageCodeConverters.languageCodeToLocale(curLanguageCode);
                      %>
                      <tr>
                        <td class="text">
                          <internal:displayLanguageFlag code="<%=curLanguageCode%>" />
                          <%=curLocale.getDisplayLanguage(jData.getProcessingContext().getLocale())%> (<%=curLanguageCode%>)
                        </td>
                        <td class="text" valign="top">
                          <input type="text" name="title_<%=curLanguageCode%>" value="<%=curTitleInLanguage%>"/>
                        </td>
                      </tr>
                    <%} %>
                  </table>
                <% } %></td>
                    </tr>
                    <tr>
                        <td colspan="2"><%if(parentCategoryKey!=null){%>
                  <% final Category cat = Category.getCategory(Integer.parseInt(parentCategoryKey),jData.getProcessingContext().getUser()); %>
                  <fmt:message key="org.jahia.admin.categories.ManageCategories.editCategory.parentCategoryKey.label"/> : <%= cat.getKey() %> (<fmt:message key="org.jahia.admin.categories.ManageCategories.editCategory.title.label"/> = <%=cat.getTitle(jParams.getLocale())%>) <br/>
                  <br/>
                <%}%>
                <b><fmt:message key="org.jahia.admin.categories.ManageCategories.editCategory.properties.label"/></b>
                <br/>
                <table border="0" cellpadding="3" cellspacing="0">
                  <tr>
                    <td class="text">
                      <b><fmt:message key="org.jahia.admin.categories.ManageCategories.editCategory.propertyName.label"/></b>
                    </td>
                    <td class="text" colspan="2">
                      <b><fmt:message key="org.jahia.admin.categories.ManageCategories.editCategory.propertyValue.label"/></b>
                    </td>
                  </tr>
                  <% if (categoryTemporaryBean.getKey() != null) {
                    Iterator propertyEntryIter = categoryTemporaryBean.getProperties().entrySet().iterator();
                    while (propertyEntryIter.hasNext()) {
                      Map.Entry curEntry = (Map.Entry) propertyEntryIter.next();
                      String curPropertyName = (String) curEntry.getKey();
                      String curPropertyValue = (String) curEntry.getValue();
                      %>
                      <tr>
                        <td class="text" >
                          <%=curPropertyName %>
                        </td>
                        <td class="text" >
                          <input type="text" name="setProperty_<%=curPropertyName%>" value="<%=curPropertyValue%>"/>
                        </td>
                        <td>
                          <div id="buttons">
                            <div class="button">
                              <a href="javascript:deleteProperty('<%=curPropertyName%>');" title="<fmt:message key="org.jahia.button.delete"/>">
                                  <fmt:message key="org.jahia.button.delete"/>
                              </a>
                            </div>
                          </div>
                        </td>
                      </tr>
                    <%}
                  }%>
                  <tr>
                    <td class="text">
                      <input type="text" name="newPropertyName" />
                    </td>
                    <td class="text">
                      <input type="text" name="newPropertyValue" />
                    </td>
                    <td class="text">
                      <fmt:message key="org.jahia.admin.categories.ManageCategories.editCategory.newPropertyExplanation.label"/>
                    </td>
                  </tr>
                </table>
                <br/></td>
                    </tr>
                </table>

                
              </div>
            </div>
          </div>
          <% } else if (theScreen.equals("rightsMgmt")) { %>
            <%=fieldForm%>
          <% } else if (theScreen.equals("logs")) { %>
            <%=logForm%>
          <% } %>
        </td>
      </tr>
    </tbody>
  </table>
  <jsp:include page="../buttons.jsp" flush="true" />
</div>