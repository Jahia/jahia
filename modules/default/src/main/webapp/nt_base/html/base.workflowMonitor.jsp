<%@ page import="org.jahia.services.workflow.WorkflowService" %>
<%@ page import="org.jahia.services.workflow.HistoryWorkflow" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jahia.services.workflow.HistoryWorkflowTask" %>
<%@ page import="java.util.Locale" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="workflow" uri="http://www.jahia.org/tags/workflow" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
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

<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="javascript" resources="jquery.fancybox.js"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>

<workflow:activeWorkflow node="${currentNode}" var="activeWorkflows" locale="${currentResource.locale}" />
<c:forEach items="${activeWorkflows}" var="currentActiveWorkflow">
    <c:if test="${currentActiveWorkflow.workflowDefinition.workflowType eq currentResource.moduleParams.workflowType}">
        <c:set var="activeWorkflow" value="${currentActiveWorkflow}"/>
        <c:set var="workflowDefinition" value="${currentActiveWorkflow.workflowDefinition}"/>

        <script type="text/javascript">
            $(document).ready(function () {
                $(".workflowStartLink").hide();
                $(".workflowExecuteLink").show();
            });
        </script>
    </c:if>
</c:forEach>
<c:if test="${empty activeWorkflow}">
    <c:if test="${not currentNode.locked}">
        <workflow:workflowsForNode checkPermission="false" node="${currentNode}" var="workflowDefinitions"
                                   workflowAction="${currentResource.moduleParams.workflowType}" locale="${currentResource.locale}"/>
        <c:forEach items="${workflowDefinitions}" var="currentWorkflowDefinition">
            <c:set var="workflowDefinition" value="${currentWorkflowDefinition}"/>
        </c:forEach>
        <c:if test="${empty workflowDefinitions}">
            <fmt:message key="label.workflow.noWorkflowSet"/>: ${currentResource.moduleParams.workflowType}
        </c:if>
        <script type="text/javascript">
            $(document).ready(function () {
                $(".workflowStartLink").show();
                $(".workflowExecuteLink").hide();
            });
        </script>
    </c:if>
    <c:if test="${currentNode.locked}">
        <fmt:message key="label.workflow.locked"/>

        <script type="text/javascript">
            $(document).ready(function () {
                $(".workflowStartLink").hide();
                $(".workflowExecuteLink").hide();
            });
        </script>
    </c:if>
</c:if>

<c:if test="${not empty workflowDefinition}">
<workflow:tasksForNode node="${currentNode}" var="tasksForNode" locale="${currentResource.locale}"/>
<jsp:useBean id="tasks" class="java.util.HashMap"/>
<c:forEach items="${tasksForNode}" var="task" varStatus="status">
    <c:if test="${task.workflowDefinition == workflowDefinition}">
        <c:set target="${tasks}" property="${task.name}" value="${task}"/>
    </c:if>
</c:forEach>

<p>
    <a id="workflowImageLink${fn:replace(currentNode.identifier,'-','')}${fn:replace(workflowDefinition.key,'-','')}"
       href="#workflowImage${fn:replace(currentNode.identifier,'-','')}${fn:replace(workflowDefinition.key,'-','')}"><fmt:message key="label.workflow.viewWorkflowStatus"/></a>
    <script type="text/javascript">
        var viewWorkflowStatus = function() {
            $("#workflowImageLink${fn:replace(currentNode.identifier,'-','')}${fn:replace(workflowDefinition.key,'-','')}").click();
        }
    </script>

</p>
<jsp:useBean id="historyTasks" class="java.util.HashMap"/>
<c:if test="${not empty activeWorkflow}">
    <c:if test="${currentResource.moduleParams.showHistory == 'true'}">
        <workflow:workflowHistory var="history" workflowId="${activeWorkflow.id}"
                                  workflowProvider="${activeWorkflow.provider}" locale="${currentResource.locale}"/>
        <fmt:message key="label.workflow.history"/>:
        <ul>
            <c:forEach items="${history}" var="historyTask">
                <c:set target="${historyTasks}" property="${historyTask.name}" value="${historyTask}"/>
                <c:if test="${not empty historyTask.endTime}">
                    <li>
                            ${historyTask.displayName}
                        <ul>
                            <li><fmt:message key="label.workflow.user"/>: ${historyTask.user}</li>
                            <li><fmt:message key="label.workflow.duration"/>: ${historyTask.duration/1000}s</li>
                            <li><fmt:message key="label.workflow.startDate"/>: <fmt:formatDate value="${historyTask.startTime}"
                                                                                               type="both" dateStyle="medium" timeStyle="medium" /></li>
                            <li><fmt:message key="label.workflow.endDate"/>: <fmt:formatDate value="${historyTask.endTime}"
                                                                                             type="both" dateStyle="medium" timeStyle="medium" /></li>
                            <li><fmt:message key="label.workflow.outcome"/>: ${historyTask.displayOutcome}</li>
                        </ul>
                    </li>
                </c:if>
            </c:forEach>
        </ul>
    </c:if>
    <fmt:message key="label.workflow.openTasks"/>:
    <ul>
        <c:forEach items="${activeWorkflow.availableActions}" var="action">
            <c:if test="${(empty currentResource.moduleParams.task) or (!empty currentResource.moduleParams.task and currentResource.moduleParams.task == action.name)}">
                <li>
                        ${action.displayName} <c:if test="${not empty tasks[action.name]}"> - <a class="workflowLink"
                                                                                                 id="linktask${currentNode.identifier}-${tasks[action.name].id}"
                                                                                                 href="#task${currentNode.identifier}-${tasks[action.name].id}"><fmt:message key="label.workflow.executeTask"/></a></c:if>
                    <ul>
                        <li><fmt:message key="label.workflow.startDate"/>: <fmt:formatDate value="${action.createTime}" type="both" dateStyle="medium" timeStyle="medium" /></li>
                        <li><fmt:message key="label.workflow.dueDate"/>: <fmt:formatDate value="${action.dueDate}" type="both" dateStyle="medium" timeStyle="medium" /></li>
                    </ul>
                </li>
            </c:if>
        </c:forEach>
    </ul>
</c:if>

<div style="display:none">
    <div id="workflowImage${fn:replace(currentNode.identifier,'-','')}${fn:replace(workflowDefinition.key,'-','')}">
        <div id="workflowImageDiv${fn:replace(currentNode.identifier,'-','')}${fn:replace(workflowDefinition.key,'-','')}"
             style="position:relative;">
            <div style="height:50px;"></div>
            <img src="<c:url value='/cms/wfImage?workflowKey=${workflowDefinition.provider}:${workflowDefinition.key}&language=${currentResource.locale}'/>"/>
            <div style="height:50px;"></div>

            <c:forEach items="${activeWorkflow.availableActions}" var="task"
                       varStatus="status">
                <div id="running${task.id}" class="runningtask-div"
                     style="position:absolute;display:none;border-radius: 15px;background-color:red;opacity:0.2;cursor:pointer;"
                     onmouseover="$('#runningInfo${task.id}').show()"
                     onmouseout="$('#runningInfo${task.id}').hide()"
                     onclick="$('#linktask${currentNode.identifier}-${task.id}').click()">
                </div>
                <div id="runningInfo${task.id}"
                     style="display:none;position:absolute;border:2px solid black;background-color:white">
                    <ul>
                        <li><fmt:message key="label.workflow.startDate"/>: <fmt:formatDate value="${task.createTime}" type="both" dateStyle="medium" timeStyle="medium" /></li>
                        <li><fmt:message key="label.workflow.dueDate"/>: <fmt:formatDate value="${task.dueDate}" type="both" dateStyle="medium" timeStyle="medium" /></li>
                    </ul>
                </div>
            </c:forEach>
            <c:forEach items="${historyTasks}" var="task" varStatus="status">
                <c:if test="${not empty task.value.endTime}">
                    <div id="history${task.value.actionId}" class="historytask-div"
                         style="position:absolute;display:none;border-radius: 15px;background-color:green;opacity:0.5;"
                         onmouseover="$('#historyInfo${task.value.actionId}').show()"
                         onmouseout="$('#historyInfo${task.value.actionId}').hide()">
                    </div>
                    <div id="historyInfo${task.value.actionId}"
                         style="display:none;position:absolute;border:2px solid black;background-color:white">
                        <ul>
                            <li><fmt:message key="label.workflow.user"/>: ${task.value.user}</li>
                            <li><fmt:message key="label.workflow.duration"/>: ${task.value.duration/1000}s</li>
                            <li><fmt:message key="label.workflow.startDate"/>: <fmt:formatDate value="${task.value.startTime}"
                                                                                               type="both" dateStyle="medium" timeStyle="medium" /></li>
                            <li><fmt:message key="label.workflow.endDate"/>: <fmt:formatDate value="${task.value.endTime}"
                                                                                             type="both" dateStyle="medium" timeStyle="medium" /></li>
                            <li><fmt:message key="label.workflow.outcome"/>: ${task.value.displayOutcome}</li>
                        </ul>
                    </div>
                </c:if>
            </c:forEach>

        </div>
    </div>
</div>

<script>
    function startWorkflow(process) {
        $.post("<c:url value='${url.base}${functions:escapePath(currentNode.path)}.startWorkflow.do'/>", {"process":process},
                function (result) {
                    location.reload();
                },
                'json'
        );
    }


    function executeTask(action, outcome) {
        $.post("<c:url value='${url.base}${functions:escapePath(currentNode.path)}.executeTask.do'/>", {"action":action, "outcome":outcome},
                function (result) {
                    location.reload();
                },
                'json'
        );
    }

    var animated = false;

    function loop(value) {
        $("#" + value).fadeIn("slow", function () {
            $("#" + value).fadeOut("slow", function () {
                if (animated) {
                    loop(value);
                }
            });
        });
    }

    function animateWorkflowTask${fn:replace(currentNode.identifier,'-','')}${fn:replace(workflowDefinition.key,'-','')}() {
        animated = true;
        $.post('<c:url value="${url.base}${functions:escapeJavaScript(currentNode.path)}.getWorkflowTasks.do"/>', {'workflowKey':'${workflowDefinition.provider}:${workflowDefinition.key}'}, function (result) {
            <c:forEach items="${activeWorkflow.availableActions}" var="task" varStatus="status">
            coords = result['${task.name}'];
            $("#running${task.id}").css('left', coords[0] + "px");
            $("#running${task.id}").css('top', (parseInt(coords[1]) + 50) + "px");
            $("#running${task.id}").css('width', coords[2] + "px");
            $("#running${task.id}").css('height', coords[3] + "px");
            $("#runningInfo${task.id}").css('left', coords[0] + "px");
            $("#runningInfo${task.id}").css('top', (parseInt(coords[1]) + 50 + parseInt(coords[3])) + "px");
            $('#running${task.id}').fadeIn();
            </c:forEach>

            <c:forEach items="${tasks}" var="task" varStatus="status">
            loop('running${task.value.id}');
            </c:forEach>

            <c:forEach items="${historyTasks}" var="task" varStatus="status">
            coords = result['${task.key}'];
            $("#history${task.value.actionId}").css('left', coords[0] + "px");
            $("#history${task.value.actionId}").css('top', (parseInt(coords[1]) + 50) + "px");
            $("#history${task.value.actionId}").css('width', coords[2] + "px");
            $("#history${task.value.actionId}").css('height', coords[3] + "px");
            $("#historyInfo${task.value.actionId}").css('left', coords[0] + "px");
            $("#historyInfo${task.value.actionId}").css('top', (parseInt(coords[1]) + 50 + parseInt(coords[3])) + "px");
            $('#history${task.value.actionId}').fadeIn();
            </c:forEach>
        }, 'json');
    }

    function stopAnimateWorkflowTask() {
        animated = false;
        $('.runningtask-div').hide();
        $('.historytask-div').hide();
    }

    $(document).ready(function () {

        $("#workflowImageLink${fn:replace(currentNode.identifier,'-','')}${fn:replace(workflowDefinition.key,'-','')}").fancybox({
            'onComplete':animateWorkflowTask${fn:replace(currentNode.identifier,'-','')}${fn:replace(workflowDefinition.key,'-','')},
            'onCleanup':stopAnimateWorkflowTask
        });

        $(".workflowLink").fancybox();
    });


</script>
<c:if test="${empty activeWorkflow}">
    <c:choose>
        <c:when test="${not empty workflowDefinition.formResourceName}">
            <a class="workflowLink" id="workflowlink${currentNode.identifier}-${workflowDefinition.key}" href="#workflow${currentNode.identifier}-${workflowDefinition.key}"><fmt:message key="label.workflow.startWorkflow"/> </a>

            <script type="text/javascript">
                var startCurrentWorkflow = function() {
                    $('#workflowlink${currentNode.identifier}-${workflowDefinition.key}').click();
                }
            </script>

            <div style="display:none;">
                <div id="workflow${currentNode.identifier}-${workflowDefinition.key}" class="workflowformdiv popupSize">
                        <%--<c:set var="workflowStartFormWFCallbackJS">alert("callback");</c:set>--%>
                    <c:url value="${url.current}.ajax" var="myUrl"/>
                    <template:include view="contribute.workflow">
                        <template:param name="resourceNodeType" value="${workflowDefinition.formResourceName}"/>
                        <template:param name="workflowStartForm"
                                        value="${workflowDefinition.provider}:${workflowDefinition.key}"/>
                        <template:param name="workflowStartFormWFName" value="${workflowDefinition.displayName}"/>
                        <template:param name="workflowStartAction" value="${url.base}${functions:escapePath(currentNode.path)}.startWorkflow.do"/>
                    </template:include>
                </div>
            </div>
        </c:when>

        <c:otherwise>
            <script type="text/javascript">
                var startCurrentWorkflow = function() {
                    startWorkflow('${workflowDefinition.provider}:${workflowDefinition.key}');
                }
            </script>
            <a href="#" onclick="startWorkflow('${workflowDefinition.provider}:${workflowDefinition.key}')"><fmt:message key="label.workflow.startWorkflow"/></a>
        </c:otherwise>
    </c:choose>
</c:if>

<c:forEach items="${tasks}" var="entry">
    <c:set value="${entry.value}" var="task"/>
    <div style="display:none">
        <div id="task${currentNode.identifier}-${task.id}" class="taskformdiv popupSize">
            <c:choose>
                <c:when test="${not empty task.formResourceName}">
                    <c:set var="workflowTaskFormTask" value="${task}" scope="request"/>
                    <c:url value="${url.current}.ajax" var="myUrl"/>
                    <template:include view="contribute.workflow">
                        <template:param name="resourceNodeType" value="${task.formResourceName}"/>
                        <template:param name="workflowTaskForm" value="${task.provider}:${task.id}"/>
                        <template:param name="workflowTaskFormTaskName" value="${task.name}"/>
                    </template:include>
                </c:when>
                <c:otherwise>
                    <div class="FormContribute">
                        <form>
                            <fieldset>
                                <legend>${task.displayName}</legend>
                                <div class="divButton">
                                    <c:forEach items="${task.outcomes}" var="outcome" varStatus="status">
                                        <button type="button" class="form-button workflowaction"
                                                onclick="executeTask('${task.provider}:${task.id}', '${outcome}')"><span
                                                class="icon-contribute icon-accept"></span>&nbsp;<span>${task.displayOutcomes[status.index]}</span>
                                        </button>
                                    </c:forEach>
                                </div>
                            </fieldset>
                        </form>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

</c:forEach>
</c:if>