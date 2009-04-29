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
<%--
Copyright 2002-2008 Jahia Ltd

Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
Version 1.0 (the "License"), or (at your option) any later version; you may
not use this file except in compliance with the License. You should have
received a copy of the License along with this program; if not, you may obtain
a copy of the License at

 http://www.jahia.org/license/

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ include file="../common/declarations.jspf" %>
<template:template>
    <template:templateHead>
        <template:meta name="keywords" metadata="keywords"/>
        <template:meta name="description" metadata="description"/>
        <!-- All headers and declarations global meta and css-->
        <%@ include file="../common/head.jspf" %>
        <utility:applicationResources/>
    </template:templateHead>
    <template:templateBody gwtScript="${param.gwtScript}">
        <div id="bodywrapper"><!--start bodywrapper-->
            <div id="container">
                <%@ include file="../common/top.jspf" %>
                <div id="page"><!--start page-->
                    <div id="headerPart2"><!--start headerPart2-->
                        <div id="logotop">
                            <template:include page="common/logo.jsp"/>
                        </div>
                        <h1 class="hide">Nom du site</h1>
                        <%@include file="../common/nav/nav.jspf" %>
                    </div>
                   <c:if test="${ !empty param.illustration }">
                    <div id="illustration"><!--start illustration-->
                        <img src="${pageContext.request.contextPath}/templates/web-templates/theme/${requestScope.currentTheme}/img/illustration.png"
                             alt="illustration"/>
                    </div>
                    </c:if>
                    <!--stop illustration-->
                    <div id="containerdata"><!--start containerdata-->
                        <div id="wrapper"><!--start wrapper-->
                            <c:if test="${ !empty param.spaceContent }">
                                <template:include page="${param.spaceContent}"/>
                            </c:if>
                        </div>
                        <!--stop wrapper-->
                        <c:if test="${ !empty param.insetA }">
                            <div id="InsetA"><!--start InsetB-->
                                <div class="space"><!--start space InsetB -->
                                    <template:include page="${param.insetA}"/>
                                </div>
                            </div>
                        </c:if>
                        <c:if test="${ !empty param.insetB }">
                            <div id="InsetB"><!--start InsetB-->
                                <div class="space"><!--start space InsetB -->
                                    <template:include page="${param.insetB}"/>
                                </div><!--stop space InsetB -->
                            </div><!-- stop InsetB-->
                        </c:if>
                        <c:if test="${ !empty param.insetC }">
                            <div id="InsetC"><!--start InsetC-->
                                <div class="space"><!--start space InsetC -->
                                    <template:include page="${param.insetC}"/>
                                </div><!--stop InsetC-->
                            </div><!--stop space InsetC -->
                        </c:if>
                        <div class="clear"></div>
                    <%@ include file="../common/footer.jspf" %>
                    <div class="clear"></div>
                </div>
                <div class="clear"></div>
            </div>
            <div class="clear"></div>
        </div>
    </template:templateBody>
</template:template>
