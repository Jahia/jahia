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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="../../common/declarations.jspf" %>
<div id="formSearchTop"><!--start formSearchTop-->
   <s:form name="simpleSearchForm">
       <div id="formSearchTopA">
        <p>
            <label><fmt:message key='search'/>: </label>
            <s:term class="text" value="${searchLabel}"/>
            <input class="png gobutton" type="image" src="<utility:resolvePath value='theme/${requestScope.currentTheme}/img/search-button.png'/>" tabindex="5"/>
        </p>
        </div>
       <c:if test="${!currentPage.homePage}">
        <div id="formSearchTopB">
        <p class="loginFormTopSection">
            <input type="checkbox" name="inTheCurrentSection" class="loginFormTopSection" id="inTheCurrentSection" ${not empty param.inTheCurrentSection ? 'checked="checked"' : ''} onchange="document.simpleSearchForm['src_pagePath.value'].value = this.checked ? '${jahia.page.ID}' : ''"/>
            <s:pagePath value="" display="false"/>
            <label class="loginFormTopSection" for="inTheCurrentSection"><fmt:message key='lookcurrentsection'/></label>
        </p>
        </div>
       </c:if>
    </s:form>
</div>
<!--stop formSearchTop-->