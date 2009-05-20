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
<%@ page import="org.jahia.services.usermanager.JahiaGroupManagerProvider" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jahia.services.sites.JahiaSite"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Set"%>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%!
public boolean isInParam(HttpServletRequest request,String name, String value){
    String[] parameter = request.getParameterValues(name);
    if(parameter != null){
        for(int i = 0;i<parameter.length; i++){
            if(parameter[i].equalsIgnoreCase(value)){
                return true;
            }
        }
    }
    return false;
}
%>

<%
    final List resultList = (List) engineMap.get("resultList");
    final List providerList = (List) engineMap.get("groupProviderList");
    final List memberList         = (List) engineMap.get("memberList");
    final Set alreadySelectedUsers = (Set) engineMap.get("alreadySelectedUsers");
    final List siteList = (List) engineMap.get("siteList");
    final JahiaSite jahiaSite = (JahiaSite) engineMap.get("currentSite");
    final Boolean selectSite = (Boolean) engineMap.get("selectSite");
    Iterator siteEnum = siteList.iterator();
    String propertiesParameters[] = request.getParameterValues("properties");
    String providersParameters[] = request.getParameterValues("providers");

%>

<script type="text/javascript">
    var defaultAction = "searchGroups";

    // call in order to submit search
    function getkey(e) {
        var code;
        if (window.event)
            code = window.event.keyCode;
        else if (e)
            code = e.which;
        else
            code = null;

        if (code!=null && code == 13) {
            sendForm('searchGroups');
        }
    }

    // call in order to cancel 'enter' button
    function getkey2(e) {
        var code;
        if (window.event)
            code = window.event.keyCode;
        else if (e)
            code = e.which;
        else
            code = null;

        if (code!=null && code == 13) {
            return false;
        }
    }

    // called when a group is double-clicked in the first select box
    function viewMembers() {
        document.mainForm.method = "POST";
        document.mainForm.screen.value='searchGroups';
        document.mainForm.subaction.value='viewMembers';
        document.mainForm.submit();
    }

    // called when a user is double-clicked in the second select box
    function viewUserGroups(userValue) {
        document.mainForm.method = "POST";
        document.mainForm.screen.value='searchUsers';
        document.mainForm.subaction.value='viewUserGroups';
        document.mainForm.submit();
    }

</script>
<table border="0" width="100%" cellpadding="0" cellspacing="0">
  <tr>
    <td valign="top" width="420" class="nopadding">
      <div class="head">
        <div class="object-title">&nbsp;</div>
      </div>
      <div class="content-body">
        <div id="operationMenu">
          <span class="dex-PushButton">
            <span class="first-child">


              <a class="ico-search" href="javascript:sendForm('searchGroups');" title="<fmt:message key="org.jahia.engines.users.SelectUG_Engine.searchItems.label"/>">
                <fmt:message key="org.jahia.engines.users.SelectUG_Engine.search.label"/></a>
            </span>
          </span>
        </div>
      </div>
      <div class="content-body">
        <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
          <tr class="evenLine">
            <th valign="top">
              <fmt:message key="org.jahia.engines.users.SelectUG_Engine.search.label"/> :
            </th>
            <td>
              <input type="text" name="searchString" size="20" onkeypress="return getkey2(event);"<% final String searchString = (String) request.getAttribute("searchString"); if (searchString != null) { %> value="<%=(String) request.getAttribute("searchString")%>"<% } %>>
              <br/><fmt:message key="org.jahia.engines.users.SelectUG_Engine.useTest.label"/>
            </td>
          </tr>
          <tr class="oddLine">
            <th valign="top">
              <fmt:message key="org.jahia.engines.users.SelectUG_Engine.in.label"/> :
            </th>
            <td>
              <ul class="noStyle">
                <li>
                  <input type="radio" name="searchIn" value="allProps" checked="checked" onclick="disableCheckBox(properties);" <%if(isInParam(request,"searchIn","allProps")){%> checked<%}%>>&nbsp;<fmt:message key="org.jahia.engines.users.SelectUG_Engine.allProperties.label" />
                </li>
                <li>
                  <input type="radio" name="searchIn" value="properties" onclick="enableCheckbox(properties);" <%if(isInParam(request,"searchIn","properties")){%> checked<%}%>>&nbsp;<fmt:message key="org.jahia.engines.users.SelectUG_Engine.properties.label"/>:
                  <ul>
                    <li>
                      <input type="checkbox" name="properties" value="groupname"  <%if(propertiesParameters == null || propertiesParameters.length == 0){%> disabled="disabled" <%}%><%if(isInParam(request,"properties","groupname")){%> checked<%}else{%><%}%>>&nbsp;
                      <fmt:message key="org.jahia.engines.groupname.label"/>
                    </li>
                    <li>
                      <input type="checkbox" name="properties" value="description" <%if(propertiesParameters == null ||propertiesParameters.length == 0){%> disabled="disabled" <%}%> <%if(isInParam(request,"properties","description")){%> checked<%}%>>&nbsp;
                      <fmt:message key="org.jahia.engines.users.SelectUG_Engine.description.label"/> (LDAP)
                    </li>
                    <li>
                      <input type="checkbox" name="properties" value="members" <%if(propertiesParameters == null ||propertiesParameters.length == 0){%> disabled="disabled" <%}%> <%if(isInParam(request,"properties","members")){%> checked<%}%>>&nbsp;
                      <fmt:message key="org.jahia.engines.users.SelectUG_Engine.members.label"/> (LDAP)
                    </li>
                  </ul>
                </li>
              </ul>
            </td>
          </tr>
          <tr class="evenLine">
            <th valign="top">
              <fmt:message key="org.jahia.engines.users.SelectUG_Engine.on.label"/> :
            </th>
            <td>
              <ul class="noStyle">
                <%  if (selectSite.booleanValue()) { %>
                <li>
                  <fmt:message key="org.jahia.engines.users.SelectUG_Engine.selectSite.label"/>:
                  <select name="selectedSite">
                    <% while(siteEnum.hasNext()) { JahiaSite site = (JahiaSite) siteEnum.next(); %>
                      <option value="<%=site.getID()%>"<%if(jahiaSite.getID()==site.getID()){%> selected<%}%>>&nbsp;<%=site.getTitle()%>&nbsp;</option>
                    <% }%>
                  </select>
                </li>
                <%  } %>
                <li>
                  <input type="radio" name="storedOn" value="everywhere"<% if (providersParameters == null || providersParameters.length == 0) { %> checked="checked" <% } %> onclick="disableCheckBox(providers);">&nbsp;
                  <fmt:message key="org.jahia.engines.users.SelectUG_Engine.everywhere.label"/>
                </li>
                <li>
                  <input type="radio" name="storedOn" value="providers"<% if (providersParameters != null && providersParameters.length > 0) { %> checked="checked" <% } %> onclick="enableCheckbox(providers);">&nbsp;
                  <fmt:message key="org.jahia.engines.users.SelectUG_Engine.providers.label"/> :
                  <ul>
                    <%
                    final Iterator providerEnum = providerList.iterator();
                    while (providerEnum.hasNext()) {
                      final JahiaGroupManagerProvider curProvider = (JahiaGroupManagerProvider) providerEnum.next();
                    %>
                      <li>
                        <input type="checkbox" name="providers" value="<%=curProvider.getKey()%>" <%if(providersParameters == null || providersParameters.length == 0){%>disabled="disabled"<%}%><% if (isInParam(request,"providers",curProvider.getKey())) { %> checked="checked" <% } %>>
                        <fmt:message key='<%="org.jahia.engines.users.SelectUG_Engine.providers." + curProvider.getKey() + ".label"%>'/>&nbsp;(<%=curProvider.getKey()%>)
                      </li>
                    <% } %>
                  </ul>
                </li>
              </ul>
            </td>
          </tr>
        </table>
        <script type="text/javascript">
          function setfocus() {
              document.mainForm.elements["searchString"].select();
          }
          setfocus();
        </script>
      </div>
    </td>
    <td class="split nopadding">
      <div class="head">
        <div class="object-title"><fmt:message key="org.jahia.engines.users.SelectUG_Engine.searchResults.label"/></div>
      </div>
      <div class="content-body padded">

        <!-- Display user/group list -->
        <%
            final Integer userNameWidth = (Integer) request.getSession().getAttribute("userNameWidth");
        %>
        <table border="0" width="100%" cellpadding="0" cellspacing="0">
          <tr>
            <td valign="top" width="40" align="center">
              <a style="margin-left: 20px;" href="javascript:sortSelectBox(document.mainForm.selectedUG, false, /.{2}/);" title="<fmt:message key="org.jahia.engines.sortBySource.label"/>">
                <img src="${pageContext.request.contextPath}/engines/images/icons/sort_descending.gif" border="0" alt="sortBySource"/></a>
            </td>
            <td width="40" align="center">
              <a style="margin-left: 25px;" href="javascript:sortSelectBox(document.mainForm.selectedUG, false, /.{8}/);" title="<fmt:message key="org.jahia.engines.users.SelectUG_Engine.sortByUserId.label"/>">
                <img src="${pageContext.request.contextPath}/engines/images/icons/sort_descending.gif" border="0" alt="sortByUserId"/></a>
            </td>
            <td valign="top" width="50">
              &nbsp;
            </td>
            <td>
              <a style="margin-left: 135px;" href='<%="javascript:sortSelectBox(document.mainForm.selectedUG, false, /.{" + (userNameWidth.intValue() + 9) + "}/);" %>' title="<fmt:message key="org.jahia.engines.sortByProperty.label"/>">
                <img src="${pageContext.request.contextPath}/engines/images/icons/sort_descending.gif" border="0" alt="sortByProperty"/> </a>
            </td>
          </tr>
          <tr>
            <td colspan="4">
              <select style="width: 500px;" name="selectedUG" size="10" class="fontfix" multiple="multiple" ondblclick="javascript:pasteSelection()">
                <% if (resultList == null || resultList.size() == 0) { %>
                  <option value="null" selected="selected">
                      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                      &nbsp;&nbsp;&nbsp;
                  </option>
                <% } else {
                  for (int i = 0; i < resultList.size(); i += 2) { %>
                    <option value='<%= JahiaTools.replacePattern((String)resultList.get(i), "&nbsp;", " ")%>'>
                      <%= (String) resultList.get(i + 1) %>
                    </option>
                <% }
                }%>
              </select>
            </td>
          </tr>
          <tr>
            <td colspan="3">
              <div>
                <a href="javascript:selectAllOptionsSelectBox(document.mainForm.selectedUG);">
                  <fmt:message key="org.jahia.engines.users.SelectUG_Engine.selectAll.label"/></a>
                &nbsp;|&nbsp;
                <a href="javascript:invertSelectionSelectBox(document.mainForm.selectedUG);">
                  <fmt:message key="org.jahia.engines.users.SelectUG_Engine.invertSelection.label"/></a>
              </div>
            </td>
          </tr>
        </table>
      </div>
      <div class="headtop">
        <div class="object-title"><a href="javascript:viewMembers()"><fmt:message key="org.jahia.engines.users.SelectUG_Engine.viewGroupMembersButton.label"/></a></div>
      </div>
      <div class="content-body padded">
        <table border="0" width="100%" cellpadding="0" cellspacing="0">
          <tr>
            <td valign="top" width="40" align="center">
              <a style="margin-left: 20px;" href="javascript:sortSelectBox(document.mainForm.memberList, false, /.{2}/);"title="<fmt:message key="org.jahia.engines.sortBySource.label"/>">
                <img src="${pageContext.request.contextPath}/engines/images/icons/sort_descending.gif" border="0" alt="sortBySource"/></a>
            </td>
            <td width="70" align="center">
              <a style="margin-left: 25px;" href="javascript:sortSelectBox(document.mainForm.memberList, false, /.{8}/);" title="<fmt:message key="org.jahia.engines.users.SelectUG_Engine.sortByUserId.label"/>">
                <img src="${pageContext.request.contextPath}/engines/images/icons/sort_descending.gif" border="0" alt="sortByUserId"/></a>
            </td>
            <td valign="top" width="30">
              &nbsp;
            </td>
            <td>
              <a style="margin-left: 135px;" href='<%="javascript:sortSelectBox(document.mainForm.memberList, false, /.{" + (userNameWidth.intValue() + 9) + "}/);" %>' title="<fmt:message key="org.jahia.engines.sortByProperty.label"/>">
                <img src="${pageContext.request.contextPath}/engines/images/icons/sort_descending.gif" border="0" alt="sortByProperty"/> </a>
            </td>
          </tr>
          <tr>
            <td colspan="4">
              <select style="width: 500px;" name="memberList" size="10" ondblclick="javascript:viewUserGroups()">
              <% if (memberList == null || memberList.size() == 0) { %>
                  <option value="null" selected>
                      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                      &nbsp;&nbsp;&nbsp;</option>
              <% } else {
                  for (int i = 0; i < memberList.size(); i += 2) {
              %>
               <option value='<%=JahiaTools.replacePattern((String)memberList.get(i), "&nbsp;", " ")%>'>
                    <%= (String) memberList.get(i+1)%>
               </option>
              <%   }
                 } %>
              </select>
            </td>
          </tr>
        </table>
      </div>
    </td>
  </tr>
</table>