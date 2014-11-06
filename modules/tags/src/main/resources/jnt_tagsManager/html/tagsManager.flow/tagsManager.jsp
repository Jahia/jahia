<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>

<template:addResources type="css" resources="admin-bootstrap-v3.2.min.css"/>
<template:addResources type="css" resources="admin-font-awesome-v4.2.0.min.css"/>

<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="admin-bootstrap-v3.2.min.js"/>
<template:addResources type="javascript" resources="jquery-ui.min.js,jquery.blockUI.js,workInProgress.js"/>
<template:addResources type="javascript" resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js"/>
<template:addResources type="javascript" resources="tagsManager.js"/>

<template:addResources type="inlinejavascript">
    <script>
        $(document).ready(function () {
            $('#tableTagsList').dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6 text-right'f>r>t<'row-fluid'<'span6'i><'span6 text-right'>>",
                "bPaginate": false,
                "iDisplayLength":25,
                "aaSorting": [[0, 'desc']]
            });
        });
    </script>
</template:addResources>

<div class="container-fluid">
    <div class="row">
        <h1><fmt:message key="jnt_tagsManager"/></h1>
    </div>
    <div class="row well">
        <table cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered table-striped" id="tableTagsList">
            <thead>
                <tr>
                    <th>
                        <fmt:message key="jnt_tagsManager.label.tag"/>
                    </th>
                    <th>
                        <fmt:message key="jnt_tagsManager.label.occurrences"/>
                    </th>
                    <th>
                        <fmt:message key="label.actions"/>
                    </th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${tagsList}" var="tag">
                    <tr>
                        <td>
                            ${tag.key}
                        </td>
                        <td>
                            ${tag.value}
                        </td>
                        <td>
                            <div class="dropdown">
                                <button class="btn btn-primary dropdown-toggle" type="button" id="dropdownMenuActions" data-toggle="dropdown">
                                    <i class="fa fa-list-ul"></i>
                                    <fmt:message key="label.actions"/>
                                    <span class="caret"></span>
                                </button>
                                <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenuActions">
                                    <li role="presentation"><a role="menuitem" tabindex="-1" href="#"><i class="fa fa-pencil"></i><fmt:message key="label.rename"/></a></li>
                                    <li role="presentation"><a role="menuitem" tabindex="-1" href="#"><i class="fa fa-trash"></i><fmt:message key="label.delete"/></a></li>
                                    <li role="presentation"><a role="menuitem" tabindex="-1" href="#"><i class="fa fa-search"></i><fmt:message key="jnt_tagsManager.label.viewUsages"/></a></li>
                                </ul>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>