<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<object width="${currentNode.properties.width.long}" height="${currentNode.properties.height.long}">
	<param name="allowFullScreen" value="${currentNode.properties.allowfullscreen.boolean}"></param>
	<param name="allowscriptaccess" value="always"></param>
	<param name="movie" value="http://www.youtube.com/v/${currentNode.properties.identifier.string}&amp;hl=fr_FR&amp;fs=1"></param>
	<embed src="http://www.youtube.com/v/${currentNode.properties.identifier.string}&amp;hl=fr_FR&amp;fs=1" type="application/x-shockwave-flash" allowscriptaccess="always" allowfullscreen="${currentNode.properties.allowfullscreen.boolean}" width="${currentNode.properties.width.long}" height="${currentNode.properties.height.long}"></embed>
</object>