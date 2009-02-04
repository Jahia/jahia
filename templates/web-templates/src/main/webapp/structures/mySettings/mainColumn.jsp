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

<%@ include file="../../common/declarations.jspf" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>

<!--start content -->
<!-- Change content id to modify positioning:  #content2=columnB/mainColumn #content3=mainColumn/columnA   content4=mainColumn only #content5=50%columnB/50% mainColumn -->
<div id="content2">
    <div class="mainColumn"><!--start spaceContent -->

        <h2><utility:resourceBundle resourceName="mySettings.title"/></h2>

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

        <div style="margin-top:15px;" id="addproperty">
            <a href="${requestScope.currentPage.url}" title="<utility:resourceBundle
            resourceName='backToPreviousPage' defaultValue="Back to previous page"/>"><utility:resourceBundle
                    resourceName='backToPreviousPage'
                    defaultValue="Back to previous page"/></a>
        </div>
    </div>
    <!--stop space content-->
</div>