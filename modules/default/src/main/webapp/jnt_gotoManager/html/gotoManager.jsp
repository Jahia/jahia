<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
 
<a href="${url.context}/engines/manager.jsp?conf=editorialcontentmanager&site=${renderContext.site.identifier}&selectedPaths=${currentNode.path}" target="_blank"><img src="${url.context}/icons/contentManager.png" width="16" height="16" alt=" " role="presentation" style="position:relative; top: 4px; margin-right:2px; "><fmt:message
            key="label.contentmanager"/></a>