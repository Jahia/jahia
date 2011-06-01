<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<c:if test="${not empty bindedComponent}">
    <c:set var="separator" value="${functions:default(currentResource.moduleParams.separator, ' ,')}"/>
    <template:addResources type="javascript" resources="jquery.js"/>
    <template:addResources type="css" resources="jquery.autocomplete.css"/>
    <template:addResources type="css" resources="thickbox.css"/>
    <template:addResources type="javascript" resources="jquery.autocomplete.js"/>
    <template:addResources type="javascript" resources="jquery.bgiframe.min.js"/>
    <template:addResources type="javascript" resources="thickbox-compressed.js"/>
    <jcr:nodeProperty node="${bindedComponent}" name="j:defaultCategory" var="assignedCategories"/>
    <c:url var="postUrl" value="${url.base}${bindedComponent.path}"/>
    <script type="text/javascript">

        function addCategory(uuid, separator) {
            var catToAddUuid = $("#categorytoadd").val();
            $.ajaxSetup({ traditional: true, cache:false });
            var isAlreadyExist = new Boolean();
            isAlreadyExist = false;
            for (i = 0; i < uuids.length; i++) {
                if (uuids[i] == catToAddUuid) {
                    isAlreadyExist = true;
                }
            }
            if ($("#categorytoadd").val() != "" && !isAlreadyExist) {
                uuids.push($("#categorytoadd").val());
                $.post("${postUrl}", {"j:defaultCategory":uuids,"jcrMethodToCall":"put","jcr:mixinTypes":"jmix:categorized"}, function(result) {
                    var catContainer = jQuery('#jahia-categories-' + uuid);
                    if (jQuery(".nocategorizeditem" + uuid).length > 0 && $(".nocategorizeditem" + uuid).is(":visible")) {
                        jQuery(".nocategorizeditem" + uuid).hide();
                        separator = '';
                    }else {
                        separator = ' ,'
                    }

                    var catDiv = $('<div></div>').attr('id','category'+catToAddUuid).attr('style','display:inline');
                    var catDisplay = jQuery('<span class="categorizeditem">' + $("#category").val() + '</span>');
                    var catLinkDelete = $('<a></a>').attr('onclick','deleteCategory(\''+ catToAddUuid +'\')').attr('class','delete').attr('href','#');

                    catContainer.append(catDiv);
                    catDiv.append(separator);
                    catDiv.append(catDisplay);
                    catDiv.append(catLinkDelete);
                    $("#category").val("");
                    $('#categorySubmit').hide();
                }, "json");
            } else {
                return false;
            }
        }
    </script>
    <c:if test="${renderContext.user.name != 'guest'}">
        <label><fmt:message key="label.add.categories"/></label>
        <input type="hidden" id="categorytoadd"/>
        <input type="text" id="category" disabled="true"/>
        <fmt:message key="label.select.category" var="categoryLabel"/>
        <ui:treeItemSelector fieldId="categorytoadd"  valueType="identifier"
                             nodeTypes="jnt:category" selectableNodeTypes="jnt:category" displayIncludeChildren="false"
                             root="${jcr:getSystemSitePath()}/categories" label="${categoryLabel}" displayFieldId="category" onSelect="function(uuid, path, title) {$('#categorytoadd').val(uuid);$('#category').val(title);$('#categorySubmit').show();return false;}"/>
        <input type="submit" title="<fmt:message key='add'/>" value="<fmt:message key='add'/>" class="button"
               onclick="addCategory('${bindedComponent.identifier}', '${separator}')" id="categorySubmit" style="display:none;">
    </c:if>
</c:if>

