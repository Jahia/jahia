<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
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
<template:addResources type="css" resources="960.css,01web.css,02mod.css,navigationN1-1.css,navigationN1-2.css,navigationN1-3.css,navigationN1-4.css,navigationN2-1.css,navigationN2-2.css"/>
<c:if test="${renderContext.editMode}">
    <template:addResources type="css" resources="edit.css"/>
</c:if>


<div id="bodywrapper"><!--start bodywrapper-->
    <div id="topheader"><!--start topheader-->
        <div class="container container_16">
            <div class="grid_16">
                <div id="headerPart1"><!--start headerPart1-->
                    <template:area path="header"/>
                    <div class="clear"></div>
                </div>
                <div class="clear"></div>
            </div>
        </div>
        <div class="clear"></div>
    </div>
    <!--stop topheader-->

    <div id="page"><!--start page-->
        <div id="bottomheader"><!--start bottomheader-->

            <div class="container container_16">
                <h1 class="hide">Nom du site</h1>

                <div class="logotop"><template:area path="logo"/></div>
            </div>
            <div class="container container_16">
                <template:area path="topMenu"/><!--Include MENU-->
            </div>
            <div class="clear"></div>
        </div>
        <!--stop bottomheader-->
        <div id="content"><!--start content-->
            <div class="container container_16">
                <div class="grid_16">
                    <template:area path="wrappercontent"/>
                </div>
            </div>
            <!--stop content-->
            <div class="clear"></div>
        </div>
        <div id="footer"><!--start footer-->
            <div id="footerPart3"><!--start footerPart3-->
                <div class="container container_16">
                    <div class='grid_2'><!--start grid_2-->
                        <template:area path="logoFooter"/>
                    </div>
                    <!--stop grid_2-->
                    <div class='grid_14'><!--start grid_14-->
                        <template:area path="footer"/>
                    </div>
                    <!--stop grid_12-->


                    <div class='clear'></div>
                </div>

                <div class="clear"></div>
            </div>
            <!--stop footerPart3-->
            <div class="clear"></div>
        </div>
        <!--stop footer-->

        <div class="clear"></div>
    </div>
    <!--stop page-->

    <div class="clear"></div>
</div>
<!--stop bodywrapper-->
