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
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<template:template>
    <template:templateHead>
        <!-- All headers and declarations global meta and css-->
        <%@ include file="common/head_externals.jspf" %>
        <utility:applicationResources/>
    </template:templateHead>
    <template:templateBody>
        <div id="bodywrapper">
            <div id="container"><!--start container-->
                <!-- Head page -->
                <template:include page="common/header.jsp"/>
            </div>
            <!--stop container-->
            <div id="container2"><!--start container2-->
                <div id="container3"><!--start container3-->
                    <div id="wrapper"><!--start wrapper-->
                        <div id="content4"><!--start content-->
                            <div class="spaceContent"><!--start spaceContent -->
                                <template:include page="common/breadcrumb.jsp"/>
                                <div class="box">
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
                                    </c:if>
                                    <template:gwtJahiaModule id="mysettings" jahiaType="mySettings">
                                        <utility:gwtResourceBundle resourceName="mySettings.password"
                                                                   aliasResourceName="pwd"/>
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
                            </div>
                            <!--stop space content-->
                        </div>
                        <!--stopContent-->
                    </div>
                    <!--stop wrapper-->
                    <div class="clear"></div>
                </div>
                <!--stop container2-->
                <!-- footer -->
                <template:include page="common/footer.jsp"/>
                <div class="clear"></div>
            </div>
            <!--stop container3-->
        </div>
    </template:templateBody>
</template:template>




