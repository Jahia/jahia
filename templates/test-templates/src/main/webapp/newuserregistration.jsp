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
                    <ui:languageSwitchingLinks display="horizontal" linkDisplay="flag" displayLanguageState="true"/>
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
