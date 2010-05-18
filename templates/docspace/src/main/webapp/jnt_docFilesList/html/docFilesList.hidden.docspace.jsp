<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery.dataTables.min.js"/>
<template:addResources type="css" resources="files.css,dataTables.css"/>
<template:include template="hidden.header"/>
<script type="text/javascript">
    $(document).ready(function() {
        var myTable = $('#fileListTable').dataTable({
            "bLengthChange": true,
            "bFilter": true,
            "bSort": true,
            "bInfo": false,
            "bAutoWidth": false,
            "bStateSave" : true,
            "aaSorting": [
                [4,'desc'],
                [1,'asc']
            ],
            "aoColumns": [
                null,
				null,
                null,
				null,
                null,
                null,
                {
                    "sType": "html"
                }
            ]
        });
    });
</script>
<table width="100%" class="table fileListTable" summary="file List Table" id="fileListTable">
    <caption class=" hidden">
        
    </caption>
    <colgroup>
        <col span="1" width="5%" class="col1"/>
        <col span="1" width="35%" class="col2"/>
        <col span="1" width="5%" class="col3"/>
        <col span="1" width="10%" class="col4"/>
        <col span="1" width="20%" class="col5"/>
        <col span="1" width="10%" class="col6"/>
        <col span="1" width="15%" class="col7"/>
    </colgroup>
    <thead>
    <tr>
        <th class="center" id="Type" scope="col"><fmt:message key="docspace.label.type"/>
            <img src="${url.currentModule}/css/img/sort-arrow-down.png" alt="down"/></th>       
        <th id="Title" scope="col"><fmt:message key="docspace.label.title"/> <img
                src="${url.currentModule}/css/img/sort-arrow-down.png" alt="down"/></th>
        <th class="center" id="Download" scope="col"><fmt:message key="docspace.label.download"/>
            </th>
        <th class="center" id="Creation" scope="col"><fmt:message key="docspace.label.creation"/><img
                src="${url.currentModule}/css/img/sort-arrow-down.png" alt="down"/></th>
        <th class="center" id="LastModif" scope="col"><fmt:message key="docspace.label.lastmodificationdate"/><img
                src="${url.currentModule}/css/img/sort-arrow-down.png" alt="down"/></th>
        <th id="Author" scope="col"><fmt:message key="docspace.label.author"/> <img
                src="${url.currentModule}/css/img/sort-arrow-down.png" alt="down"/></th>
        <th id="Rating" scope="col"><fmt:message key="docspace.label.rating"/> <img
                src="${url.currentModule}/css/img/sort-arrow-down.png" alt="down"/></th>
    </tr>
    </thead>

    <tbody>
    <c:forEach items="${currentList}" var="subchild" begin="${begin}" end="${end}">

        <tr class="odd">
            <td class="center" headers="Type">
                <c:choose>
                    <c:when test="${jcr:isNodeType(subchild, 'jnt:docspace')}"><a style="display:block;width:32px;height:32px"><img alt="docspace" src="${url.currentModule}/css/img/docspacemedium.png" height="32" width="32"/></a></c:when>
                    <c:otherwise>
                        <span class="icon ${functions:fileIcon(subchild.name)}"></span>                    </c:otherwise>
                </c:choose>            </td>
          <td headers="Title">
			<a href="${url.base}${subchild.path}<c:if test="${jcr:isNodeType(subchild, 'jnt:docspaceFile')}">.docspace</c:if>.html">${subchild.name}</a><br />
          </td>
           <td class="center" headers="Download">
            <c:if test="${not jcr:isNodeType(subchild, 'jnt:docspace')}"><a title="<fmt:message key="docspace.label.download"/>" href="${subchild.url}"><img src="${url.currentModule}/css/img/download.png"/></a></c:if>
          </td>

            <jcr:nodeProperty node="${subchild}" name="jcr:created" var="created"/>
            <jcr:nodeProperty node="${subchild}" name="jcr:lastModified" var="modified"/>
            <fmt:formatDate value="${created.time}" dateStyle="medium" type="date" var="displayDate"/>
            <td class="center" headers="Creation">${displayDate}</td>
            <td class="center" headers="LastModif"><fmt:formatDate value="${modified.time}" dateStyle="medium" type="both"/></td>
            <td headers="Author">${subchild.propertiesAsString['jcr:createdBy']}</td>
            <td class="center" headers="Rating"><template:option node="${subchild}" template="hidden.average.readonly"
                                                  nodetype="jmix:rating"/></td>
        </tr>
    </c:forEach>
    <div class="clear"></div>
    <c:if test="${editable and renderContext.editMode}">
        <template:module path="*"/>
    </c:if>


    </tbody>
</table>
<template:include template="hidden.footer">
    <template:param name="searchUrl" value="${url.current}"/>
</template:include>
