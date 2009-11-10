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
            <img src="${picture.node.thumbnailUrls['thumbnail']}" alt="${lastname} picture" align="left" width="58" height="58" style="border:1px solid #CCCCCC; margin-right:7px; ">
        </div>
    </c:if>

            <h5><jcr:nodeProperty node="${currentNode}" name="peopleFirstname"/> <jcr:nodeProperty
                    node="${currentNode}" name="lastname"/></h5>

            <p><jcr:nodeProperty node="${currentNode}" name="function"/></p>
             <p><jcr:nodeProperty node="${currentNode}" name="businessUnit"/></p>
            <p>M.:<jcr:nodeProperty node="${currentNode}" name="cellular"/></p>
            <p>T.:<jcr:nodeProperty node="${currentNode}" name="telephone"/></p>
            <p>F.:<jcr:nodeProperty node="${currentNode}" name="fax"/></p>
            <p><jcr:nodeProperty node="${currentNode}" name="email" var="email"/>
            <a href='mailto:${email.string}'>${email.string}</a></p>
            <p><jcr:nodeProperty node="${currentNode}" name="biography"/>      </p>
        
        <!--stop collapsible -->
        
</div>
    <!--stop peopleBody -->
<div class="clear"></div>

