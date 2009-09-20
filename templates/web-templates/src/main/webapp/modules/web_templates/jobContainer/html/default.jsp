<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>


<jcr:nodeProperty node="${currentNode}" name="reference" var="reference"/>
<jcr:nodeProperty node="${currentNode}" name="businessUnit" var="businessUnit"/>
<jcr:nodeProperty node="${currentNode}" name="contract" var="contract"/>
<jcr:nodeProperty node="${currentNode}" name="town" var="town"/>
<jcr:nodeProperty node="${currentNode}" name="country" var="country"/>
<jcr:nodeProperty node="${currentNode}" name="educationLevel" var="educationLevel"/>
<jcr:nodeProperty node="${currentNode}" name="description" var="description"/>
<jcr:nodeProperty node="${currentNode}" name="skills" var="skills"/>


