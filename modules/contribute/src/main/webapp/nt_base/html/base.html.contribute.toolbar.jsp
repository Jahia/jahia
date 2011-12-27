<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="utils" uri="http://www.jahia.org/tags/utilityLib" %>
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
<utils:setBundle basename="JahiaContributeMode" useUILocale="true" templateName="Jahia Contribute Mode"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,jquery.fancybox.js,animatedcollapse.js"/>
<%--
<template:addResources type="css" resources="contribute-toolbar.css,jquery.fancybox.css"/>
--%>
<template:addResources type="css" resources="contribute.min.css"/>
<fmt:message key="label.noSelection" var="i18nNoSelection"/>
<c:set var="i18nNoSelection" value="${functions:escapeJavaScript(i18nNoSelection)}"/>
<template:addResources>
    <script type="text/javascript">
        var contributeParams = new Array();
        $.ajaxSetup({
            accepts: {
                script: "application/json"
            },
            cache:false
        });

        function getUuids() {
            var uuids = new Array();
            var i = 0;
            $("input:checked").each(function(index) {
                uuids[i++] = $(this).attr("name");
            });
            return uuids;
        }

        function reload() {
            for(var i=0; i < contributeParams.length; i++ ){

                $("#" + contributeParams[i].contributeReplaceTarget).load(contributeParams[i].contributeReplaceUrl, '', null);
            }
        }

        <fmt:message key="label.delete.confirm" var="i18nConfirmDelete"/>
        <fmt:message key="message.undelete.selected.confirm" var="i18nConfirmUndelete"/>
        <fmt:message key="label.comment" var="i18nComment"/>
        function deleteNodes(markForDeletion) {
            var uuids = getUuids();
            if (uuids.length > 0) {
            	var comment;
            	if (markForDeletion == true) {
                    comment = prompt('${functions:escapeJavaScript(i18nConfirmDelete)}\n${functions:escapeJavaScript(i18nComment)}:', '');
                    if (comment == null) {
                    	return false;
                    }
            	} else if (markForDeletion == false) {
                    if (!confirm('${functions:escapeJavaScript(i18nConfirmUndelete)}')) {
                    	return false;
                    }
            	} else if (!confirm('${functions:escapeJavaScript(i18nConfirmDelete)}')) {
            		return false;
            	}
            	$.post("<c:url value='${url.base}${renderContext.mainResource.node.path}.deleteNodes.do'/>", {"uuids": uuids, "markForDeletion":markForDeletion,"markForDeletionComment":comment}, function(result) {
                    reload();
                }, "json");
            } else {
                window.alert("${i18nNoSelection}");
            }
        }

        function copyNodes() {
            var uuids = getUuids();
            if (uuids.length > 0) {
                $.post("<c:url value='${url.base}${renderContext.mainResource.node.path}.copy.do'/>", {"uuids": uuids}, function(result) {
                    showClipboard();
                }, "json");
            } else {
                window.alert("${i18nNoSelection}");
            }
        }

        function cutNodes() {
            var uuids = getUuids();
            if (uuids.length > 0) {
                $.post("<c:url value='${url.base}${renderContext.mainResource.node.path}.cut.do'/>", {"uuids": uuids}, function(result) {
                    showClipboard();
                }, "json");
            } else {
                window.alert("${i18nNoSelection}");
            }
        }

        <fmt:message key="message.requestPublication.confirm" var="i18nConfirmPublish"/>
        function publishNodes(ids, confirmMsg) {
            var uuids = typeof ids == 'undefined' ? getUuids() : ids;
            if (uuids.length > 0) {
            	if (confirm(typeof confirmMsg == 'undefined' ? '${functions:escapeJavaScript(i18nConfirmPublish)}' : confirmMsg)) {
                    $.post("<c:url value='${url.base}${renderContext.mainResource.node.path}.publishNodes.do'/>", {"uuids": uuids}, function(result) {
                    	<fmt:message key="label.workflow.started" var="i18nWorkflowStarted"/>
                        window.alert("${functions:escapeJavaScript(i18nWorkflowStarted)}");
                        reload();
                    }, "json");
            	}
            } else {
                window.alert("${i18nNoSelection}");
            }
        }

        function pasteNodes(contributeParams) {
            $.post("<c:url value='${url.base}'/>"+contributeParams.contributeTarget+".paste.do", {}, function(result) {
                reload();
                hideClipboard();
            }, "json");
        }

        function emptyClipboard() {
            $.post("<c:url value='${url.base}${renderContext.mainResource.node.path}.emptyclipboard.do'/>", {}, function(result) {
                hideClipboard();
            }, "json");
        }

        function showClipboard() {
            $.post("<c:url value='${url.base}${renderContext.mainResource.node.path}.checkclipboard.do'/>", {}, function(data) {
                if (data != null && data.size > 0) {
                    var nodetypes = data.nodetypes;
                    var showPaste = true;
                    for (var j=0; j < contributeConstraintParameters.length && showPaste;j++) {
                        for (var k=0; k < nodetypes.length && showPaste;k++) {
                            if(nodetypes[k].indexOf(contributeConstraintParameters[j]) < 0){
                                showPaste = false;
                            }
                        }
                    }
                    if(showPaste) {
                        $(".pastelink").show();
                        $(".titleaddnewcontent").show();
                    }
                    $("#empty-${currentNode.identifier}").show();
                    $("#clipboard-${currentNode.identifier}").html("<fmt:message key="label.clipboard.contains"/> " + data.size +
                            ' element(s)</span></a>');
                    $("#clipboard-${currentNode.identifier}").show();
                    $("#clipboardpreview-${currentNode.identifier}").empty();
                    var paths = data.paths;
                    for (var i = 0; i < paths.length; i++) {
                        $.get("<c:url value='${url.base}'/>" + paths[i] + ".html.ajax", {}, function(result) {
                            $("#clipboardpreview-${currentNode.identifier}").append("<div style='border:thin'>");
                            $("#clipboardpreview-${currentNode.identifier}").append(result);
                            $("#clipboardpreview-${currentNode.identifier}").append("</div>");
                        }, "html")
                    }
                    $("#clipboard-${currentNode.identifier}").fancybox();
                }
            }, "json");
        }

        function hideClipboard() {
            $(".titleaddnewcontent").hide();
            $(".pastelink").hide();
            $("#empty-${currentNode.identifier}").hide();
            $("#clipboard-${currentNode.identifier}").hide();
        }

        function onresizewindow() {
            h = document.documentElement.clientHeight - $("#contributeToolbar").height();
            $("#contributewrapper").attr("style","position:relative; overflow:auto; height:"+ h +"px");
        }

        $(document).ready(function() {
            $(".fancylink").fancybox({
                'titleShow' : false,
                'autoDimensions' : false,
                'width' : 800,
                'height' : 600,
                'onComplete' : function() {
                    animatedcollapse.init();
                }
            });
        });

    </script>
</template:addResources>
<div id="contributeToolbar" >

    <div id="edit">
        <a href="<c:url value='${url.live}'/>" ><img src="<c:url value='/icons/live.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                key="label.live"/></a>
        <a href="<c:url value='${url.preview}'/>" ><img src="<c:url value='/icons/preview.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                key="label.preview"/></a>
        <span> </span>
        <c:if test="${jcr:hasPermission(currentNode, 'jcr:removeChildNodes_default')}">
            <a href="#" id="delete-${currentNode.identifier}" onclick="deleteNodes(true); return false;"><img src="<c:url value='/icons/delete.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                key="label.delete"/></a>
        </c:if>
        <a href="#" id="copy-${currentNode.identifier}" onclick="copyNodes(); return false;"><img src="<c:url value='/icons/copy.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.copy"/></a>
        <c:if test="${jcr:hasPermission(currentNode, 'jcr:removeChildNodes_default')}">
            <a href="#" id="cut-${currentNode.identifier}" onclick="cutNodes();"><img src="<c:url value='/icons/cut.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.cut"/></a>
        </c:if>
        <c:if test="${jcr:hasPermission(currentNode, 'jcr:write_default')}">
            <a href="#" id="publish-${currentNode.identifier}" onclick="publishNodes(); return false;"><img src="<c:url value='/icons/publish.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.requestPublication"/></a>
        </c:if>
        <a href="#" id="empty-${currentNode.identifier}" onclick="emptyClipboard(); return false;" style="display:none;"><img src="<c:url value='/icons/clipboard.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                key="label.clipboard.reset"/></a>
        <a href="#clipboardpreview-${currentNode.identifier}" id="clipboard-${currentNode.identifier}" style="display:none;"><img src="<c:url value='/icons/clipboard.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                key="label.clipboard.contains"/></a>
        <a href="<c:url value='${url.basePreview}${renderContext.user.localPath}.contributeTasklist.html.ajax'/>" class="fancylink"><img src="<c:url value='/icons/user.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                key="label.goto.myTasks"/></a>
        <c:choose>
            <c:when test="${jcr:isNodeType(currentNode, 'jnt:folder') || jcr:isNodeType(currentNode, 'nt:file')}">
                <c:if test="${jcr:hasPermission(currentNode,'fileManager')}">
                <c:url var="mgrUrl" value="/engines/manager.jsp">
                    <c:param name="conf" value="filemanager"/>
                    <c:param name="site" value="${renderContext.site.identifier}"/>
                    <c:param name="selectedPaths" value="${currentNode.path}"/>
                </c:url>
                <a href="${mgrUrl}" target="_blank"><img src="<c:url value='/icons/treepanel-files-manager-1616.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                        key="label.filemanager"/></a>
                </c:if>
            </c:when>
            <c:otherwise>
                <c:set var="contentPath" value="${currentNode.resolveSite.path}/contents"/>
                <c:if test="${fn:startsWith(currentNode.path,contentPath) && jcr:hasPermission(currentNode,'editorialContentManager')}">
                <c:url var="mgrUrl" value="/engines/manager.jsp">
                    <c:param name="conf" value="editorialcontentmanager"/>
                    <c:param name="site" value="${renderContext.site.identifier}"/>
                    <c:param name="selectedPaths" value="${currentNode.path}"/>
                </c:url>
                    <a href="${mgrUrl}" target="_blank"><img src="<c:url value='/icons/treepanel-content-manager-1616.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
                            key="label.contentmanager"/></a>
                </c:if>
            </c:otherwise>
        </c:choose>
        <span><fmt:message key="label.goto"/>: </span> <a href="<c:url value='${url.base}${currentNode.resolveSite.home.path}.html'/>"><img src="<c:url value='/icons/siteManager.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.siteHomepage"/></a>
        <a href="<c:url value='${url.base}${currentNode.resolveSite.path}/contents.html'/>"><img src="<c:url value='/icons/content-manager-1616.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.siteContent"/></a>
        <a href="<c:url value='${url.base}${currentNode.resolveSite.path}/files.html'/>"><img src="<c:url value='/icons/files-manager-1616.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.siteFiles"/></a>
        <a href="<c:url value='${url.logout}'/>"><img src="<c:url value='/icons/logout.png'/>" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message key="label.logout"/></a>
    </div>
    <div style="display:none;">
        <div id="clipboardpreview-${currentNode.identifier}">
        </div>
    </div>
</div>

<div style="display:none;">
    <div id="tasks" >
        <%-- Just load the resources here ! --%>
        <template:module path="${renderContext.user.localPath}" view="contributeTasklist" var="temp"/>
    </div>
</div>
<div id="contributewrapper">