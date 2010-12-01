<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="contribute-toolbar.css"/>
<script>
    var contributeParams = new Array();
    /*
     $("#delete-${currentNode.identifier}").button();
     $("#copy-${currentNode.identifier}").button();
     $("#paste-${currentNode.identifier}").button();
     */
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

    function deleteNodes() {
        var uuids = getUuids();
        if (uuids.length > 0) {
            $.post("${url.base}${renderContext.mainResource.node.path}.deleteNodes.do", {"uuids": uuids}, function(result) {
                reload();
            }, 'json');
        }
    }

    function copyNodes() {
        var uuids = getUuids();
        if (uuids.length > 0) {
            $.post("${url.base}${renderContext.mainResource.node.path}.copy.do", {"uuids": uuids}, function(result) {
                showClipboard();
            }, 'json');
        }
    }

    function cutNodes() {
        var uuids = getUuids();
        if (uuids.length > 0) {
            $.post("${url.base}${renderContext.mainResource.node.path}.cut.do", {"uuids": uuids}, function(result) {
                showClipboard();
            }, 'json');
        }
    }

    function publishNodes() {
        var uuids = getUuids();
        if (uuids.length > 0) {
            $.post("${url.base}${renderContext.mainResource.node.path}.publishNodes.do", {"uuids": uuids}, function(result) {
                reload();
            }, 'json');
        }
    }

    function pasteNodes(contributeParams) {
        $.post("${url.base}"+contributeParams.contributeTarget+".paste.do", {}, function(result) {
            reload();
            hideClipboard();
        }, 'json');
    }

    function emptyClipboard() {
        $.post("${url.base}${renderContext.mainResource.node.path}.emptyclipboard.do", {}, function(result) {
            hideClipboard();
        }, 'json');
    }

    function showClipboard() {
        $.post("${url.base}${renderContext.mainResource.node.path}.checkclipboard.do", {}, function(result) {
            $(".pastelink").show();
            $("#empty-${currentNode.identifier}").show();
            $("#clipboard-${currentNode.identifier}").html('<fmt:message key="label.clipboard.contains"/> ' + result.size +
                                                           ' element(s)</span></a>');
            $("#clipboard-${currentNode.identifier}").show();
            $("#clipboardpreview-${currentNode.identifier}").empty();
            var paths = result.paths;
                for (var i = 0; i < paths.length; i++) {
                    $.get("${url.base}" + paths[i] + ".html.ajax", {}, function(result) {
                        $("#clipboardpreview-${currentNode.identifier}").append("<div style='border:thin'>");
                        $("#clipboardpreview-${currentNode.identifier}").append(result);
                        $("#clipboardpreview-${currentNode.identifier}").append("</div>");
                    }, "html")
                }
            $("#clipboard-${currentNode.identifier}").fancybox();
        }, "json");
    }

    function hideClipboard() {
        $(".pastelink").hide();
        $("#empty-${currentNode.identifier}").hide();
        $("#clipboard-${currentNode.identifier}").hide();
    }

    function onresizewindow() {
        h = window.innerHeight- $("#contributeToolbar").height();
        $("#bodywrapper").attr("style","overflow:auto; height:"+ h +"px");
    }

</script>
<div id="contributeToolbar" >

    <div id="edit">
    <a href="#" id="delete-${currentNode.identifier}" onclick="deleteNodes();"><img src="${url.context}/icons/delete.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; "><fmt:message
            key="label.delete"/></a>
    <a href="#" id="copy-${currentNode.identifier}" onclick="copyNodes();"><img src="${url.context}/icons/copy.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; "><fmt:message key="label.copy"/></a>
    <a href="#" id="cut-${currentNode.identifier}" onclick="cutNodes();"><img src="${url.context}/icons/cut.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; "><fmt:message key="label.copy"/></a>
    <a href="#" id="publish-${currentNode.identifier}" onclick="publishNodes();"><img src="${url.context}/icons/publish.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; "><fmt:message key="label.publication"/></a>
    <a href="#" id="empty-${currentNode.identifier}" onclick="emptyClipboard();" style="display:none;"><img src="${url.context}/icons/clipboard.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; "><fmt:message
            key="label.clipboard.reset"/></a>
    <a href="#clipboardpreview-${currentNode.identifier}" id="clipboard-${currentNode.identifier}" style="display:none;"><img src="${url.context}/icons/clipboard.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; "><fmt:message
            key="label.clipboard.contains"/></a>
    <a href="${url.base}${jcr:getSystemSitePath()}/home/my-profile.html"><img src="${url.context}/icons/user.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; "><fmt:message
            key="label.goto.myTasks"/></a>

    <a href="${url.context}/engines/manager.jsp?conf=editorialcontentmanager&site=${renderContext.site.identifier}&selectedPaths=${currentNode.path}" target="_blank"><img src="${url.context}/icons/contentManager.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; "><fmt:message
            key="label.contentmanager"/></a>
    </div>
    <div style="display:none;">
    <div id="clipboardpreview-${currentNode.identifier}">
    </div>
    </div>
</div>

