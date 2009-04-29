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
<%@ include file="common/declarations.jspf" %>
<template:template gwtForGuest="true">
    <template:templateHead>
        <template:include page="common/header.jsp"/>
        <link rel="alternate" type="application/rss+xml" title="web templates : news"
              href="${currentPage.url}/template/blog.rss?definitionName=social_templates_blog_blogEntries"/>
        <template:include page="modules/blog/loadPreferences.jsp"/>
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
                                    <c:if test="${!empty param.editPreferences}">
                                        <template:include page="modules/blog/editPreferences.jsp">
                                            <template:param name="numBlogEntries" value="${blogMaxEntries}"/>
                                        </template:include>
                                    </c:if>
                                    <c:if test="${empty param.article && empty param.addArticle && empty param.editPreferences}">
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
                                <c:if test="${requestScope.currentRequest.hasWriteAccess}">
                                    <template:include page="modules/preferences.jsp"/>
                                    <template:include page="modules/nav/addEntry.jsp"/>
                                </c:if>
                                <template:include page="modules/filtersDisplay.jsp"/>
                                <template:include page="modules/nav/navCategories.jsp"/>
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