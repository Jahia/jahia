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

<%@ include file="../../declarations.jspf" %>
<template:containerList name="flashContainer" id="flash"
                       actionMenuNamePostFix="flash" actionMenuNameLabelKey="flash.add">
        <template:container id="flashContainer" displayActionMenu="false">
            <ui:actionMenu contentObjectName="flashContainer" namePostFix="flash" labelKey="flash.update">
            <template:field name="flashSourceFlashContainer" display="false" valueBeanID="flashSource"/>
            <template:field name="widthFlashContainer" display="false" valueBeanID="widthFlash"/>
            <template:field name="heightFlashContainer" display="false" valueBeanID="heightFlash"/>
            <template:field name="flashPlayerFlashContainer" display="false" valueBeanID="flashPlayer"/>
            <template:field name="idFlashContainer" display="false" valueBeanID="idFlash"/>
            <template:field name="nameFlashContainer" display="false" valueBeanID="nameFlash"/>
            <template:field name="swliveconnectFlashContainer" display="false" valueBeanID="swliveconnectFlash"/>
            <template:field name="playFlashContainer" display="false" valueBeanID="playFlash"/>
            <template:field name="loopFlashContainer" display="false" valueBeanID="loopFlash"/>
            <template:field name="menuFlashContainer" display="false" valueBeanID="menuFlash"/>
            <template:field name="qualityFlashContainer" display="false" valueBeanID="qualityFlash"/>
            <template:field name="scaleFlashContainer" display="false" valueBeanID="scaleFlash"/>
            <template:field name="alignFlashContainer" display="false" valueBeanID="alignFlash"/>
            <template:field name="salignFlashContainer" display="false" valueBeanID="salignFlash"/>
            <template:field name="wmodeFlashContainer" display="false" valueBeanID="wmodeFlash"/>
            <template:field name="bgcolorFlashContainer" display="false" valueBeanID="bgcolorFlash"/>
            <template:field name="baseFlashContainer" display="false" valueBeanID="baseFlash"/>
            <template:field name="flashvarsFlashContainer" display="false" valueBeanID="flashvarsFlash"/>
	    <div id="flashcontent${flashContainer.id}">
            <div id="flashcontent"><!--START FLASH -->
                <strong>You need to upgrade your Flash Player</strong><br />
                <br />
                <a href="www.adobe.com/go/getflashplayer"><img src="<utility:resolvePath value='theme/${requestScope.currentTheme}/img/160x41_Get_Flash_Player.jpg'/>" alt="get flash player" /></a>
                </div></div>
        <script type="text/javascript">
            var so = new SWFObject("${flashSource.downloadUrl}", "${nameFlash}", "${widthFlash}", "${heightFlash}", "${flashPlayer}", "${bgcolorFlash}");
                    <c:if test="${not empty wmodeFlash}">
                          so.addParam("wmode", "${wmodeFlash}");
                    </c:if>
                    <c:if test="${not empty idFlash}">
                          so.addParam("id", "${idFlash}");
                    </c:if>
                    <c:if test="${not empty swliveconnectFlash}">
                          so.addParam("swliveconnect", "${swliveconnectFlash}");
                    </c:if>
                    <c:if test="${not empty playFlash}">
                          so.addParam("wmode", "${playFlash}");
                    </c:if>
                    <c:if test="${not empty menuFlash}">
                          so.addParam("menu", "${menuFlash}");
                    </c:if>
                    <c:if test="${not empty scaleFlash}">
                          so.addParam("scale", "${scaleFlash}");
                    </c:if>
                    <c:if test="${not empty qualityFlash}">
                          so.addParam("quality", "${qualityFlash}");
                    </c:if>
                    <c:if test="${not empty alignFlash}">
                          so.addParam("align", "${alignFlash}");
                    </c:if>
                    <c:if test="${not empty salignFlash}">
                          so.addParam("salign", "${salignFlash}");
                    </c:if>
                    <c:if test="${not empty baseFlash}">
                          so.addParam("base", "${baseFlash}");
                    </c:if>
                    <c:if test="${not empty flashvarsFlash}">
                          so.addParam("flashvars", "${flashvarsFlash}");
                    </c:if>
                  so.write("flashcontent${flashContainer.id}");</script>
            </ui:actionMenu>
        </template:container>
</template:containerList>