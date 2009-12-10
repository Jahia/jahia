<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

 <jcr:nodeProperty node="${currentNode}" name="image" var="image"/>

<div class="teaser">
    <a href=""><img src="${image.node.url}" class="floatleft" alt="image"/></a>
</div>
<div class="clear"> </div>
