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
<%--
Copyright 2002-2008 Jahia Ltd

Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
Version 1.0 (the "License"), or (at your option) any later version; you may
not use this file except in compliance with the License. You should have
received a copy of the License along with this program; if not, you may obtain
a copy of the License at

 http://www.jahia.org/license/

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
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
                    <span class="breadcrumbs"><utility:resourceBundle resourceName='youAreHere'
                                                                      defaultValue="You are here"/>:</span>
                    <ui:currentPagePath cssClassName="breadcrumbs"/>
                    <ui:languageSwitchingLinks display="horizontal" linkDisplay="flag" displayLanguageState="true"/>
                </div>
            </div>
        </div>
        <div id="pagecontent">
            <div class="content1cols">
                <div class="padding">
                    <div id="columnB">
                        <h2><utility:resourceBundle resourceName="mySettings.title"/></h2>
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
                            <a href="${requestScope.currentPage.url}" title="<utility:resourceBundle
                                                resourceName='backToPreviousPage' defaultValue="Back to previous page"/>"><utility:resourceBundle
                                    resourceName='backToPreviousPage' defaultValue="Back to previous page"/></a>
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



