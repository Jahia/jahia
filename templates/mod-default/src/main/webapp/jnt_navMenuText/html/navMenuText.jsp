<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<a href="#" onclick="return false;" style="cursor:default;">${fn:escapeXml(currentNode.properties.text.string)}</a>