<!--
Prerequisites :
    - The function submitForm() should exists in the wrap JSP file.
    - The function handleKey() should exists in the wrap JSP file.
    - The function handleKeyCode() should exists in the wrap JSP file.
-->

<%@ page language = "java" %>
<%@ page import="java.security.Principal" %>
<%@ page import = "java.util.*"%>
<%@ page import="org.jahia.services.usermanager.*" %>
<%@ page import="org.jahia.data.viewhelper.principal.PrincipalViewHelper" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<%
    List providerList     = (List) request.getAttribute( "providerList" );
int stretcherToOpen   = 0;
%>

<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/selectbox.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/checkbox.js"></script>

<table border="0" style="width:100%">
    <tr>
        <td valign="top">
            <!-- Search user and group -->
            <table border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td colspan="2">
                        <br><fmt:message key="label.search"/>&nbsp;:
                        <input type="text" name="searchString" size="15"
                            <%
                            String searchString = request.getParameter("searchString");
                            if (searchString != null) {
                                %>value='<%=StringEscapeUtils.escapeHtml(request.getParameter("searchString"))%>'<%
                            }
                                %>onkeydown="if (event.keyCode == 13) javascript:submitForm('search');">
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;&nbsp;<fmt:message key="label.in"/>&nbsp;:</td>
                    <td>
                        <input type="radio" name="searchIn" value="allProps" checked
                               onclick="disableCheckBox(document.mainForm.elements.properties);">&nbsp;<fmt:message key="label.allProperties"/>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td valign="top">
                        <input type="radio" name="searchIn" value="properties"
                               onclick="enableCheckbox(document.mainForm.elements.properties);">&nbsp;<fmt:message key="label.properties"/><br>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="username" disabled><nobr> <fmt:message key="label.username"/></nobr><br>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="j:firstName" disabled> <fmt:message key="org.jahia.admin.firstName.label"/><br>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="j:lastName" disabled> <fmt:message key="org.jahia.admin.lastName.label"/><br>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="j:email" disabled> <fmt:message key="label.email"/><br>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="j:organization" disabled><nobr> <fmt:message key="org.jahia.admin.organization.label"/></nobr><br>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;&nbsp;<fmt:message key="label.on"/>&nbsp;:&nbsp;</td>
                    <td>
                        <input type="radio" name="storedOn" value="everywhere"
                               <%if (providerList.size() > 1) { %> checked <% } %>
                               onclick="disableCheckBox(providers);">&nbsp;<fmt:message key="label.everyWhere"/>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td><nobr>
                        <input type="radio" name="storedOn" value="providers"
                               <%if (providerList.size() <= 1) { %> checked <% } %>
                               onclick="enableCheckbox(providers);">&nbsp;<fmt:message key="label.providers"/></nobr>&nbsp;:<br>
<%
                        Iterator providerEnum = providerList.iterator();
                        while (providerEnum.hasNext()) {
                            JahiaUserManagerProvider curProvider = (JahiaUserManagerProvider) providerEnum.next();
%>
                            &nbsp;&nbsp;&nbsp;&nbsp;
                            <input type="checkbox" name="providers" value="<%=curProvider.getKey()%>" disabled
                            <%if (providerList.size() <= 1) { %> checked <% } %>>
                                <%//=curProvider.getTitle()%> <%=curProvider.getKey()%><br>
<%
                        }
%>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td align="right">
                        <br>

                        <span class="dex-PushButton">
                          <span class="first-child">
                              <a class="ico-search" href="javascript:submitForm('search');"><fmt:message key="label.search"/></a>
                          </span>
                         </span>
                    </td>
                </tr>
            </table>
            <!-- -->
        </td>
<%
    Integer userNameWidth=new Integer(30);
    request.getSession().setAttribute("userNameWidth",userNameWidth);
%>
        <td>
        <!-- Display user list -->
            <table class="text" border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td>
                       <center> <i><fmt:message key="org.jahia.admin.users.ManageUsers.searchResult.label"/></i></center> <br>
                        <table class="text" width="100%" border="0" cellspacing="0" cellpadding="0">
                            <tr>
                                <td>

                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="sort"
                                           href="javascript:sortSelectBox(document.mainForm.selectedUsers, false, /\|(.*)/g);"
                                           title="<fmt:message key='org.jahia.admin.users.ManageUsers.sortByUserName.label'/>"><fmt:message key="org.jahia.admin.users.ManageUsers.sortByUserName.label"/></a>
                                    </span>
                                </span>

                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="sort"
                                           href="javascript:sortSelectBox(document.mainForm.selectedUsers, false, /(.*)\|/g);"
                                           title="<fmt:message key='label.sortByLastname'/>"><fmt:message key="label.sortByLastname"/></a>
                                    </span>
                                </span>

                                </td>
                            </tr>
                        </table>
                        <%
                        Set resultSet = (Set)request.getAttribute( "resultList" );
                        String[] textPattern = {"Name, "+userNameWidth, "Properties, 30"};
                        PrincipalViewHelper principalViewHelper = new PrincipalViewHelper(textPattern);
                        %>
                        <select ondblclick="javascript:handleKey(event);"
                                <%if (resultSet.size() == 0) {%>disabled<%}%>
                                onkeydown="javascript:handleKeyCode(event.keyCode);"
                                style="width:435px;" name="selectedUsers" size="25" class="fontfix" multiple="multiple">
                            <%
                            if (resultSet.size() == 0) {
                                %><option value="null" selected>
                                -- - -&nbsp;&nbsp;-&nbsp;&nbsp;&nbsp; - <fmt:message key="org.jahia.admin.users.ManageUsers.noUserFound.label"/> -&nbsp;&nbsp;&nbsp;-&nbsp;&nbsp;- - --
                                  </option><%
                            } else {
                                Iterator it = resultSet.iterator();
                                while (it.hasNext()) {
                                    Principal p = (Principal)it.next();
                                    pageContext.setAttribute("principalKey", principalViewHelper.getPrincipalValueOption(p));
                                    pageContext.setAttribute("principalLabel", principalViewHelper.getPrincipalTextOption(p));
                                    %><option value="${fn:escapeXml(principalKey)}">${principalLabel}</option><%
                            }
                            } %>
                        </select><br>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
