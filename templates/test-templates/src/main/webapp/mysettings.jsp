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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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
                             <internal:gwtResourceBundle resourceName="mySettings.password" aliasResourceName="pwd"/>
                             <internal:gwtResourceBundle resourceName="mySettings.confirm.password"
                                                       aliasResourceName="c_pwd"/>
                             <internal:gwtResourceBundle resourceName="mySettings.button.add.prop"
                                                       aliasResourceName="button_add_personal_property"/>
                             <internal:gwtResourceBundle resourceName="mySettings.button.remove.prop"
                                                       aliasResourceName="button_remove_personal_property"/>
                             <internal:gwtResourceBundle resourceName="mySettings.button.save"
                                                       aliasResourceName="button_save"/>
                             <internal:gwtResourceBundle resourceName="mySettings.title"
                                                       aliasResourceName="title_mysettings"/>
                             <internal:gwtResourceBundle resourceName="mySettings.title.remove.prop"
                                                       aliasResourceName="title_remove_properties"/>
                             <internal:gwtResourceBundle resourceName="mySettings.button.remove"
                                                       aliasResourceName="button_remove"/>
                             <internal:gwtResourceBundle resourceName="mySettings.title.new.prop"
                                                       aliasResourceName="title_new_property"/>
                             <internal:gwtResourceBundle resourceName="mySettings.label.newprop.fieldname"
                                                       aliasResourceName="label_fieldname_new_property"/>
                             <internal:gwtResourceBundle resourceName="mySettings.label.newprop.fieldvalue"
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



