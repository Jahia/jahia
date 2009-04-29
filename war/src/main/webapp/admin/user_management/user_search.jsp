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
<!--
Prerequisites :
    - The function submitForm() should exists in the wrap JSP file.
    - The function handleKey() should exists in the wrap JSP file.
    - The function handleKeyCode() should exists in the wrap JSP file.
-->

<%@ page language = "java" %>
<%@ page import="java.security.Principal" %>
<%@ page import = "java.util.*"%>
<%@ page import="org.jahia.utils.JahiaTools"%>
<%@ page import="org.jahia.services.usermanager.*" %>
<%@ page import="org.jahia.data.viewhelper.principal.PrincipalViewHelper" %>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
    List providerList     = (List) request.getAttribute( "providerList" );
int stretcherToOpen   = 1;
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
                        <br><fmt:message key="org.jahia.admin.users.ManageUsers.search.label"/>&nbsp;:
                        <input type="text" name="searchString" size="10"
                            <%
                            String searchString = request.getParameter("searchString");
                            if (searchString != null) {
                                %>value='<%=request.getParameter("searchString")%>'<%
                            }
                                %>onkeydown="if (event.keyCode == 13) javascript:submitForm('search');">
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;&nbsp;<fmt:message key="org.jahia.admin.users.ManageUsers.in.label"/>&nbsp;:</td>
                    <td>
                        <input type="radio" name="searchIn" value="allProps" checked
                               onclick="disableCheckBox(properties);">&nbsp;<fmt:message key="org.jahia.admin.users.ManageUsers.allProp.label"/>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td valign="top">
                        <input type="radio" name="searchIn" value="properties"
                               onclick="enableCheckbox(properties);">&nbsp;<fmt:message key="org.jahia.admin.users.ManageUsers.properties.label"/><br>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="username" disabled><nobr> <fmt:message key="org.jahia.admin.username.label"/></nobr><br>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="firstname" disabled> <fmt:message key="org.jahia.admin.firstName.label"/><br>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="lastname" disabled> <fmt:message key="org.jahia.admin.lastName.label"/><br>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="email" disabled> <fmt:message key="org.jahia.admin.eMail.label"/><br>
                        &nbsp;&nbsp;&nbsp;&nbsp;
                        <input type="checkbox" name="properties" value="organization" disabled><nobr> <fmt:message key="org.jahia.admin.organization.label"/></nobr><br>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;&nbsp;<fmt:message key="org.jahia.admin.users.ManageUsers.on.label"/>&nbsp;:&nbsp;</td>
                    <td>
                        <input type="radio" name="storedOn" value="everywhere"
                               <%if (providerList.size() > 1) { %> checked <% } %>
                               onclick="disableCheckBox(providers);">&nbsp;<fmt:message key="org.jahia.admin.users.ManageUsers.everyWhere.label"/>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td><nobr>
                        <input type="radio" name="storedOn" value="providers"
                               <%if (providerList.size() <= 1) { %> checked <% } %>
                               onclick="enableCheckbox(providers);">&nbsp;<fmt:message key="org.jahia.admin.users.ManageUsers.providers.label"/></nobr>&nbsp;:<br>
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
                              <a class="ico-search" href="javascript:submitForm('search');"><fmt:message key="org.jahia.admin.users.ManageUsers.search.label"/></a>
                          </span>
                         </span>
                    </td>
                </tr>
            </table>
            <!-- -->
        </td>
<%
    Integer userNameWidth=new Integer(15);
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
                                           href="javascript:sortSelectBox(document.mainForm.selectedUsers, false, /.{2}/);"
                                           title="<fmt:message key='org.jahia.admin.users.ManageUsers.sortByProvider.label'/>"><fmt:message key="org.jahia.admin.users.ManageUsers.sortByProvider.label"/></a>
                                    </span>
                                </span>

                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="sort"
                                           href="javascript:sortSelectBox(document.mainForm.selectedUsers, false, /.{8}/);"
                                           title="<fmt:message key='org.jahia.admin.users.ManageUsers.sortByUserName.label'/>"><fmt:message key="org.jahia.admin.users.ManageUsers.sortByUserName.label"/></a>
                                    </span>
                                </span>

                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="sort"
                                           href='<%= "javascript:sortSelectBox(document.mainForm.selectedUsers, false, /.{" + (userNameWidth.intValue() + 9) + "}/);" %>'
                                           title="<fmt:message key='org.jahia.admin.users.ManageUsers.sortByProperty.label'/>"><fmt:message key="org.jahia.admin.users.ManageUsers.sortByProperty.label"/></a>
                                    </span>
                                </span>

                                </td>
                            </tr>
                        </table>
                        <%
                        Set resultSet = (Set)request.getAttribute( "resultList" );
                        String[] textPattern = {"Principal", "Provider, 6", "Name, "+userNameWidth, "Properties, 20"};
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
                                    %><option value="<%=principalViewHelper.getPrincipalValueOption(p)%>">
                                        <%=principalViewHelper.getPrincipalTextOption(p)%>
                                    </option><%
                            }
                            } %>
                        </select><br>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
