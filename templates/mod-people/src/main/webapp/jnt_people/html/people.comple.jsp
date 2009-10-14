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
    <jcr:nodeProperty var="picture" node="${currentNode}" name="peoplePicture"/>
    <c:if test="${not empty picture}">
        <div style="border:1px solid #CCCCCC; margin-right:7px; margin-bottom:7px"">
            <img src="${picture.node.thumbnailUrls['thumbnail']}" alt="${peopleLastname} picture" align="left" width="60" height="60">
        </div>
    </c:if>

            <h5><jcr:nodeProperty node="${currentNode}" name="peopleFirstname"/> <jcr:nodeProperty
                    node="${currentNode}" name="peopleLastname"/></h5>

            <p><jcr:nodeProperty node="${currentNode}" name="peopleFunction"/></p>
             <p><jcr:nodeProperty node="${currentNode}" name="peopleBusinessUnit"/></p>
            <p><jcr:nodeProperty node="${currentNode}" name="peopleCellular"/></p>
            <p><jcr:nodeProperty node="${currentNode}" name="peopleTelephone"/></p>
            <p><jcr:nodeProperty node="${currentNode}" name="peopleFax"/></p>
            <p><jcr:nodeProperty node="${currentNode}" name="peopleEmail" var="email"/>
            <a href='mailto:${email.string}'>${email.string}</a></p>
            <p><jcr:nodeProperty node="${currentNode}" name="peopleBiography"/>      </p>
        
        <!--stop collapsible -->
        
</div>
    <!--stop peopleBody -->
<div class="clear"></div>

