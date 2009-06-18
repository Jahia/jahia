<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>

<h3>View page through renderer</h3>
<a href="<%= request.getContextPath() %>/render/default${currentPage.JCRPath}.html">${currentPage.JCRPath}.html</a>

<h3>View container list as RSS</h3>
<a href="<%= request.getContextPath() %>/render/default${currentPage.JCRPath}/maincontent2.rss">maincontentList2.rss</a>


<h3>Default template</h3>


<template:containerList name="maincontent2" id="maincontentList2" actionMenuNamePostFix="mainContents" >
    <template:container id="mainContent2Container"  cacheKey="mainContent2Container">
        <template:module contentBean="mainContent2Container" />
    </template:container>
</template:containerList>

<h3>Small template / title only</h3>

<template:containerList name="maincontent2" id="maincontentList2" actionMenuNamePostFix="mainContents"
                           displayActionMenu="false">
    <template:container id="mainContent2Container" displayActionMenu="false" cacheKey="module2">
        <template:module template="small" contentBean="mainContent2Container" />
    </template:container>
</template:containerList>

<h3>JCR Template example</h3>

<template:containerList name="maincontent2" id="maincontentList2" actionMenuNamePostFix="mainContents"
                           displayActionMenu="false">
    <template:container id="mainContent2Container" displayActionMenu="false"  cacheKey="module3">
        <template:module template="jcr" contentBean="mainContent2Container" />
    </template:container>
</template:containerList>

<h3>File listing example</h3>
<a href="<%= request.getContextPath() %>/render/default/content/shared/files.rss">RSS example</a> <br>
<a href="<%= request.getContextPath() %>/render/default/content/shared.html">Full page recursive view</a><br>
<jcr:node path="/content/shared" var="sharedFolder"/>
<template:module node="sharedFolder" />
