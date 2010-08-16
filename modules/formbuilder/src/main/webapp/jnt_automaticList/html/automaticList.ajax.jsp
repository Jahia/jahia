<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="option" type="org.jahia.services.content.nodetypes.initializers.ChoiceListValue"--%>
<template:addResources type="css" resources="jquery.autocomplete.css"/>
<template:addResources type="css" resources="thickbox.css"/>
<template:addResources type="javascript" resources="jquery.ajaxQueue.js"/>
<template:addResources type="javascript" resources="jquery.autocomplete.js"/>
<template:addResources type="javascript" resources="jquery.bgiframe.min.js"/>
<template:addResources type="javascript" resources="thickbox-compressed.js"/>

<script>
    $(document).ready(function() {

        $("#${currentNode.name}").autocomplete("${url.initializers}", {
            dataType: "json",
            parse: function parse(data) {
                return $.map(data, function(row) {
                    return {
                        data: row,
                        value: "" + row["value"],
                        result: "" + row["name"]
                    }
                });
            },
            formatItem: function(item) {
                return "" + item["name"][0];
            },
            extraParams: {
                initializers : "${fn:split(currentNode.properties.type.string,';')[0]}",
                path : "${currentNode.path}",
                name : "type"
            }
        });
    });
</script>
<p class="field">
    <label for="${currentNode.name}">${currentNode.properties.label.string}</label>
    <input type="text" id="${currentNode.name}" name="${currentNode.name}" value="<c:if test="${not empty sessionScope.formError}">${sessionScope.formDatas[currentNode.name][0]}</c:if>"/>
</p>