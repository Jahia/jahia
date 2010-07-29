<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<object width="${currentNode.properties.width.long}" height="${currentNode.properties.height.long}">
		<param name="allowFullScreen" value="${currentNode.properties.allowfullscreen.boolean}"></param>
		<param name="allowScriptAccess" value="always"></param>
		<param name="movie" value="http://www.dailymotion.com/swf/video/${currentNode.properties.identifier.string}?additionalInfos=0"></param>
		<embed type="application/x-shockwave-flash" src="http://www.dailymotion.com/swf/video/${currentNode.properties.identifier.string}?additionalInfos=0" width="${currentNode.properties.width.long}" height="${currentNode.properties.height.long}" allowfullscreen="${currentNode.properties.allowfullscreen.boolean}" allowscriptaccess="always"></embed>
</object>
