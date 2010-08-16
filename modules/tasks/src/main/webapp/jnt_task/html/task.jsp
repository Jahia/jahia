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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="css" resources="tasks.css"/>

<script type="text/javascript">
    function send(task, state) {
        form = document.forms['myform'];
        form.action = '${url.base}' + task;
        form.elements.state.value = state;
        form.submit();
    }
</script>

<div class="Form taskFormConsult"><!--start Form -->
    <form method="post" name="myform" action="${url.base}${currentNode.path}">
        <input type="hidden" name="nodeType" value="jnt:task">
        <input type="hidden" name="state">


        <fieldset>
            <legend>
                <fmt:message key="jnt_task.newTask"/>
            </legend>


            <p>
                <label class="left">
                    <fmt:message key="mix_title.jcr_title"/>
                :</label>
                <span class="value">${currentNode.properties['jcr:title'].string}</span>

            <p>
                <label class="left">
                    <fmt:message key="jnt_task.description"/>
                    :</label>
                <span class="value">${currentNode.properties.description.string}</span>
            </p>

            <p>
                <label class="left">
                    <fmt:message key="jnt_task.priority"/>
                    :</label>
                <span class="right value">${currentNode.properties.priority.string}</span>
            </p>
            <%--<p><label for="task_dueDate" class="left">Due date:</label>--%>
            <%--<input type="text" name="dueDate" id="task_dueDate" class="field" value="" tabindex="17" /></p>--%>


            <p>
                <label class="left">
                    <fmt:message key="jnt_task.assignee"/>
                    :</label>
                <span class="right value">${currentNode.properties.assignee.node.name}</span>

            </p>

            <p>
                <label class="left">
                    <fmt:message key="jnt_task.state"/>
                    :</label>
                <span class="right value">${currentNode.properties.state.string}</span>
                <c:choose>
                    <c:when test="${currentNode.properties.state.string == 'active'}">
                        <span><img alt="" src="${url.currentModule}/images/right_16.png"/></span>
            <span>
                <a href="javascript:send('${currentNode.path}','suspended')"><fmt:message key="jnt_task.suspended"/></a>&nbsp;
                <a href="javascript:send('${currentNode.path}','cancelled')"><fmt:message key="jnt_task.cancel"/></a>&nbsp;
                <a href="javascript:send('${currentNode.path}','finished')"><fmt:message key="jnt_task.complete"/></a>
            </span>
                    </c:when>
                    <c:when test="${currentNode.properties.state.string == 'finished'}">
                        <img alt="" src="${url.currentModule}/images/tick_16.png"/>
                    </c:when>
                    <c:when test="${currentNode.properties.state.string == 'suspended'}">
                        <span><img alt="" src="${url.currentModule}/images/bubble_16.png"/></span>
            <span>
                <a href="javascript:send('${currentNode.path}','cancelled')"><fmt:message key="jnt_task.cancel"/></a>&nbsp;
                <a href="javascript:send('${currentNode.path}','active')"><fmt:message key="jnt_task.continue"/></a>
            </span>
                    </c:when>
                    <c:when test="${currentNode.properties.state.string == 'canceled'}">
                        <img alt="" src="${url.currentModule}/images/warning_16.png"/>
                    </c:when>
                </c:choose>
            </p>

        </fieldset>

    </form>
</div>