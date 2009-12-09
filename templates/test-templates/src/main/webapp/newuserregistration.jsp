<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.*" %>
<%@ include file="common/declarations.jspf" %>
<style type="text/css">
<!--
DIV#errors { color : #B42C29; }
DIV#errors li { color : #B42C29; }
-->
</style>
<%!
    public String getSingleValue(HttpServletRequest req, String attrName) {
        String[] values = (String[]) req.getAttribute(attrName);
        if (values == null) {
            return "";
        }
        if (values.length == 0) {
            return "";
        }
        return values[0];
    }
%>
<template:template>
    <template:templateHead>
        <%@ include file="common/template-head.jspf" %>
        <utility:applicationResources/>
    </template:templateHead>
    <template:templateBody>
        <div id="header">
            <div id="utilities">
                <div class="content">
                    <a name="pagetop"></a>
                    <span class="breadcrumbs"><fmt:message key='youAreHere'/>:</span>
                    <ui:currentPagePath cssClassName="breadcrumbs"/>
                    <ui:langBar display="horizontal" linkDisplay="flag" displayLanguageState="true"/>
                </div>
            </div>
        </div>
        <div id="pagecontent">
            <div class="content1cols">
                <div class="padding">
                    <div id="columnB">
                        <h2><fmt:message key="newUserRegistration.title"/></h2>
                        <div><fmt:message key="newUserRegistration.intro"/>"</div>
                        <c:if test="${not empty passwordPolicyMessages}">
                            <div class="error">
                              <fmt:message key="mySettings.errors"/>:
                              <ul>
                                <c:forEach items="${passwordPolicyMessages}" var="msg">
                                    <li><c:out value="${msg}"/></li>
                                </c:forEach>
                              </ul>
                            </div>
                        </c:if><br style="clear:both"/>
                        <form name="mainForm" method="post" action="?mode=display&screen=save">
                          <table class="text" border="0" cellspacing="3" cellpadding="0">
                            <tr>
                              <td><fmt:message key="newUserRegistration.username"/> :</td>
                              <td><input type="text" name="newUser_username" value='<%=getSingleValue(request, "newUser_username")%>' /></td>
                            </tr>
                            <tr>
                              <td><fmt:message key="mySettings.password"/> :</td>
                              <td><input type="password" name="newUser_password1" value='<%=getSingleValue(request, "newUser_password1")%>' /></td>
                            </tr>
                            <tr>
                              <td><fmt:message key="mySettings.confirm.password"/> :</td>
                              <td><input type="password" name="newUser_password2" value='<%=getSingleValue(request, "newUser_password2")%>' /></td>
                            </tr>
                            <tr>
                              <td><fmt:message key="mySettings.firstname"/> :</td>
                              <td><input type="text" name="newUserProp_firstname" value='<%=getSingleValue(request, "newUserProp_firstname")%>' /></td>
                            </tr>
                            <tr>
                              <td><fmt:message key="mySettings.lastname"/> :</td>
                              <td><input type="text" name="newUserProp_lastname" value='<%=getSingleValue(request, "newUserProp_lastname")%>' /></td>
                            </tr>
                            <tr>
                              <td><fmt:message key="mySettings.organization"/> :</td>
                              <td><input type="text" name="newUserProp_organization" value='<%=getSingleValue(request, "newUserProp_organization")%>' /></td>
                            </tr>
                            <tr>
                              <td><fmt:message key="mySettings.email"/> :</td>
                              <td><input type="text" name="newUserProp_email" value='<%=getSingleValue(request, "newUserProp_email")%>' /></td>
                            </tr>
                          </table>
                        <div>
                          <input type="submit" name="submit" value='<fmt:message key="newUserRegistration.button.submit"/>'/>
                        </div>                                                  	
                        </form>
                        <div>
                            <a href="${requestScope.currentPage.url}" title="<fmt:message key='backToPreviousPage'/>"><fmt:message key='backToPreviousPage'/></a>
                        </div>                        
                    </div>
                    <br class="clear"/>
                </div>
            </div>
            <!-- end of content1cols section -->
        </div>
        <!-- end of pagecontent section-->

        <div id="footer">
            <template:include page="common/footer.jsp"/>
        </div>
    </template:templateBody>
</template:template>
