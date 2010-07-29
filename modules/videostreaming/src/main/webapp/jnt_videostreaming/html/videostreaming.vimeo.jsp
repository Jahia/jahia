<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<object width="${currentNode.properties.width.long}" height="${currentNode.properties.height.long}">
	<param name="allowfullscreen" value="${currentNode.properties.allowfullscreen.boolean}" />
	<param name="allowscriptaccess" value="always" />
	<param name="movie" value="http://vimeo.com/moogaloop.swf?clip_id=${currentNode.properties.identifier.string}&amp;server=vimeo.com&amp;show_title=1&amp;show_byline=1&amp;show_portrait=0&amp;color=&amp;fullscreen=1" />
	<embed src="http://vimeo.com/moogaloop.swf?clip_id=${currentNode.properties.identifier.string}&amp;server=vimeo.com&amp;show_title=1&amp;show_byline=1&amp;show_portrait=0&amp;color=&amp;fullscreen=1" type="application/x-shockwave-flash" allowfullscreen="${currentNode.properties.allowfullscreen.boolean}" allowscriptaccess="always" width="${currentNode.properties.width.long}" height="${currentNode.properties.height.long}"></embed>
</object>