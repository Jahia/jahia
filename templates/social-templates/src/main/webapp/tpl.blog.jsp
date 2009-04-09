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
<%@ include file="common/declarations.jspf" %>
<template:template gwtForGuest="true">
    <template:templateHead>
        <template:include page="common/header.jsp"/>
        <link rel="alternate" type="application/rss+xml" title="web templates : news"
              href="${currentPage.url}/template/blog.rss?definitionName=social_templates_blog_blogEntries"/>
    </template:templateHead>
    <template:templateBody gwtScript="">
        <div id="bodywrapper"><!--start bodywrapper-->

            <div id="container"><!--start container-->
                <div id="page"><!--start page-->
                    <template:include page="common/top.jsp"/>
                    <div id="containerdata" class="containerDataContent2"><!--start containerdata-->
                        <div id="wrapper"><!--start wrapper-->

                            <div id="content2">
                                <!--start content  #content2=areaB/content #content3=content/InsetA   content4=alone #content5=50%areaB/50%content-->
                                <div class="spacer"><!--start spacer -->
                                    <!--stop post-->
                                    <template:containerList name="blogPrefs"  displayActionMenu="false">
                                        <template:container id="preference" cache="off" displayActionMenu="false">
                                            <template:field name="maxEntries" var="blogMaxEntries" defaultValue="10" display="false"/>
                                            <c:set var="blogMaxEntries" value="${blogMaxEntries}" scope="page"/>
                                        </template:container>
                                    </template:containerList>
                                    <c:if test="${!empty param.article}">
                                        <template:include page="modules/blog/blogComment.jsp">
                                            <template:param name="article" value="${param.article}"/>
                                        </template:include>
                                    </c:if>
                                    <c:if test="${!empty param.addArticle}">
                                        <template:include page="modules/blog/blogAddEntry.jsp">
                                            <template:param name="article" value="${param.article}"/>
                                        </template:include>
                                    </c:if>
                                    <c:if test="${empty param.article && empty param.addArticle}">
                                        <template:include page="modules/blog/blog.jsp">
                                            <template:param name="numBlogEntries" value="${blogMaxEntries}"/>
                                        </template:include>
                                    </c:if>
                                    <!--stop post-->
                                </div>
                                <!--stop space content-->


                            </div>
                            <!--stopContent-->
                        </div>
                        <!--stop wrapper-->
                        <div id="areaB"><!--start areaB-->

                            <div class="spacer"><!--start spacer areaB -->
                                <c:if test="${requestScope.currentRequest.editMode}">
                                    <template:include page="modules/preferences.jsp"/>
                                </c:if>
                                <template:include page="modules/nav/addEntry.jsp"/>
                                <template:include page="modules/filtersDisplay.jsp"/>
                                <template:include page="modules/searchForm.jsp"/>
                                <template:include page="modules/aboutMe.jsp"/>
                                <template:include page="modules/nav/navDate.jsp"/>
                                <template:include page="modules/nav/tagCloud.jsp"/>
                                <template:include page="modules/bookmark.jsp"/>
                            </div>
                            <!--stop space areaB-->
                        </div>
                        <!--stop areaB-->
                        <div class="clear"></div>
                    </div>
                    <!--stop containerdata-->
                    <template:include page="common/footer.jsp"/>
                    <div class="clear"></div>
                </div>
                <!--stop page-->
                <div class="clear"></div>
            </div>
            <!--stop container-->
            <div class="clear"></div>
        </div>
        <!--stop bodywrapper-->
    </template:templateBody>
</template:template>