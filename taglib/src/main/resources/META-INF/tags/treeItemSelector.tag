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
<%@ tag body-content="empty" description="Renders the trigger link and the tree control to select an item." %>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted item value with." %>
<%@ attribute name="displayFieldId" required="false" type="java.lang.String"
              description="The input field name and ID to synchronize the seletcted item display title with." %>
<%@ attribute name="fieldIdIncludeChildren" required="false" type="java.lang.String"
              description="The ID and name of the include children input field." %>
<%@ attribute name="displayIncludeChildren" required="false" type="java.lang.Boolean"
              description="Do we need to show the include children checkbox? [true]" %>
<%@ attribute name="includeChildren" type="java.lang.Boolean"
              description="The initial value for the include children checkbox field. [true]" %>
<%@ attribute name="label" required="false" type="java.lang.String"
              description="The trigger link text." %>
<%@ attribute name="includeChildrenLabel" required="false" type="java.lang.String"
              description="The include children checkbox text." %>
<%@ attribute name="onSelect" required="false" type="java.lang.String"
              description="The JavaScript function to be called after an item is selectd. Three paramaters are passed as arguments: node identifier, node path and display name. If the function retuns true, the value will be also set into the field value. Otherwise nothing will be done by this tag." %>
<%@ attribute name="nodeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of node types to filter out the tree. Empty by default, i.e. all nodes will be displayed." %>
<%@ attribute name="selectableNodeTypes" required="false" type="java.lang.String"
              description="Comma-separated list of node types that can be selected in the tree. Empty by default, i.e. all nodes will be selectable." %>
<%@ attribute name="root" required="false" type="java.lang.String"
              description="The path of the root node for the tree. [current site path]" %>
<%@ attribute name="valueType" required="false" type="java.lang.String"
              description="Either identifier, path or title of the selected item. This value will be stored into the target field. [path]" %>
<%@ attribute name="fancyboxOptions" required="false" type="java.lang.String"
              description="In case the options for the jQuery FancyBox plugin needs to be overridden, they should be specified here in a form {option1: value1, opttion2: value2}. If option value is a literal, please, enclose it into single quotes." %>
<%@ attribute name="treeviewOptions" required="false" type="java.lang.String"
              description="In case the options for the jQuery Treeview plugin needs to be overridden, they should be specified here in a form {option1: value1, opttion2: value2}. If option value is a literal, please, enclose it into single quotes." %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:if test="${empty requestScope['org.jahia.tags.treeItemSelector.resources']}">
	<template:addResources type="css" resources="jquery.treeview.css,jquery.fancybox.css"/>
	<template:addResources type="javascript" resources="jquery.min.js,jquery.treeview.min.js,jquery.treeview.async.jahia.js,jquery.fancybox.pack.js"/>
	<template:addResources type="inlineCss">
	div#fancy_div {
		background: #FFF;
		color: #000;
		overflow: auto;
	}
	</template:addResources>
	<template:addResources type="inlineJavaScript">
	function jahiaCreateTreeItemSelector(fieldId, displayFieldId, baseUrl, root, nodeTypes, selectableNodeTypes, valueType, onSelect, treeviewOptions, fancyboxOptions) {
		$("#" + fieldId + "-treeItemSelectorTrigger").fancybox($.extend({
			frameHeight: 600,
			frameWidth: 350,
			hideOnOverlayClick: false,
			hideOnContentClick: false,
			callbackOnShow: function () {
				var queryString = (nodeTypes.length > 0 ? "nodeTypes=" + encodeURIComponent(nodeTypes) : "") + (selectableNodeTypes.length > 0 ? "&selectableNodeTypes=" + encodeURIComponent(selectableNodeTypes) : "");
				queryString = queryString.length > 0 ? "?" + queryString : ""; 
				$("#fancy_div #" + fieldId + "-treeItemSelectorTree").treeview($.extend({
					urlBase: baseUrl,
					urlExtension: ".tree.json" + queryString,
					urlStartWith: baseUrl + root + ".treeRootItem.json" + queryString,
					callback: function (uuid, path, title) {
						var setValue = true;
						if (onSelect && (typeof onSelect == 'function')) {
					        setValue = onSelect(uuid, path, title);
					    }
					    if (setValue) {
					        document.getElementById(fieldId).value = 'title' == valueType ? title : ('identifier' == valueType ? uuid : path);
					        if (displayFieldId.length > 0) {
					        	document.getElementById(displayFieldId).value = title;
					        }
					    }
						$("#" + fieldId + "-treeItemSelectorTrigger").fancybox.close(); 
					}		
				}, treeviewOptions));
			}
		}, fancyboxOptions));
 	}
	</template:addResources>
	<c:set var="org.jahia.tags.treeItemSelector.resources" value="true" scope="request"/>
</c:if>
<c:set var="root" value="${functions:default(root, renderContext.siteNode.path)}"/>
<c:set var="displayIncludeChildren" value="${functions:default(displayIncludeChildren, 'true')}"/>
<c:if test="${empty fieldIdIncludeChildren}"><c:set var="fieldIdIncludeChildren" value="${fieldId}_includeChildren"/></c:if>
<%-- by default set includeChildren to 'true' to search in subnodes --%>
<c:set var="includeChildren" value="${functions:default(includeChildren, 'true')}"/>
<%-- resolve includeChildren either from request parameter or from the default value (note that the 'false' value is not submitted for checkbox) --%>
<c:set var="includeChildren" value="${functions:default(param[fieldIdIncludeChildren], empty paramValues[fieldId] ? includeChildren : 'false')}"/>
<c:if test="${empty label}"><c:set var="label"><fmt:message key="selectors.select"/></c:set></c:if>
<c:if test="${empty includeChildrenLabel}"><c:set var="includeChildrenLabel"><fmt:message key="selectors.includeChildren"/></c:set></c:if>
<a href="#${fieldId}-treeItemSelector" id="${fieldId}-treeItemSelectorTrigger">${fn:escapeXml(label)}</a>
<c:if test="${displayIncludeChildren}">
    &nbsp;<input type="checkbox" id="${fieldIdIncludeChildren}" name="${fieldIdIncludeChildren}" value="true" ${includeChildren ? 'checked="checked"' : ''}/>&nbsp;<label for="${fieldIdIncludeChildren}">${fn:escapeXml(includeChildrenLabel)}</label>
</c:if>
<template:addResources type="inlineJavaScript">
$(document).ready(function() { jahiaCreateTreeItemSelector("${fieldId}", "${displayFieldId}", "${url.base}", "${root}", "${nodeTypes}", "${selectableNodeTypes}", "${valueType}", ${not empty onSelect ? onSelect : 'null'}, ${not empty treeviewOptions ? treeviewOptions :  'null'}, ${not empty fancyboxOptions ? fancyboxOptions : 'null'}); });
</template:addResources>
<div id="${fieldId}-treeItemSelector" style="display:none"><ul id="${fieldId}-treeItemSelectorTree"></ul></div>