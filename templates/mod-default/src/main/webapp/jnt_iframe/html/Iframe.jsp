<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<jcr:nodeProperty node="${currentNode}" name="IframeSource" var="IframeSource"/>
<jcr:nodeProperty node="${currentNode}" name="IframeName" var="IframeName"/>
<jcr:nodeProperty node="${currentNode}" name="IframeWidth" var="IframeWidth"/>
<jcr:nodeProperty node="${currentNode}" name="IframeHeight" var="IframeHeight"/>
<jcr:nodeProperty node="${currentNode}" name="IframeFrameborder" var="IframeFrameborder"/>
<jcr:nodeProperty node="${currentNode}" name="IframeMarginwidth" var="IframeMarginwidth"/>
<jcr:nodeProperty node="${currentNode}" name="IframeMarginheight" var="IframeMarginheight"/>
<jcr:nodeProperty node="${currentNode}" name="IframeScrolling" var="IframeScrolling"/>
<jcr:nodeProperty node="${currentNode}" name="IframeAlt" var="IframeAlt"/>

<%--As Iframe is deprecated in XHTML 1.0 we use object tag--%>

<object
        data="${IframeSource.string}"
        name="${IframeName.string}"
        width="${IframeWidth.long}"
        height="${IframeHeight.long}"
        border="${IframeFrameborder.long}"
        type="text/html">
</object>