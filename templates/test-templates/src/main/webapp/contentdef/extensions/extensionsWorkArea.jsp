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
<%@ include file="../../common/declarations.jspf"%>

<h3>Subscribable content list</h3>
<p>Using default extension templates and logic to subscribe to notifications for the list</p>
<template:containerList name="subscribableContent" id="subscribableContentList">
  <jsp:include page="../../common/displayContainerWithSub.jsp"/>
</template:containerList>

<h3>Technology Watch</h3>
<p>Subscribe to notifications for the whole list</p>
<template:containerList name="subscribableTechWatch" id="subscribableTechWatchList">
  <jsp:include page="../../common/displayContainerWithSub.jsp"/>
</template:containerList>

<h3>Articles</h3>
<p>Showing the possibility of subscribing to notifications for each article</p>
<template:containerList name="subscribableArticle" id="subscribableArticleList">
  <jsp:include page="../../common/displayContainerWithSub.jsp"/>
</template:containerList>