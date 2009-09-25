<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<p class="maincontent">
    <h4><template:field name='mainContentTitle'/></h4>
    <template:field name='mainContentAlign' display="false" var="mainContentAlign"/>
    <template:image file="mainContentImage" cssClassName="${mainContentAlign}"
                            align="${mainContentAlign}"/>
    <template:field name="mainContentBody"/>
</p>
URL: <a href="${pageContext.request.contextPath}/render/default${currentNode.path}.html">${currentNode.name}.html</a>
<br class="clear"/>
