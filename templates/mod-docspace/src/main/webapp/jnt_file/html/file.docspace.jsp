<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ page import="org.jahia.utils.FileUtils" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
                       resources="${url.context}/gwt/resources/ckeditor/ckeditor.js"/>

<template:addResources type="javascript" resources="jquery.cuteTime.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
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
    });
</script>
<div class='grid_12'><!--start grid_12-->
    <div class="boxdocspace "><!--start boxdocspace -->
        <div class="boxdocspacepadding16 boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border">
                    <div class="floatright">
                        <span>Back to docspace : <a
                                href="${url.base}${currentNode.parent.path}.html">${currentNode.parent.name}</a></span>

                        <form action="#" method="post">
                            <select name="actions">
                                <option>Actions</option>
                                <option>Informations</option>
                                <option>Suprimer</option>
                                <option>Telecharger</option>
                                <option>Telecharger en pdf</option>
                            </select>
                        </form>
                    </div>
                    <div class="imagefloatleft">
                        <div class="itemImage itemImageLeft"><a
                                class="<%=FileUtils.getFileIcon( ((JCRNodeWrapper)pageContext.findAttribute("currentNode")).getName()) %>"
                                href="#"></a>
                        </div>
                    </div>
                    <h3><fmt:message key="docspace.label.document.name"/> : ${currentNode.name}</h3>

                    <p class="clearMaringPadding docspacedate "><fmt:message key="label.created"/> : <fmt:formatDate
                            value="${currentNode.properties['jcr:created'].time}" pattern="yyyy/MM/dd"/>, <fmt:message
                            key="docspace.label.document.createdBy"/> : <span class="author"><a
                            href="${url.base}/users/${currentNode.properties['jcr:createdBy'].string}.html">${currentNode.properties['jcr:createdBy'].string}</a></span>
                    </p>

                    <p class="clearMaringPadding docspacedate"><fmt:message
                            key="docspace.label.document.lastModification"/> : <fmt:formatDate
                            value="${currentNode.properties['jcr:lastModified'].time}" pattern="yyyy/MM/dd"/>,
                        <fmt:message key="docspace.label.document.createdBy"/> : <span class="author"><a
                                href="${url.base}/users/${currentNode.properties['jcr:lastModifiedBy'].string}.html">${currentNode.properties['jcr:lastModifiedBy'].string}</a></span>
                    </p>
                    <br class="clear"/>

                    <p><template:option node="${currentNode}" template="hidden.tags"
                                        nodetype="jmix:tagged"/><br/><template:option node="${currentNode}"
                                                                                      template="hidden.addTag"
                                                                                      nodetype="jmix:tagged"/></p>

                    <p><template:option node="${currentNode}" template="hidden.average" nodetype="jmix:rating"/></p>
                    <!--stop boxdocspace -->
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop boxdocspace -->

    <h4 class="boxdocspace-title2">Discution et Versions </h4>

    <div class="boxdocspace-title2">

        <div class="TableActions"><!--start Form-->

            <form action="#" method="post">
                <p>
                    <a href="#formdocspaceupload" title="upload"><img class="rightside"
                                                                      src="${url.currentModule}/css/img/upload.png"
                                                                      alt="upload"/></a>
                    <a href="#formdocspacecomment" title="comment"><img class="rightside"
                                                                        src="${url.currentModule}/css/img/comment.png"
                                                                        alt="comment"/></a>
                    <label>Filtre : </label>

                    <select name="tagfilter">
                        <option>All</option>
                        <option>Comments</option>
                        <option>Versions</option>

                    </select>
                    <label> - Search: </label>
                    <input class="text" type="text" name="search" value="Search..." tabindex="4"/>
                    <input class="gobutton" type="image" src="${url.currentModule}/css/img/search-button.png"
                           tabindex="5"/>

                </p>

            </form>

        </div>
        <!--stop Form-->

    </div>


    <div class="boxdocspace">
        <div class="boxdocspacepadding10 boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border"><!--start boxdocspace -->
                    <template:option nodetype="jmix:comments" template="hidden.options.wrapper" node="${currentNode}"/>

                </div>
            </div>
        </div>
    </div>
    <!--stop boxdocspace -->

    <div class='clear'></div>
</div>
<!--stop grid_12-->


<div class='grid_4'><!--start grid_4-->
    <h4 class="boxdocspace-title">Description</h4>

    <div class="boxdocspace"><!--start boxdocspace -->
        <div class="boxdocspacepadding16 boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border">
                    <p class="clearMaringPadding">
                        <span jcr:id="jcr:description" id="ckeditorEditDescription"
                              jcr:url="${url.base}${currentNode.path}">
                        <c:if test="${not empty currentNode.properties['jcr:description'].string}">${currentNode.properties['jcr:description'].string}</c:if>
                        <c:if test="${empty currentNode.properties['jcr:description'].string}">Add a description (click here)</c:if>
                    </span>
                        Download live version : <a href="${url.baseLive}${currentNode.path}">Download</a><br>
                        Download staging version : <a href="${currentNode.url}">Download</a><br>

                    <form method="POST" name="publishFile" action="${url.base}${currentNode.path}.publishFile.do">
                        <input type="submit" name="publish file"/>
                    </form>

                    </p>
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop boxdocspace -->

    <h4 class="boxdocspace-title">Versions</h4>
    <ul class="docspacelist docspacelistversion">
        <c:set var="checkPublishedVersion" value="true"/>
        <c:forEach items="${functions:reverse(currentNode.versionsAsVersion)}" var="version" varStatus="status">
            <li>
                <c:if test="${checkPublishedVersion}">
                    <c:set var="publishedVersion" value="false"/>
                    <c:forEach items="${functions:reverse(currentNode.versionInfos)}" var="versionInfo">
                        <c:if test="${not empty versionInfo.checkinDate}">
                            <c:if test="${version.created.time.time <= versionInfo.checkinDate.time.time}">
                                <c:set var="publishedVersion" value="true"/>
                            </c:if>
                        </c:if>
                    </c:forEach>
                </c:if>
                <img class="floatleft" alt="user default icon" src="${url.currentModule}/css/img/version.png"/>
                <c:choose>
                <c:when test="${jcr:hasPermission(currentNode, 'write') or publishedVersion}">
                    <a href="${currentNode.url}?v=${version.name}">Version ${version.name}</a>
                </c:when>
                <c:otherwise>
                    Version ${version.name}
                </c:otherwise>
                </c:choose>
                <p class="docspacedate"><fmt:formatDate
                        value="${version.created.time}" pattern="yyyy/MM/dd HH:mm"/>
                    <c:if test="${publishedVersion eq 'true'}">
                        &nbsp;(published)
                        <c:set var="checkPublishedVersion" value="false"/>
                        <c:set var="publishedVersion" value="false"/>
                    </c:if>
                </p>

                <div class='clear'></div>
            </li>
        </c:forEach>
    </ul>
</div>
<!--stop grid_4-->

