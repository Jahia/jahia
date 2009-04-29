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
<%@ include file="common/declarations.jspf" %>
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
                        <h2><fmt:message key="mySettings.title"/></h2>
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
                        <template:gwtJahiaModule id="mysettings" jahiaType="mySettings">
                            <utility:gwtResourceBundle resourceName="mySettings.password" aliasResourceName="pwd"/>
                            <utility:gwtResourceBundle resourceName="mySettings.confirm.password"
                                                       aliasResourceName="c_pwd"/>
                            <utility:gwtResourceBundle resourceName="mySettings.button.add.prop"
                                                       aliasResourceName="button_add_personal_property"/>
                            <utility:gwtResourceBundle resourceName="mySettings.button.remove.prop"
                                                       aliasResourceName="button_remove_personal_property"/>
                            <utility:gwtResourceBundle resourceName="mySettings.button.save"
                                                       aliasResourceName="button_save"/>
                            <utility:gwtResourceBundle resourceName="mySettings.title"
                                                       aliasResourceName="title_mysettings"/>
                            <utility:gwtResourceBundle resourceName="mySettings.title.remove.prop"
                                                       aliasResourceName="title_remove_properties"/>
                            <utility:gwtResourceBundle resourceName="mySettings.button.remove"
                                                       aliasResourceName="button_remove"/>
                            <utility:gwtResourceBundle resourceName="mySettings.title.new.prop"
                                                       aliasResourceName="title_new_property"/>
                            <utility:gwtResourceBundle resourceName="mySettings.label.newprop.fieldname"
                                                       aliasResourceName="label_fieldname_new_property"/>
                            <utility:gwtResourceBundle resourceName="mySettings.label.newprop.fieldvalue"
                                                       aliasResourceName="label_fieldvalue_new_property"/>
                        </template:gwtJahiaModule>
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



