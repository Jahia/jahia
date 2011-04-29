<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="tasks.css"/>

<div class="Form taskForm"><!--start Form -->

    <jcr:propertyInitializers nodeType="jnt:task" name="priority" var="priorities"/>
    <jcr:propertyInitializers nodeType="jnt:task" name="assignee" var="users"/>


    <form method="post" action="<c:url value='${url.base}${currentNode.path}/*'/>">
        <input type="hidden" name="jcrNodeType" value="jnt:task">
        <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>">
        <fieldset>
            <legend>
                <fmt:message key="jnt_task.newTask"/>
            </legend>


            <p>
                <label for="task_title" class="left">
                    <fmt:message key="mix_title.jcr_title"/>
                </label>
                <input type="text" name="jcr:title" id="task_title" class="field" value="" tabindex="16"/></p>

            <p>
                <label for="task_description" class="left">
                    <fmt:message key="jnt_task.description"/>
                    :</label>
                <input type="text" name="description" id="task_description" class="field" value="" tabindex="17"/>
            </p>

            <p>
                <label for="task_priority" class="left">
                    <fmt:message key="jnt_task.priority"/>
                    :</label>
                <select name="priority" id="task_priority" class="combo" tabindex="21">
                    <c:forEach items="${priorities}" var="priority">
                        <option value="${priority.value.string}"> ${priority.displayName} </option>
                    </c:forEach>
                </select>
            </p>
            <%--<p><label for="task_dueDate" class="left">Due date:</label>--%>
            <%--<input type="text" name="dueDate" id="task_dueDate" class="field" value="" tabindex="17" /></p>--%>


            <p>
                <label for="task_assignee" class="left">
                    <fmt:message key="jnt_task.assignee"/>
                    :</label>

                <select name="assignee" id="task_assignee" class="combo" tabindex="21">
                    <c:forEach items="${users}" var="user">
                        <option value="${user.value.string}"> ${user.displayName} </option>
                    </c:forEach>
                </select>
            </p>
        </fieldset>

        <div class="divButton"><input type="submit" id="submit" class="button" value="Create task" tabindex="28"/>
        </div>
    </form>
</div>
<div class='clear'></div>


