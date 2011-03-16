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
<%--@elvariable id="format" type="org.artofsolving.jodconverter.document.DocumentFormat"--%>
<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="css" resources="converter.css,files.css"/>
<c:if test="${renderContext.loggedIn}">
<jcr:sql var="result"
         sql="select * from [jmix:convertedFile] as conversion where isdescendantnode(conversion, ['/users/${renderContext.user.name}/']) order by conversion.[jcr:lastModified] desc"/>
<c:set var="currentList" value="${result.nodes}" scope="request"/>
<c:set var="listTotalSize" value="${functions:length(result.nodes)}" scope="request"/>
<c:choose>
    <c:when test="${empty param.pagesize}">
        <c:set var="pageSize" value="20"/>
    </c:when>
    <c:otherwise>
        <c:set var="pageSize" value="${param.pagesize}"/>
    </c:otherwise>
</c:choose>
<template:initPager totalSize="${moduleMap.listTotalSize}" pageSize="${pageSize}"
                    id="${currentNode.identifier}"/>
<h4 class="boxconverter-title2"><fmt:message key="label.upload.file.for.conversion"/></h4>

<div class="boxconverter"><!--start boxconverter -->
    <div class="boxconvertergrey boxconverterpadding10 boxconvertermarginbottom16">
        <div class="boxconverter-inner">
            <div class="boxconverter-inner-border">
                <div class="Form formconverterupload"><!--start formconverterupload-->
                    <form action="<c:url value='${url.base}${currentNode.path}.convert.do'/>" method="post"
                          enctype="multipart/form-data" id="conversionForm">
                        <input type="hidden" name="redirectTo"
                               value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>"/>

                        <p>
                            <label for="uploadfile" class="left"><fmt:message key="label.upload.a.document"/>:</label>
                            <input id="uploadfile" name="fileField" tabindex="1" type="file"/>
                        </p>

                        <p>
                            <label for="conversionType" class="left"><fmt:message key="label.transform.into"/>:</label>
                            <select name="mimeType" id="conversionType">
                                <c:forEach items="${functions:possibleFormats()}" var="format">
                                    <option value="${format.mediaType}">${format.extension}&nbsp;(${fn:escapeXml(format.name)})</option>
                                </c:forEach>
                            </select>
                            <input type="submit" id="submit" class="button" value="<fmt:message key='label.convert'/>" tabindex="4"/>
                        </p>
                    </form>

                </div>
            </div>
        </div>
    </div>
</div>
    <script>
        $(document).ready(new function(){
            $("#conversionType")
        });
    </script>
<!--stop boxconverter -->
<c:if test="${listTotalSize > 0}">
    <c:forEach items="${result.nodes}" var="lastNode" begin="0" end="0">
        <div class="boxconverter "><!--start boxconverter -->
            <div class="boxconverterpadding16 boxconvertermarginbottom16">
                <div class="boxconverter-inner">
                    <div class="boxconverter-inner-border">
                        <div class="floatright">

                        </div>
                        <div class="imagefloatleft">
                            <div class="itemImage itemImageLeft"><span class="icon_large ${functions:fileExtension(lastNode.properties.originDocName.string)}_large"></span></div>
                            <div class="itemImageConverterArrow itemImageLeft">
                            	<img alt="" src="<c:url value='${url.currentModule}/img/convert.png'/>"/>
                            </div>
                            <div class="itemImage itemImageLeft"><span class="icon_large ${functions:fileExtension(lastNode.name)}_large"></span></div>
                        </div>
                        <h3><fmt:message
                                key="label.from"/>:&nbsp;${fn:escapeXml(functions:abbreviate(lastNode.properties.originDocName.string,20,30,'...'))}</h3>

                        <h3><fmt:message key="label.to"/>:&nbsp;<a href="${lastNode.url}"><img alt=""
                                                                                 src="<c:url value='${url.currentModule}/img/download.png'/>"/>&nbsp;${fn:escapeXml(functions:abbreviate(lastNode.name,20,30,'...'))}
                        </a></h3>
                   <span class="clearMaringPadding converterdate"><fmt:message
                           key="label.conversion.date"/>:&nbsp;<fmt:formatDate
                           value="${lastNode.properties['jcr:lastModified'].date.time}" dateStyle="short"
                           type="both"/></span>
                        <!--stop boxconverter -->
                        <div class="clear"></div>
                    </div>
                </div>
            </div>
        </div>
        <!--stop boxconverter -->
        <c:if test="${lastNode.properties.conversionSucceeded.boolean eq false}">
            <div class="boxconverter">
                <div class=" boxconverterred boxconverterpadding16 boxconvertermarginbottom16">
                    <div class="boxconverter-inner">
                        <div class="boxconverter-inner-border">
                            <h3 class="boxconvertertitleh3 clearMaringPadding"><fmt:message key="label.error"/>:</h3>

                            <p class="clearMaringPadding">${fn:escapeXml(lastNode.properties.conversionFailedMessage.string)}</p>

                            <div class="clear"></div>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>
    </c:forEach>
</c:if>

<h4 class="boxconverter-title2"><fmt:message key="label.conversion.report"/></h4>

<div class="boxconverter" id="${currentNode.identifier}">
<div class="boxconvertergrey boxconverterpadding16 boxconvertermarginbottom16">
<div class="boxconverter-inner">
<div class="boxconverter-inner-border"><!--start boxconverter -->

<table width="100%" class="table tableConverterRapport "
       summary="<fmt:message key="label.conversion.report"/>">
    <caption class="hidden">
        <fmt:message key="label.conversion.report"/>
    </caption>
    <colgroup>
        <col span="1" width="10%" class="col1"/>
        <col span="1" width="30%" class="col2"/>
        <col span="1" width="10" class="col3"/>
        <col span="1" width="10%" class="col4"/>
        <col span="1" width="15%" class="col5"/>
        <col span="1" width="15%" class="col5"/>
        <col span="1" width="10%" class="col5"/>
    </colgroup>
    <thead>
    <tr>
        <th class="center" id="Statut" scope="col"><fmt:message key="label.status"/></th>
        <th id="TitleOriginal" scope="col"><fmt:message key="label.original.name"/></th>
        <th class="center" id="OriginalDoc" scope="col"><fmt:message key="label.original.type"/></th>
        <th class="center" id="TranformDoc" scope="col"><fmt:message key="label.converted.type"/></th>
        <th class="center" id="Date" scope="col"><fmt:message key="label.conversion.date"/></th>
        <th class="center" id="User" scope="col"><fmt:message key="label.user"/></th>
        <th class="center" id="Download" scope="col"><fmt:message key="label.download"/></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${moduleMap.currentList}" var="subchild" varStatus="status" begin="${moduleMap.begin}" end="${moduleMap.end}">
        <c:set var="conversionStatus">
            <c:choose>
                <c:when test="${subchild.properties.conversionSucceeded.boolean}">
                    <img alt="" src="<c:url value='${url.currentModule}/img/valide.png'/>"/>
                </c:when>
                <c:otherwise>
                    <img alt="" src="<c:url value='${url.currentModule}/img/error.png'/>"/>
                </c:otherwise>
            </c:choose>
        </c:set>
        <tr class="${status.count mod 2 eq 0 ? 'even' : 'odd'}">
            <td class="center" headers="Statut">${conversionStatus}</td>
            <td headers="TitleOriginal">${fn:escapeXml(functions:abbreviate(subchild.properties.originDocName.string,20,30,'...'))}</td>
            <td class="center"
                headers="OriginalDoc">${subchild.properties.originDocFormat.string}</td>
            <td class="center"
                headers="TranformDoc">${subchild.properties.convertedDocFormat.string}</td>
            <td class="center" headers="Date"><fmt:formatDate
                    value="${subchild.properties['jcr:lastModified'].date.time}" dateStyle="short"
                    type="both"/></td>
            <td class="center" headers="User">${subchild.properties['jcr:lastModifiedBy'].string}</td>
            <td class="center" headers="Download"><a
                    href="${subchild.url}"><img alt="" src="<c:url value='${url.currentModule}/img/download.png'/>"/></a>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<template:displayPagination nbItemsList="5,10,20,40,60,80,100,200"/>
<template:removePager id="${currentNode.identifier}"/>
<!--stop pagination-->
<div class="clear"></div>

</div>
</div>
</div>
</div>
<!--stop boxconverter -->
</c:if>
