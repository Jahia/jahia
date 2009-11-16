<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<script type="text/javascript">
    function ShowHideLayer(boxID) {
	/* Obtain reference for the selected boxID layer and its button */
	var box = document.getElementById("collapseBox"+boxID);

	/* If the selected box is currently invisible, show it */
	if(box.style.display == "none" || box.style.display=="") {
		box.style.display = "block";
	}
	/* otherwise hide it */
	else {
		box.style.display = "none";
	}
}
</script>

<div style="border:1px solid #CCCCCC; padding:5px">
    <jcr:nodeProperty var="picture" node="${currentNode}" name="picture"/>
    <c:if test="${not empty picture}">
        <div>
            <img src="${picture.node.thumbnailUrls['thumbnail']}" alt="${currentNode.properties.lastname.string} picture" align="left" width="58" height="58" style="border:1px solid #CCCCCC; margin-right:7px; ">
        </div>
    </c:if>

            <h4>${currentNode.properties.firstname.string} ${currentNode.properties.lastname.string}</h4>
            <p>${currentNode.properties.function.string}</p>
            <p>${currentNode.properties.businessUnit.string}</p>
            <p>M.: ${currentNode.properties.cellular.string}</p>
            <p>T.: ${currentNode.properties.telephone.string}</p>
            <p>F.: ${currentNode.properties.fax.string}</p>
            <p><a href='mailto:${currentNode.properties.email.string}'>${currentNode.properties.email.string}</a></p>
            <p>${currentNode.properties.biography.string}</p>
</div>

<div class="clear"></div>