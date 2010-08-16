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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="selectorType" type="org.jahia.services.content.nodetypes.SelectorType"--%>
<template:addResources type="javascript" resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>
<jcr:nodeProperty node="${currentNode}" name="image" var="image"/>
<template:addResources type="css" resources="formmaincontent.css"/>
<div class="maincontent">
    <form class="FormMainContent" action="${url.base}${currentNode.path}/*" method="post" id="${currentNode.identifier}jnt_mainContentForm">
        <input type="hidden" name="nodeType" value="jnt:mainContent"/>

        <h3 class="title"><label for="${currentNode.identifier}jnt_mainContentTitle">Title</label><input type="text"
                                                                                                         name="jcr:title"
                                                                                                         id="${currentNode.identifier}jnt_mainContentTitle"/>
        </h3>
        <div class="clear"></div>
        <p>
        <label for="file${currentNode.identifier}jnt_mainContentImage">Image</label>
        <input type="hidden" name="image" id="${currentNode.identifier}jnt_mainContentImage"/>
		<p>
        <div class="clear"></div>
        <div id="file${currentNode.identifier}jnt_mainContentImage">
            <span>add a file (file will be uploaded in your files directory before submitting the form)</span>
        </div>
        <script>
            $(document).ready(function() {
                $("#file${currentNode.identifier}jnt_mainContentImage").editable('${url.base}${currentNode.path}', {
                    type : 'ajaxupload',
                    onblur : 'ignore',
					submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>OK</button>',
					cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>Cancel</button>',
                    tooltip : 'Click to edit',
                    callback : function (data, status, original) {
                        $("#${currentNode.identifier}jnt_mainContentImage").val(data.uuids[0]);
                        $("#file${currentNode.identifier}jnt_mainContentImage").html($('<span>file uploaded</span>'));
                    }
                });
            });
        </script>
        <p>
        <div class="clear"></div>
        <label for="${currentNode.identifier}jnt_mainContentAlign">Image Alignment</label>
        <jcr:propertyInitializers var="options" nodeType="jnt:mainContent" name="align"/>
        <select name="align"
                id="${currentNode.identifier}jnt_mainContentAlign">
            <c:forEach items="${options}" var="option">
                <option value="${option.value.string}">${option.displayName}</option>
            </c:forEach>
        </select>
        <p>
        <label for="ckeditor${currentNode.identifier}jnt_mainContentBody">Body</label>
        <input type="hidden" name="body" id="${currentNode.identifier}jnt_mainContentBody"/>
        <textarea rows="50" cols="40" id="ckeditor${currentNode.identifier}jnt_mainContentBody"></textarea>
        <script>

            $(document).ready(function() {
                richTextEditors['${currentNode.identifier}jnt_mainContentBody'] = CKEDITOR.replace("ckeditor${currentNode.identifier}jnt_mainContentBody", { toolbar : 'User'});
            });

            $("#${currentNode.identifier}jnt_mainContentForm").submit(function() {
                $("#${currentNode.identifier}jnt_mainContentBody").val(richTextEditors['${currentNode.identifier}jnt_mainContentBody'].getData());
            });

            var options${currentNode.name}jnt_mainContentForm = {
                success: function() {
                    replace('${currentNode.identifier}', '${currentResource.moduleParams.currentListURL}', '');
                    $.each(richTextEditors, function(key, value) {
                        value.setData("");
                    });
                },
                dataType: "json",
                resetForm : true
            };// wait for the DOM to be loaded
            $(document).ready(function() {
                // bind 'myForm' and provide a simple callback function
                $('#${currentNode.identifier}jnt_mainContentForm').ajaxForm(options${currentNode.name}jnt_mainContentForm);
            });
        </script>

        <div class="divButton">
            <input class="button" type="submit" value="<fmt:message key="label.add.new.content.submit"/>"/>
            <input class="button" type="reset" value="<fmt:message key="label.add.new.content.reset"/>"/>
        </div>
    </form>
</div>
<br class="clear"/>