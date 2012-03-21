<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<template:addResources type="javascript"
                       resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="javascript" resources="jquery.fancybox.js"/>
<template:addResources type="javascript" resources="jquery.autocomplete.js"/>
<template:addResources type="css" resources="jquery.autocomplete.css"/>

<template:addResources type="css" resources="jquery.fancybox.css"/>
<c:set var="taskType" value="${currentNode.properties['taskType'].string}"/>

<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<script type="text/javascript">
    $(document).ready(function() {

        $("a#createTasks").fancybox();


        $("#task_assignee").autocomplete("<c:url value='${url.findUser}'/>", {
                        dataType: "json",
                        cacheLength: 1,
                        parse: function (data) {
                            return $.map(data, function(row) {
                                return {
                                    data: row,
                                    value: row['username'],
                                    result: getText(row)
                                }
                            });
                        },
                        formatItem: function(item) {
                            return getText(item);
                        }
                    }).result(function(event, item, formatted) {
                        if (!item) {
                            return;
                        }
                    $('#assignee_hidden').val(item['username'])
                    });

        $("#createTaskForm").submit(function() {
            var datePicked = $("#${currentNode.name}-dueDate").val().replace(/^\s+|\s+$/g, '').replace(" ", "T");
            $("#dueDate_hidden").val(datePicked);
            return true;
        })

    });
</script>
<c:set var="title">${currentNode.properties['jcr:title'].string}</c:set>
<c:if test="${empty currentNode.properties['jcr:title'].string}"><c:set var="title"><fmt:message key="label.add.new.task"/></c:set></c:if>

<a class="aButton createTasks" id="createTasks" href="#createTasksForm"><span>${title}</span></a>

<div style="display:none">
    <div id="createTasksForm" class="popupSize">
        <h3>${title}</h3>

        <div class="Form taskForm"><!--start Form -->

            <jcr:propertyInitializers nodeType="jnt:task" name="priority" var="priorities"/>

            <template:tokenizedForm>
            <form id="createTaskForm" method="post" action="<c:url value='${url.basePreview}${bindedComponent.path}/tasks/*'/>">
                <input type="hidden" name="jcrNodeType" value="jnt:task">
                <input type="hidden" name="jcrParentType" value="jnt:tasks">
                <input type="hidden" name="type" value="${taskType}"/>
                <input type="hidden" name="jcrRedirectTo"
                       value="<c:url value='${url.base}${renderContext.mainResource.node.path}.${renderContext.mainResource.template}'/>">

                <p>
                    <label for="task_title">
                        <fmt:message key="mix_title.jcr_title"/>
                    </label>
                    <input type="text" name="jcr:title" id="task_title" class="field" value="" tabindex="16"/></p>

                <c:if test="${currentNode.properties['useDescription'].boolean}">
                    <p>
                        <label for="task_description">
                            <fmt:message key="jnt_task.description"/>
                            :</label>
                        <textarea name="description" id="task_description" class="field" value=""
                                  tabindex="17"></textarea>
                    </p>
                </c:if>

                <c:if test="${currentNode.properties['usePriority'].boolean}">
                    <p>
                        <label for="task_priority">
                            <fmt:message key="jnt_task.priority"/>
                            :</label>
                        <select name="priority" id="task_priority" class="combo" tabindex="21">
                            <c:forEach items="${priorities}" var="priority">
                                <option value="${priority.value.string}"> ${priority.displayName} </option>
                            </c:forEach>
                        </select>
                    </p>
                </c:if>
                <c:if test="${currentNode.properties['useAssignee'].boolean}">
                    <p>
                        <label for="task_assignee">
                            <fmt:message key="jnt_task.assignee"/>
                            :</label>

                        <%--<select name="assignee" id="task_assignee" class="combo" tabindex="21">--%>

                            <%--<option value=""><fmt:message key="label.noassignee"/></option>--%>
                            <%--<option value="${renderContext.user}"><fmt:message key="Me (${renderContext.user})"/></option>--%>
                            <%--<c:forEach items="${users}" var="user">--%>
                                <%--<option value="${user.value.string}"> ${user.displayName} </option>--%>
                            <%--</c:forEach>--%>
                        <%--</select>--%>

                        <input id="task_assignee" name="assigneeName" type="text" value="${startSearching}" tabindex="22"
                               onfocus="if(this.value==this.defaultValue)this.value='';"
                               onblur="if(this.value=='')this.value=this.defaultValue;" class="text-input"/>
                        <input  type="text" id="assignee_hidden" name="assigneeUserKey" style="display:none"/>

                    </p>
                </c:if>
                <c:if test="${currentNode.properties['useDueDate'].boolean}">
                    <p>
                        <label for="task_dueDate"><fmt:message key="jnt_task.dueDate"/>:</label>

                        <input  type="text" id="dueDate_hidden" name="dueDate" style="display:none"/>

                        <input ${disabled} id="${currentNode.name}-dueDate" type="text" name="dueDate-picker" id="task_dueDate"
                                           class="" value="" tabindex="24" readonly="readonly"/>
                        <ui:dateSelector fieldId="${currentNode.name}-dueDate" time="true" >
                            {dateFormat: $.datepicker.ISO_8601, showButtonPanel: true, showOn:'focus'}
                        </ui:dateSelector>

                    </p>
                </c:if>
                <input type="hidden" name="state" value="active"/>

                <div><input type="submit" id="submit" class="button" value="Create task"
                                              tabindex="28"/>
                </div>
            </form>
            </template:tokenizedForm>
        </div>
        <div class='clear'></div>
    </div>
</div>