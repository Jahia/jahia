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
                    <div id="containerdata"><!--start containerdata-->
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
                                    <c:if test="${empty param.article}">
                                        <template:include page="modules/blog/blog.jsp"/>
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


                                <template:include page="modules/searchForm.jsp"/>

                                <template:include page="modules/aboutMe.jsp"/>

                                <template:include page="modules/nav/navDate.jsp"/>


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