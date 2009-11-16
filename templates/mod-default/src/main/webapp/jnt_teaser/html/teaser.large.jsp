<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<div class="spacer">
    <!--start box -->
    <div class="box2 "><!--start box 2 default-->
        <div class="box2-topright"></div><div class="box2-topleft"></div>
        <h3 class="box2-header"><span><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></span></h3>
        <div class="box2-illustration" style="background-image:url(${image.node.url})"></div>

        <div class="box2-text">${currentNode.properties.abstract.string}</div>
        <div class="box2-more"><template:module template="link" path="link"/></div>
        <div class="box2-bottomright"></div>
        <div class="box2-bottomleft"></div>
        <div class="clear"> </div>
    </div>
    <!--stop box -->
</div>
<div class="clear"> </div>
