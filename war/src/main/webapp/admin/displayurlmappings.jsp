<%@ page import="org.jahia.bin.JahiaAdministration" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib uri="http://jsptags.com/tags/navigation/pager" prefix="pg" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ include file="/admin/include/header.inc" %>

<%
    String url = request.getRequestURI();
    if(url.charAt(url.length()-1) == '/')
        url = url.substring(0, url.length() - 1);

%>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="label.urlmapping.display"/></h2>
</div>
<div id="main">
    <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
        <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/admin/include/tab_menu.inc" %>
            </td>
        </tr>
        <tr>
            <td>
                <jsp:include page="/admin/include/left_menu.jsp">
                    <jsp:param name="mode" value="site"/>
                </jsp:include>
                <br/><br/>
                <div id="content" class="fit">
                    <div class="head">
                        <h2 class="page"><fmt:message key='label.urlmapping.all'/></h2>
                    </div>
                    <div class="content-body">
                        <jcr:node var="siteNode" uuid="${theSite.uuid}"/>
                        <jcr:sql var="vanities" sql="select * from [jnt:vanityUrl] as vanity where ISDESCENDANTNODE(vanity,'${siteNode.path}')"/>
                        <pg:pager
                                url="<%=url%>"
                                items="${functions:length(vanities.nodes)}"
                                maxPageItems="10"
                                maxIndexPages="5"
                                export="currentPageNumber=pageNumber"
                                >
                        <table class="evenOddTable" style="width: 100%;">
						<tr><td colspan=5>
                        <pg:index export="itemCount">
                            <div class="pagination"><!--start pagination-->
                                <div class="paginationNavigation">
                                    <pg:prev ifnull="true">
                                        <c:if test="${not empty pageUrl}"><a
                                                href="${pageUrl}&do=urlmapping"><strong><fmt:message
                                                key="label.previous"/>&nbsp;</strong></a></c:if>
                                        <c:if test="${empty pageUrl}"><span><strong><fmt:message
                                                key="label.previous"/>&nbsp;</strong></span></c:if>
                                    </pg:prev>
                                    <pg:pages>
                                        <c:if test="${pageNumber != currentPageNumber}"><a
                                                href="${pageUrl}&do=urlmapping">${pageNumber}&nbsp;</a></c:if>
                                        <c:if test="${pageNumber == currentPageNumber}"><span>${pageNumber}&nbsp;</span></c:if>
                                    </pg:pages>
                                    <pg:next ifnull="true">
                                        <c:if test="${not empty pageUrl}"><a
                                                href="${pageUrl}&do=urlmapping"><strong>&nbsp;<fmt:message
                                                key="label.next"/></strong></a></c:if>
                                        <c:if test="${empty pageUrl}"><span><strong>&nbsp;<fmt:message
                                                key="label.next"/></strong></span></c:if>
                                    </pg:next>
                                </div>
                            </div>
                            <!--stop pagination-->
                        </pg:index>
</td></tr>
                            <tr><th><b><fmt:message key='label.urlmapping.mapping'/></b></th><th><b><fmt:message key='label.page'/></b></th>
                                <th><b><fmt:message key='label.urlmapping.lang'/></b></th><%/*th><b><fmt:message key='org.jahia.admin.urlmapping.site'/></b></th*/%>
                                <th><b><fmt:message key='label.urlmapping.active'/></b></th><th><b><fmt:message key='label.urlmapping.default'/></b></th></tr>
                            <%
                                int counter = 0;
                            %>
                            <c:forEach items="${vanities.nodes}" var="vanity">
                                <pg:item>
                                    <%
                                        if(counter % 2 == 0) {
                                    %>

                                    <tr class="evenLine">
                                                <%}else{%>
                                    <tr class="oddLine">
                                        <%}

                                            counter ++;%>


                                        <td><a href="" target="_blank">${vanity.properties["j:url"].string}</a></td>
                                        <td><a href="" target="_blank">${vanity.parent.parent.path}</a></td>
                                        <td>${vanity.properties["jcr:language"].string}</td>
                                        <%/*td><=mapping.getSiteID()></td*/%>
                                        <td>${vanity.properties["j:active"].string}</td>
                                        <td>${vanity.properties["j:default"].string}</td>


                                    </tr>
                                </pg:item>
                            </c:forEach>
                    </div>
                </div>
            </td>
        </tr>
        </tbody>
	<tr><td colspan=5>
    <pg:index export="itemCount">
        <div class="pagination"><!--start pagination-->
            <div class="paginationNavigation">
                <pg:prev ifnull="true">
                    <c:if test="${not empty pageUrl}"><a
                            href="${pageUrl}&do=urlmapping"><strong><fmt:message
                            key="label.previous"/>&nbsp;</strong></a></c:if>
                    <c:if test="${empty pageUrl}"><span><strong><fmt:message
                            key="label.previous"/>&nbsp;</strong></span></c:if>
                </pg:prev>
                <pg:pages>
                    <c:if test="${pageNumber != currentPageNumber}"><a
                            href="${pageUrl}&do=urlmapping">${pageNumber}&nbsp;</a></c:if>
                    <c:if test="${pageNumber == currentPageNumber}"><span>${pageNumber}&nbsp;</span></c:if>
                </pg:pages>
                <pg:next ifnull="true">
                    <c:if test="${not empty pageUrl}"><a
                            href="${pageUrl}&do=urlmapping"><strong>&nbsp;<fmt:message
                            key="label.next"/></strong></a></c:if>
                    <c:if test="${empty pageUrl}"><span><strong>&nbsp;<fmt:message
                            key="search.results.pagination.next"/></strong></span></c:if>
                </pg:next>
            </div>
        </div>
        <!--stop pagination-->
    </pg:index>
	</td></tr>
	</table>
    </pg:pager>
</div>
<div id="actionBar">
    <span class="dex-PushButton">
    <span class="first-child">
    <a class="ico-back"
       href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
    </span>
    </span>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>