<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<p class="maincontent">
    <template:field name='mainContentTitle'/>
    <template:field name='mainContentAlign' display="false" var="mainContentAlign"/>
    <template:image file="mainContentImage" cssClassName="${mainContentAlign}"
                            align="${mainContentAlign}"/>
    <template:field name="mainContentBody"/>
</p>
URL: <a href="<%= request.getContextPath() %>/render/default${currentNode.path}.html"><%= request.getContextPath() %>/render/default/${currentNode.path}.html</a>
<br class="clear"/>
