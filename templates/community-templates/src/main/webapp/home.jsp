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

<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ include file="common/declarations.jspf" %>
<template:template>
    <template:templateHead>
        <!-- All headers and declarations global meta and css-->
        <%@ include file="common/head_externals.jspf" %>
        <utility:applicationResources/>
    </template:templateHead>
    <template:templateBody>
        <div id="bodywrapper">
            <div id="container"><!--start container-->
                <!-- Head page -->
                <template:include page="common/header.jsp"/>
            </div>
            <!--stop container-->
            <div id="container2"><!--start container2-->
                <div id="container3"><!--start container3-->
                    <div id="wrapper"><!--start wrapper-->
                        <div id="content"><!--start content-->
                            <div class="spaceContent"><!--start spaceContent -->
                                <!-- Theme selector-->
                                <template:include page="common/themesSelector.jsp"/>
                                <!-- Edito -->
                                <template:include page="common/edito.jsp"/>

                                <div class="box"><!--start box -->
                                    <!-- News list -->
                                    <template:include page="common/newslist.jsp"/>
                                </div><!--stop box -->
                                <!-- Last tools -->
                                <template:include page="common/lastTools.jsp"/>
                            </div><!--stop space content-->
                        </div><!--stopContent-->
                    </div><!--stop wrapper-->
                    <div id="rightInset"><!--start rightInset-->
                        <div class="space"><!--start space rightInset -->
                            <!-- right box -->
                            <template:include page="common/columnA.jsp"/>
                        </div><!--stop space rightInset-->
                    </div><!--stop rightInset-->
                    <div id="leftInset"><!--start leftInset-->
                        <div class="space"><!--start space leftInset -->
                            <!-- shortcuts -->
                            <template:include page="common/shortcuts.jsp"/>
                            <!-- Search area -->
                            <template:include page="common/searchArea.jsp"/>
                        </div><!--stop space leftInset-->
                    </div><!--stop leftInset-->
                    <div class="clear"></div>
                </div><!--stop container2-->
                <!-- footer -->
                <template:include page="common/footer.jsp"/>

                <div class="clear"></div>
            </div><!--stop container3-->
        </div>
    </template:templateBody>
</template:template>



