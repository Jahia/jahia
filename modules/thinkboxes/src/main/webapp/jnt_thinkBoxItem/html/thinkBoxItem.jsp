<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

Titre: ${currentNode.properties['jcr:title'].string}
<br /><br />
url: ${currentNode.properties['url'].string}
<br /><br />
description: 
<br/>${currentNode.properties['description'].string}
<br /><br />
note:
<br />
${currentNode.properties['note'].string}