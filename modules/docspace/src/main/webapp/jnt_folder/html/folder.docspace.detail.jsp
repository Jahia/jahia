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

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="docspace.css,files.css,toggle-docspace.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>

<template:addResources type="javascript" resources="jquery.cuteTime.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.fancybox.pack.js"/>
<template:addResources type="javascript" resources="ajaxreplace.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<c:set value="${jcr:hasPermission(currentNode, 'write')}" var="hasWriteAccess"/>
<script>
    $(document).ready(function() {
        $("#ckeditorEditDescription").editable(function (value, settings) {
            var url = $(this).attr('jcr:url');
            var submitId = $(this).attr('jcr:id');
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post(url, data, null, "json");
            return(value);
        }, {
            type : 'ckeditor',
            onblur : 'ignore',
            submit : 'Ok',
            cancel : 'Cancel',
            tooltip : 'Click to edit'
        });

        $("#actions").click(function() {
            if ($(this).hasClass('delete')) {
                if (confirm("Do you REALLY want to delete this docspace with ALL related sub-docspaces and files?")) {
                    var data = {};
                    data['methodToCall'] = 'delete';
                    $.post('${url.base}${currentNode.path}', data, function () {
                        window.location.href = '${url.base}${currentNode.parent.path}.html';
                    }, "json");
                } else {
                    $(this).val("");
                }
            }
        });

        $("#createSubDocspace").submit(function() {
            if ($("#docspacetitle").val().length < 1) {
                $("#login_error").show();
                $.fancybox.resize();
                return false;
            }

            $.fancybox.showActivity();
            $.ajax({
                type        : "POST",
                cache       : false,
                url         : '${url.base}${currentNode.path}/' + noAccent($('#docspacetitle').val().replace(' ', '')),
                data        : $(this).serializeArray(),
                success     : function(data) {
                    $.get('${url.base}${currentNode.path}.docspace.html', null, function(data) {
                        $("#documentList${currentNode.identifier}").html(data);
                    });
                    $.fancybox.resize();
                    $.fancybox.center();
                    $.fancybox.close();
                }
            });

            return false;
        });

        $("#showCreateSubDocspace").fancybox({
            'scrolling'          : 'no',
            'titleShow'          : false,
            'hideOnContentClick' : false,
            'showCloseButton'    : true,
            'overlayOpacity'     : 0.6,
            'transitionIn'        : 'none',
            'transitionOut'        : 'none',
            'centerOnScroll'     : true,

            'onClosed'           : function() {
                $("#login_error").hide();
            }
        });
        $('#publish').click(function() {
            $.post('${url.base}${currentNode.path}.publishFile.do', $(this).serializeArray(), null, "json");
            return false;
        });
        $('#publishAll').click(function() {
            $.post('${url.base}${currentNode.path}.publishFile.do?publishChildren=true', $(this).serializeArray(), null, "json");
            return false;
        });
    });

    function noAccent(chaine) {
        temp = chaine.replace(/[àâä]/gi, "a");
        temp = temp.replace(/[éèêë]/gi, "e");
        temp = temp.replace(/[îï]/gi, "i");
        temp = temp.replace(/[ôö]/gi, "o");
        temp = temp.replace(/[ùûü]/gi, "u");
        var t = "";
        for (var i = 0; i < temp.length; i++) {
            if (temp.charCodeAt(i) > 47 && temp.charCodeAt(i) < 123) t += temp.charAt(i);
        }
        return t;
    }
</script>
<div>
    <a class="docspaceBack" href="${url.base}${currentNode.parent.path}.html"><fmt:message
            key="docspace.label.back"/> ${currentNode.parent.name}</a>
    <c:if test="${hasWriteAccess}">
        <a href="#" id="publish" title="Publish" class="docspaceBack"><fmt:message
                key="docspace.label.docspace.publish"/></a>
        <a href="#" id="publishAll" title="Publish All" class="docspaceBack"><fmt:message
                key="docspace.label.docspace.publish.all"/></a>
        <a href="#" id="actions" title="Delete" class="delete"><fmt:message key="docspace.label.docspace.delete"/></a>
    </c:if>
    <div class='clear'></div>
</div>

<div>

    <div class="boxdocspace "><!--start boxdocspace -->
        <div class="boxdocspacepadding16 boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border">

                    <div class="imagefloatleft">
                        <div class="itemImage itemImageLeft"><img alt=""
                                                                  src="${url.currentModule}/css/img/docspacebig.png"/>
                        </div>
                    </div>
                    <h3><fmt:message key="docspace.label.docspace"/>&nbsp;<jcr:nodeProperty node="${currentNode}"
                                                                                       name="jcr:title"/></h3>

                    <p class="clearMaringPadding docspacedate"><fmt:message key="docspace.label.creation"/>
                        <jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
                        <fmt:formatDate value="${created.time}" pattern="yyyy/MM/dd HH:mm"/>
                    </p>

                    <p class="clearMaringPadding docspaceauthor"><a
                            href="#"><fmt:message
                            key="docspace.label.document.createdBy"/>&nbsp;${currentNode.properties['jcr:createdBy'].string}</a>
                    </p>

                    <div class="clear"></div>
                    <c:if test="${hasWriteAccess}">
                    <div jcr:id="jcr:description" id="ckeditorEditDescription"
                         jcr:url="${url.base}${currentNode.path}"></c:if>
                        <div class="clear"></div>
                        <c:if test="${not empty currentNode.properties['jcr:description'].string}">${currentNode.properties['jcr:description'].string}</c:if>
                        <c:if test="${empty currentNode.properties['jcr:description'].string and hasWriteAccess}">Add a description (click here)</c:if>
                        <c:if test="${hasWriteAccess}">
                    </div>
                    </c:if>
                    <hr/>
                    <template:option node="${currentNode}" template="hidden.tags"
                                     nodetype="jmix:tagged"/><template:option node="${currentNode}"
                                                                              template="hidden.addTag"
                                                                              nodetype="jmix:tagged"/>
                    <!--stop boxdocspace -->
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop boxdocspace -->
    <div class='clear'></div>
</div>
<div>
    <h4 class="boxdocspace-title2"><fmt:message key="docspace.label.list.workspaces.title"/></h4>
    <c:if test="${hasWriteAccess}">
        <div class="boxdocspace"><!--start boxdocspace -->
            <div class="boxdocspacegrey boxdocspacepadding10 boxdocspacemarginbottom16">
                <div class="boxdocspace-inner">
                    <div class="boxdocspace-inner-border">
                        <div class="floatleft uploadfile">
                            <form action="${currentNode.name}/*" method="POST" name="uploadFile"
                                  enctype="multipart/form-data">
                                <span><strong><fmt:message key="docspace.label.upload.files"/></strong></span>
                                <input type="hidden" name="nodeType" value="jnt:file"/>
                                <input type="hidden" name="redirectTo"
                                       value="${url.base}${renderContext.mainResource.node.path}"/>
                                <input type="hidden" name="targetDirectory" value="${currentNode.path}"/>
                                <input type="file" name="file">
                                <input type="hidden" name="version" value="true"/>
                                <input class="button" type="submit" id="upload" value="Upload"/>
                            </form>
                        </div>
                        <div class="floatright"><label><fmt:message key="docspace.label.subdocspace.new"/></label>
                            <a id="showCreateSubDocspace" href="#divCreateSubDocspace"><img alt="Create Sub Docspace"
                                                                                            src="${url.currentModule}/css/img/create-sub-docspace-medium.png"/></a>
                        </div>
                        <div class="clear"></div>
                    </div>
                    <!--stop formSearchTop-->

                </div>
            </div>
        </div>
    </c:if>
</div>
<!--stop boxdocspace -->

<div class='clear'></div>
<c:if test="${hasWriteAccess}">
    <div id="divCreateSubDocspace">
        <div class="popup-bodywrapper">
            <h3 class="boxdocspace-title"><fmt:message key="docspace.label.subdocspace.new"/></h3>

            <form class="formDocspace" id="createSubDocspace" method="post" action="">
                <input type="hidden" name="autoCheckin" value="true">
                <input type="hidden" name="nodeType" value="jnt:folder">
                <input type="hidden" name="jcr:mixinTypes" value="jmix:tagged"/>
                <input type="hidden" name="jcr:mixinTypes" value="mix:title"/>
                <input type="hidden" name="jcr:mixinTypes" value="jmix:pager"/>
                <fieldset>
                    <legend><fmt:message key="docspace.label.subdocspace.creation"/></legend>
                    <p id="login_error" style="display:none;">Please, enter data</p>

                    <p><label for="docspacetitle" class="left"><fmt:message key="docspace.label.title"/></label>
                        <input type="text" name="jcr:title" id="docspacetitle" class="field" value=""
                               tabindex="20"/></p>


                    <p><label for="docspacedesc" class="left"><fmt:message
                            key="docspace.label.description"/> :</label>
                        <textarea name="jcr:description" id="docspacedesc" cols="45" rows="3"
                                  tabindex="21"></textarea></p>
                    <input class="button" type="button" value="<fmt:message key="docspace.label.workspace.create"/>"
                           tabindex="28"
                           id="docspacecreatebutton" onclick="$('#createSubDocspace').submit();">
                </fieldset>
            </form>
        </div>
    </div>
</c:if>


