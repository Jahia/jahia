<%@ page import="org.jahia.utils.FileUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:module node="${currentNode}" forcedTemplate="hidden.header"  editable="false"/>

<table width="100%" class="table tableTasks " summary="Edition Mes taches en cours (table)">
<caption class=" hidden">
Edition Mes taches en cours (table)
</caption>
<colgroup>
    <col span="1" width="5%" class="col1"/>
    <col span="1" width="40%" class="col2"/>
    <col span="1" width="15%" class="col3"/>
    <col span="1" width="15%" class="col4"/>
</colgroup>
<thead>
    <tr>
        <th class="center" id="Type" scope="col">Type <a title="sort down" href="#"> <img src="${url.currentModule}/css/img/sort-arrow-down.png" alt="down"/></a></th>
        <th id="Title" scope="col"> Titre  <a title="sort down" href="#"> <img src="${url.currentModule}/css/img/sort-arrow-down.png" alt="down"/></a></th>
        <th class="center" id="Creation" scope="col">Creation<a title="sort down" href="#"> <img src="${url.currentModule}/css/img/sort-arrow-down.png" alt="down"/></a></th>
        <th id="Author" scope="col">Autheur <a title="sort down" href="#"> <img src="${url.currentModule}/css/img/sort-arrow-down.png" alt="down"/></a></th>
    </tr>
</thead>

<tbody>
<c:forEach items="${currentList}" var="subchild" begin="${begin}" end="${end}">

    <tr class="odd">
        <td class="center" headers="Type"><a style="display:block;width:16px;height:16px" class="<%=FileUtils.getFileIcon( ((JCRNodeWrapper)pageContext.findAttribute("subchild")).getName()) %>"></a> </td>
        <td headers="Title"><a href="${url.base}${subchild.path}.details.html">${subchild.name}</a>


<a class="BtMore rightside" href="#"></a>
        </td>

<jcr:nodeProperty node="${subchild}" name="jcr:created" var="created"/>
<fmt:formatDate value="${created.time}" dateStyle="full" var="displayDate"/>
        <td class="center" headers="Creation">${displayDate}</td>
        <td headers="Author">${subchild.propertiesAsString['jcr:createdBy']}</td>
    </tr>

</c:forEach>
<div class="clear"></div>
<c:if test="${editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>


</tbody>
</table>
<template:module node="${currentNode}" forcedTemplate="hidden.footer"  editable="false"/>
