<%@ tag body-content="empty" description="Displays a button to subscribe to a RSS feed" %>
<%@ attribute name="targetURL" required="true" rtexprvalue="true" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<a title='<fmt:message key="rssFeed"/>'  href="${targetURL}"> <img title='<fmt:message key="rssFeed"/>' src="${pageContext.request.contextPath}/css/images/icones/rss_small.gif" alt="RSS"/></a>

