<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>


 <jcr:nodeProperty node="${currentNode}" name="title" var="title"/>
 <jcr:nodeProperty node="${currentNode}" name="positionTop" var="positionTop"/>
 <jcr:nodeProperty node="${currentNode}" name="positionLeft" var="positionLeft"/>
 <%--
 This is a file. it will be done later
  <jcr:nodeProperty node="${currentNode}" name="background" var="background"/>
 --%>
 <jcr:nodeProperty node="${currentNode}" name="cast" var="cast"/>

    <div id="illustration2" style="background:transparent url(<%--${background.url}--%>) no-repeat top left;">
        <div class="illustration2-text" style='margin-top:${positionTop.string}px; margin-left:${positionLeft.string}px'>
            <h2>${title.string}</h2>
            <p>${cast.string}</p>
        <div class="clear"> </div></div>
    </div>
