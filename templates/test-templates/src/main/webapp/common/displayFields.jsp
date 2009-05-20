<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ include file="declarations.jspf"%>
<c:set var="processedContainer" value='<%=pageContext.findAttribute(request.getParameter("containerId"))%>'/>
<c:forEach items="${processedContainer.fields}" var="processedEntry">
  <c:set var="processedField" value="${processedEntry.value}"/>
  <div class="fieldTitle">${processedField.title}:</div>
  <template:field name='${processedField.name}' containerName="processedContainer" var="fldValue" display="false"/>
  <div class="fieldValue">
    <c:choose>
      <c:when test="${processedField.fieldType == fieldTypeConstants.BIGTEXT}">
        <template:field name='${processedField.name}' containerName="processedContainer"/>
        <c:if test="${!empty showMaxCharBigText}">
        	<b/>MaxChar: 40, continueString = ...</b><br/>
        	<template:field name='${processedField.name}' containerName="processedContainer" maxChar="40" continueString="..."/><br/>
        	<b>MaxWord: 5, continueString = etc.</b> <br/>
        	<template:field name='${processedField.name}' containerName="processedContainer" maxWord="5" continueString=" etc."/><br/>
        </c:if>
      </c:when>
      <c:when test="${processedField.fieldType == fieldTypeConstants.DATE}">
        <template:field name='${processedField.name}' containerName="processedContainer"/>
        <template:field name='${processedField.name}'
        	             var="theDateBean" display="false" containerName="processedContainer"/>
        &nbsp;(<fmt:formatDate value="${theDateBean.date}" pattern="yyyy-MM-dd HH:mm"/>)
      </c:when>
      <c:when test="${processedField.fieldType == fieldTypeConstants.PAGE}">
        <template:field name='${processedField.name}' containerName="processedContainer" />
        <template:link page="fldValue" containerName="processedContainer"/>
      </c:when>
      <c:when test="${processedField.fieldType == fieldTypeConstants.FILE}">
        <template:field name='${processedField.name}' containerName="processedContainer"/>
        <template:field name='${processedField.name}'
        	             var="${processedField.name}value" display="false" containerName="processedContainer"/>

        <template:file file="${processedField.name}value" containerName="processedContainer"/>
      </c:when>
      <c:when test="${processedField.fieldType == fieldTypeConstants.CATEGORY}">
        <c:forEach items="${fldValue.category}" var="cat">
            ${cat.title},&nbsp;
        </c:forEach>
      </c:when>
      <c:otherwise>
        <template:field name='${processedField.name}' containerName="processedContainer"/>
      </c:otherwise>
    </c:choose>
   </div>
</c:forEach>


