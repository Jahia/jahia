<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

Contract: ${currentNode.properties.contract.string}<br/>

Contract 2:
<jcr:nodePropertyRenderer node="${currentNode}" name="contractI18n" renderer="resourceBundle"/><br/>

Country:
<jcr:nodePropertyRenderer node="${currentNode}" name="country" renderer="country"/><br/>
Contract:
<jcr:nodePropertyRenderer node="${currentNode}" name="countryWithFlag" renderer="flagcountry"/><br/>

<%-- 
Book: ${currentNode.properties.book.string}<br/>

Product: ${currentNode.properties.product.string}<br/>

Content: ${currentNode.properties.content.string}<br/>
--%>

Category: ${currentNode.properties.category.string}<br/>

