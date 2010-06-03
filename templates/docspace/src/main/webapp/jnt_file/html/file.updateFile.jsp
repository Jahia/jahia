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
<template:addResources type="css" resources="jquery.fancybox.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>

<template:addResources type="javascript" resources="jquery.cuteTime.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<c:set value="${jcr:hasPermission(currentNode, 'write')}" var="hasWriteAccess"/>
<c:if test="${hasWriteAccess}">
    <div class="boxdocspace"><!--start boxdocspace -->
        <div class="boxdocspacegrey boxdocspacepadding10 boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border">
                    <form action="${url.base}${currentNode.parent.path}/*" method="POST" name="uploadFile"
                          enctype="multipart/form-data">
                        <input type="hidden" name="nodeType" value="jnt:file"/>
                        <input type="hidden" name="redirectTo"
                               value="${url.base}${renderContext.mainResource.node.path}"/>
                        <input type="hidden" name="newNodeOutputFormat" value="docspace.html"/>
                        <input type="hidden" name="targetDirectory" value="${currentNode.parent.path}"/>
                        <input type="hidden" name="version" value="true"/>
                        <label><fmt:message key="docspace.label.document.add.version"/></label>

                        <input type="file" name="file">
                        <input class="button" type="submit" id="upload" value="Upload"/>
                    </form>
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
</c:if>