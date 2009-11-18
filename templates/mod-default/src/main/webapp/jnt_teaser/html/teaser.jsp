<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>


 <%--<jcr:nodeProperty node="${currentNode}" name="link" var="link"/>--%>

        <div class="spacer"><!--start spacer -->
            <div class="box box-fixed-height"><!--start box -->
                <img src="${image.node.url}" class="floatleft"/>
                <div class="box-content">
                    <h3><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h3>
                    <p> ${currentNode.properties.abstract.string}</p>
                </div>
                <div class="more">
                    <span><template:module template="link" path="link"/></span>
                 </div>
            </div><!--stop box -->
        <div class="clear"> </div>
        </div><!--stop spacer -->
