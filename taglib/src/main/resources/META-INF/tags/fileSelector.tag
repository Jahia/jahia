<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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
<%@ tag body-content="empty" description="Renders the trigger link and the tree control to select a file." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted item value with." %>
<%@ attribute name="displayFieldId" required="false" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted item display title with." %>
<%@ attribute name="label" required="false" type="java.lang.String"
              description="The trigger link text." %>
<%@ attribute name="onSelect" required="false" type="java.lang.String"
              description="The JavaScript function to be called after an item is selectd. Three paramaters are passed as arguments: node identifier, node path and display name. If the function retuns true, the value will be also set into the field value. Otherwise nothing will be done by this tag." %>
<%@ attribute name="nodeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of node types to filter out the tree. [nt:folder,nt:file,jnt:virtualsite]" %>
<%@ attribute name="selectableNodeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of node types that can be selected in the tree. [nt:file]" %>
<%@ attribute name="root" required="false" type="java.lang.String"
              description="The path of the root node for the tree. [current site path]" %>
<%@ attribute name="valueType" required="false" type="java.lang.String"
              description="Either identifier, path or title of the selected item. This value will be stored into the target field. [path]" %>
<%@ attribute name="fancyboxOptions" required="false" type="java.lang.String"
              description="In case the options for the jQuery FancyBox plugin needs to be overridden, they should be specified here in a form {option1: value1, opttion2: value2}. If option value is a literal, please, enclose it into single quotes." %>
<%@ attribute name="treeviewOptions" required="false" type="java.lang.String"
              description="In case the options for the jQuery Treeview plugin needs to be overridden, they should be specified here in a form {option1: value1, opttion2: value2}. If option value is a literal, please, enclose it into single quotes." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<c:if test="${empty label}"><c:set var="label"><fmt:message key="selectors.fileSelector.selectFile"/></c:set></c:if>
<uiComponents:treeItemSelector fieldId="${fieldId}" displayFieldId="${displayFieldId}" displayIncludeChildren="false"
	label="${label}" onSelect="${onSelect}"
	nodeTypes="${functions:default(nodeTypes, 'nt:folder,nt:file,jnt:virtualsite')}" selectableNodeTypes="${functions:default(selectableNodeTypes, 'nt:file')}"
	root="${root}" valueType="${valueType}" fancyboxOptions="${fancyboxOptions}" treeviewOptions="${treeviewOptions}"/>