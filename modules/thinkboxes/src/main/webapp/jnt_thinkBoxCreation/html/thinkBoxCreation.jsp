<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:if test="${pageContext.request.serverPort != 80}">
    <c:set var="serverUrl" value="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}"/>
</c:if>
<c:if test="${pageContext.request.serverPort == 80}">
    <c:set var="serverUrl" value="${pageContext.request.scheme}://${pageContext.request.serverName}"/>
</c:if>
<a href="javascript:(function(){CJN_SERVER='${serverUrl}';CJN_NOTE_FORM='${url.base}${currentNode.path}.js.html.ajax';try{var%20x=document.createElement('SCRIPT');x.type='text/javascript';x.src=CJN_SERVER+'/javascript/jahiaclip.js?'+(new%20Date().getTime()/100000);document.getElementsByTagName('head')[0].appendChild(x);}catch(e){alert(e);}})();"><img alt="Jahia Think Box" src="${url.currentModule}/img/logo_jahia_community.png" border="0" /></a>




