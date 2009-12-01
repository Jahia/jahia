<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<utility:useConstants var="jcr" className="org.jahia.api.Constants" scope="application"/>
<form action="<%= request.getContextPath() %>/render/default/en/sites/testSite/content/news/*" method="post">
    <input type="hidden" name="nodeType" value="jnt:newsContainer"/>
    <input type="text" name="newsTitle" value=""/>
    <textarea rows="10" cols="80" name="newsDesc">News content</textarea>
    <input type="submit"/>
</form>