<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="tasks.css"/>

<div class="Form taskForm"><!--start Form -->

    <jcr:propertyInitializers nodeType="jnt:task" name="priority" var="priorities"/>
    <jcr:propertyInitializers nodeType="jnt:task" name="assignee" var="users"/>


    <form method="post" action="${url.base}${currentNode.path}/*">
        <input type="hidden" name="nodeType" value="jnt:task">
        <input type="hidden" name="redirectTo" value="${url.base}${renderContext.mainResource.node.path}">
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


