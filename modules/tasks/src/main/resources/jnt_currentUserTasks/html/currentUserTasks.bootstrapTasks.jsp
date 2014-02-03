<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="task" type="org.jahia.services.workflow.WorkflowTask"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<template:addResources type="javascript" resources="jquery.min.js,admin-bootstrap.js,jquery.blockUI.js,bootstrap-filestyle.min.js,jquery.metadata.js,workInProgress.js"/>
<template:addResources type="javascript" resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js,datatables/dataTables.bootstrap-ext.js"/>
<template:addResources type="css" resources="admin-bootstrap.css,datatables/css/bootstrap-theme.css,tablecloth.css"/>
<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>

<template:addResources>
    <script type="text/javascript">
        $(document).ready(function () {
            $(":file").filestyle({classButton: "btn",classIcon: "icon-folder-open"/*,buttonText:"Translation"*/});
        });
    </script>
    <script type="text/javascript">
        $(document).ready(function () {
            $('#userTasks_table').dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "iDisplayLength":10,
                "sPaginationType": "bootstrap",
                "aaSorting": [] //this option disable sort by default, the user steal can use column names to sort the table
            });
        });
    </script>
</template:addResources>

<script type="text/javascript">
    var ready = true;
    <c:choose>
    <c:when test="${not empty modeDispatcherId}">
    <c:url  var="reloadurl" value="${url.basePreview}${currentNode.parent.path}.html.ajax">
    <c:forEach items="${param}" var="p">
    <c:param name="${p.key}" value="${p.value}"/>
    </c:forEach>
    </c:url>
    <c:set var="identifierName" value="\#${modeDispatcherId}"/>
    </c:when>
    <c:otherwise>
    <c:url  var="reloadurl" value="${url.basePreview}${currentNode.path}.html.ajax">
    <c:forEach items="${param}" var="p">
    <c:param name="${p.key}" value="${p.value}"/>
    </c:forEach>
    </c:url>
    <c:set var="identifierName" value="#currentUserTasks${currentNode.identifier}"/>
    </c:otherwise>
    </c:choose>
    function sendNewStatus(uuid, task, state, finalOutcome) {
        if (ready) {
            ready = false;
            $(".taskaction-complete").addClass("taskaction-disabled");
            $(".taskaction").addClass("taskaction-disabled");
            post = function () {
                $.post('<c:url value="${url.base}"/>' + task, {"jcrMethodToCall":"put","state":state,"finalOutcome":finalOutcome,"form-token":document.forms['tokenForm_' + uuid].elements['form-token'].value}, function() {
                    $('${identifierName}').load('${reloadurl}',null,function() {
                        $("#taskdetail_"+uuid).css("display","block");
                    });
                }, "json");
            }

            if ($("#taskDataForm_"+uuid).size() > 0) {
                $("#taskDataForm_"+uuid).ajaxSubmit( {
                    success: post
                });
            } else {
                post()
            }
        }
    };
    function sendNewAssignee(uuid, task, key) {
        if (ready) {
            ready = false;
            $(".taskaction-complete").addClass("taskaction-disabled");
            $(".taskaction").addClass("taskaction-disabled");
            $.post('<c:url value="${url.base}"/>' + task, {"jcrMethodToCall":"put","state":"active","assigneeUserKey":key,"form-token":document.forms['tokenForm_' + uuid].elements['form-token'].value}, function() {
                $('${identifierName}').load('${reloadurl}',null,function(){
                    $("#taskdetail_"+uuid).css("display","block");
                });
            }, "json");
        }
    };

    function switchTaskDisplay(identifier) {
        $(".taskdetail").each(function () {
            if (!$(this).is("#taskdetail_" + identifier)) {
                $(this).slideUp("medium");
            }
        });
        $("#taskdetail_" + identifier).slideToggle("medium");
    }

</script>

<h1>Bootstrap tasks</h1>
<div id="tasklist">
    <div id="${user.UUID}">
        <c:set value="${currentNode.properties['displayState'].boolean}" var="dispState"/>
        <c:set value="${currentNode.properties['displayDueDate'].boolean}" var="dispDueDate"/>
        <c:set value="${currentNode.properties['displayLastModifiedDate'].boolean}" var="dispLastModifiedDate"/>
        <c:set value="${currentNode.properties['displayAssignee'].boolean}" var="dispAssignee"/>
        <c:set value="${currentNode.properties['displayCreator'].boolean}" var="dispCreator"/>
        <fieldset class="well">
            <table cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered" id="userTasks_table">
                <thead>
                <tr>
                    <th><fmt:message key="mix_title.jcr_title"/></th>
                    <th><fmt:message key="label.owner"/></th>
                    <th><fmt:message key="mix_createdBy.jcr_createdBy"/></th>
                    <th><fmt:message key="jnt_task.state"/></th>
                </tr>
                </thead>
                <tbody>
                <%@include file="userTasksTableRow.jspf" %>
                </tbody>
            </table>
        </fieldset>
        <div class="clear">

        </div>
    </div>
</div>