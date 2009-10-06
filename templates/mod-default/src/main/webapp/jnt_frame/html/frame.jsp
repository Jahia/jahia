<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<jcr:nodeProperty node="${currentNode}" name="source" var="source"/>
<jcr:nodeProperty node="${currentNode}" name="name" var="name"/>
<jcr:nodeProperty node="${currentNode}" name="width" var="width"/>
<jcr:nodeProperty node="${currentNode}" name="height" var="height"/>
<jcr:nodeProperty node="${currentNode}" name="frameborder" var="frameborder"/>
<jcr:nodeProperty node="${currentNode}" name="marginwidth" var="marginwidth"/>
<jcr:nodeProperty node="${currentNode}" name="marginheight" var="marginheight"/>
<jcr:nodeProperty node="${currentNode}" name="scrolling" var="scrolling"/>
<jcr:nodeProperty node="${currentNode}" name="alt" var="alt"/>

<%--As Iframe is deprecated in XHTML 1.0 we use object tag--%>

<object
        data="${source.string}"
        name="${name.string}"
        width="${width.long}"
        height="${height.long}"
        border="${frameborder.long}"
        type="text/html">
</object>