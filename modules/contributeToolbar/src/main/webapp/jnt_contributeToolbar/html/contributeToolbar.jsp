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
<c:choose>
    <c:when test="${renderContext.contributionMode}">
        <template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
        <script>
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

            function deleteNodes() {
                var uuids = getUuids();
                if (uuids.length > 0) {
                    $.post("${url.base}${renderContext.mainResource.node.path}.deleteNodes.do", {"uuids": uuids}, function(result) {
                        window.location.reload();
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
                        window.location.reload();
                    }, 'json');
                }
            }

            function pasteNodes() {
                $.post("${url.base}${renderContext.mainResource.node.path}.paste.do", {}, function(result) {
                    window.location.reload();
                }, 'json');
            }

            function emptyClipboard() {
                $.post("${url.base}${renderContext.mainResource.node.path}.emptyclipboard.do", {}, function(result) {
                    hideClipboard();
                }, 'json');
            }

            function showClipboard() {
                $.post("${url.base}${renderContext.mainResource.node.path}.checkclipboard.do", {}, function(result) {
                    $("#paste-${currentNode.identifier}").show();
                    $("#empty-${currentNode.identifier}").show();
                    $("#clipboard-${currentNode.identifier}").html("<span>Clipboard contains " + result.size +
                                                                   " element(s)</span>");
                    $("#clipboard-${currentNode.identifier}").show();
                    var paths = result.paths;
                    $("#clipboard-${currentNode.identifier}").hover(function() {
                        $("#clipboardpreview-${currentNode.identifier}").show();
                        for (var i = 0; i < paths.length; i++) {
                            $.get("${url.base}" + paths[i] + ".html", {}, function(result) {
                                $("#clipboardpreview-${currentNode.identifier}").append("<div style='border:thin'>");
                                $("#clipboardpreview-${currentNode.identifier}").append(result);
                                $("#clipboardpreview-${currentNode.identifier}").append("</div>");
                            }, "html")
                        }
                    }, function() {
                        $("#clipboardpreview-${currentNode.identifier}").empty();
                        $("#clipboardpreview-${currentNode.identifier}").hide();
                    });
                }, "json");
            }

            function hideClipboard() {
                $("#paste-${currentNode.identifier}").hide();
                $("#empty-${currentNode.identifier}").hide();
                $("#clipboard-${currentNode.identifier}").hide();
            }

            $(document).ready(function() {
                showClipboard();
            })
        </script>
        <div id="contributeToolbar">
            <button id="delete-${currentNode.identifier}" onclick="deleteNodes();"><fmt:message
                    key="label.delete"/></button>
            <button id="copy-${currentNode.identifier}" onclick="copyNodes();"><fmt:message key="label.copy"/></button>
            <button id="cut-${currentNode.identifier}" onclick="cutNodes();"><fmt:message key="label.cut"/></button>
            <button id="publish-${currentNode.identifier}" onclick="publishNodes();"><fmt:message key="label.publication"/></button>
            <button id="paste-${currentNode.identifier}" onclick="pasteNodes();" style="display:none;"><fmt:message
                    key="label.paste"/></button>
            <button id="empty-${currentNode.identifier}" onclick="emptyClipboard();" style="display:none;"><fmt:message
                    key="label.clipboard.reset"/></button>
            <a href="${url.base}${jcr:getSystemSitePath()}/home/my-profile.html"><fmt:message
                    key="label.goto.myTasks"/></a>

            <a href="${url.context}/engines/manager.jsp?conf=editorialcontentmanager&site=${renderContext.site.identifier}&selectedPaths=${currentNode.path}"><fmt:message
                    key="label.contentmanager"/></a>

            <div style="display:none" id="clipboard-${currentNode.identifier}">
            </div>
            <div style="display:none" id="clipboardpreview-${currentNode.identifier}">
            </div>

        </div>
    </c:when>
    <c:otherwise>
        <div id="contibuteToolbar">
            <button><fmt:message key="label.delete"/></button>
            <button><fmt:message key="label.copy"/></button>
            <button><fmt:message key="label.cut"/></button>
            <button><fmt:message key="label.publication"/></button>
            <button><fmt:message key="label.paste"/></button>
            <button><fmt:message key="label.clipboard.reset"/></button>
        </div>
    </c:otherwise>
</c:choose>


