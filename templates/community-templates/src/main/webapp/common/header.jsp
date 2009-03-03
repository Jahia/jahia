<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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