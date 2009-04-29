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
<%@ include file="declarations.jspf" %>
<div id="header"><!--start header-->
    <div id="headerPart1"><!--start headerPart1-->
        <div id="loginFormTop"><!--start loginFormTop-->
            <template:include page="common/loginForm.jsp"/>
        </div>
        <!--stop loginFormTop-->
        <div class="topNavigation"><!--start topNavigation-->
            <template:include page="common/links/basicLinksDisplay.jsp">
                <template:param name="cssClassName" value=""/>
            </template:include>
        </div>
        <!--stop topNavigation-->
    </div>
    <!--stop headerPart1-->
    <div id="headerPart2"><!--start headerPart2-->
        <div id="navigationN1"><!--start navigationN1-->
            <ui:navigationMenu displayActionMenuBeforeLink="true" cssClassName="" kind="topTabs" labelKey="pages.add"
                               requiredTitle="true" usePageIdForCacheKey="true"/>
        </div>
        <!--stop navigationN1-->
        <div id="logotop">
            <template:include page="common/logo.jsp"/>
        </div>
        <%// todo : add Site Name %>
        <h1 class="hide">${currentSite.title}</h1>
    </div>
    <!--stop headerPart2-->
    <div id="headerPart3"><!--start headerPart3-->
        <div id="formSearchTop"><!--start formSearchTop-->
            <s:form>
                <p><label class="hide"><fmt:message key='search'/>: </label>
                    <s:term class="text" value="" tabindex="4"/>
                    <c:set var="myImagePath" value="theme/${requestScope.currentTheme}/img/go-button.png"/>
                    <input type="image" class="gobutton png"
                           src="<utility:resolvePath value='theme/${requestScope.currentTheme}/img/go-button.png'/>"
                           tabindex="5"/></p>
            </s:form>
        </div>
        <!--stop formSearchTop-->
        <div class="languages"><!--start languages-->
            <p><fmt:message key="languageSelector"/>:</p>
            <ui:languageSwitchingLinks display="horizontal" linkDisplay="doubleLetter"
                                       displayLanguageState="true"/>
        </div>
        <!--stop languages-->
    </div>
    <!--stop headerpart3-->
</div>
<!--stop header-->