<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="values" value="${currentNode.propertiesAsString}"/>
<tr class="odd">
    <td headers="job"><a href="<c:url value='${url.base}${currentNode.path}.html'/>">${fn:escapeXml(values['jcr:title'])}</a>&nbsp;</td>
    <td headers="location">${fn:escapeXml(values.town)},<br/><jcr:nodePropertyRenderer node="${currentNode}" name="country" renderer="country"/>&nbsp;</td>
    <td headers="businessUnit">${fn:escapeXml(values.businessUnit)}&nbsp;</td>
</tr>
