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
<%@ include file="../../common/declarations.jspf" %>
<div id="content"><!--start content-->
    <div class="spaceContent"><!--start spaceContent -->
        <template:include page="common/breadcrumb.jsp"/>
        <!--stop box -->
        <div class="box"><!--start box -->
            <h2><c:out value="${requestScope.currentPage.highLightDiffTitle}"/></h2>
            <template:include page="common/maincontent/maincontentDisplay.jsp">
                <template:param name="id" value="columnB"/>
            </template:include>
        </div>
        <template:include page="common/box/box.jsp">
            <template:param name="name" value="columnB_box"/>
        </template:include>
    </div>
    <!--stop columnspace -->
    <div class="clear"></div>
</div>
  