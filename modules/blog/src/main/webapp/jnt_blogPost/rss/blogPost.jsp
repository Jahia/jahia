<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="jcr:created" var="created"/>
<jcr:nodeProperty node="${currentNode}" name="text" var="text"/>
<item>
    <title>${title.string}</title>
    <link><c:url value='${url.base}${currentResource.node.path}.html'/> </link>
    <description>${text.string}</description>
    <pubDate>${created.time}</pubDate>
    <dc:date>${created.time}</dc:date>
</item>