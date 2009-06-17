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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<utility:setBundle basename="JahiaInternalResources"/>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Jahia Link Checker</title>
		<internal:gwtInit standalone="true"/>
		<internal:gwtImport module="org.jahia.ajax.gwt.module.linkchecker.LinkChecker" />
		<link href="screen.css" rel="stylesheet" type="text/css"/>
        <link href="jcg.css" rel="stylesheet" type="text/css"/>
        <link href="../../css/andromeda.css" rel="stylesheet" type="text/css"/>
	</head>
	<body>
		<div id="wrapper">
	    <div id="header">
	      <h1>Jahia</h1>
        <h2>Additional Tool: Jahia Link Checker</h2>
    	</div>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.checkLinks.label"
                                        aliasResourceName="lc_checkLinks"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.exportcsv.label"
                                        aliasResourceName="lc_exportcsv"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.link.label"
                                        aliasResourceName="lc_link"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.stop.label"
                                        aliasResourceName="lc_stop"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.pageTitle.label"
                                        aliasResourceName="lc_pageTitle"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.language.label"
                                        aliasResourceName="lc_language"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.code.label"
                                        aliasResourceName="lc_code"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.edit.label"
                                        aliasResourceName="lc_edit"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.page.label"
                                        aliasResourceName="lc_page"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.field.label"
                                        aliasResourceName="lc_field"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.details.label"
                                        aliasResourceName="lc_details"/>

            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.processed.label"
                                        aliasResourceName="lc_processed"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.checking.label"
                                        aliasResourceName="lc_checking"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.of.label"
                                        aliasResourceName="lc_of"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.linksfound.label"
                                        aliasResourceName="lc_linksfound"/>
            <internal:gwtResourceBundle resourceName="org.jahia.admin.linkChecker.invalid.label"
                                        aliasResourceName="lc_invalid"/>
            


            <internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.workflow.label"
                                        aliasResourceName="lc_workflow"/>
            <internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.versioned.label"
                                        aliasResourceName="lc_versioned"/>
            <internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.live.label"
                                        aliasResourceName="lc_live"/>
            <internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.staging.label"
                                        aliasResourceName="lc_staging"/>
            <internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.usages.notify.label"
                                        aliasResourceName="lc_notify"/>
            <internal:gwtGenerateDictionary/>
    	<br class="c"/>
			<div id="linkchecker" />
		</div>
        <div id="actionBar">
            <span class="dex-PushButton">
                <span class="first-child">
                    <a class="ico-back" href='<%=org.jahia.bin.JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
                </span>
            </span>
        </div>
	</body>
</html>