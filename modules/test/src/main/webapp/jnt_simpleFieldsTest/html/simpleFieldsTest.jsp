<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>link
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>


string : ${currentNode.properties.string.string}<br/>

long: ${currentNode.properties.long.string}<br/>

double: ${currentNode.properties.double.string}<br/>

<c:choose>
    <c:when test="${currentNode.properties.boolean.boolean}">
        the propertye boolean return True<br/>
    </c:when>
    <c:when test="${!currentNode.properties.boolean.boolean}">
        the propertye boolean return False<br/>
    </c:when>
    <c:otherwise>
        the propertye boolean was not set<br/>
    </c:otherwise>
</c:choose>

string with default: ${currentNode.properties.stringWithDefault.string}<br/>

string with initializer is Text: ${currentNode.properties.stringText.string}<br/>

string with initializer is Text(multiline): ${currentNode.properties.stringTextMultiline.string}<br/>

string with initializer is Richtext: ${currentNode.properties.stringRichText.string}<br/>

mandatory string: ${currentNode.properties.mandatoryString.string}<br/>

nofulltext string: ${currentNode.properties.nofulltextString.string}<br/>

alphanumeric string ((string) < '[a-zA-Z1-9]*'): ${currentNode.properties.alphanumericString.string}<br/>

integer between 1 and 10 ((long) < '[1,10]'): ${currentNode.properties.longBetween1and10.string}<br/>


multiple strings:
<jcr:nodeProperty node="${currentNode}" name= "multipleString" var="multiStrings"/>
<c:forEach items="${multiStrings}" var="i"><br/>
	${i.string}<br/>
</c:forEach><br/>




