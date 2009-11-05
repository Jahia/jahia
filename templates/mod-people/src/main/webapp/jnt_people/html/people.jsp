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
<div class="peopleListItem">
    <jcr:nodeProperty var="picture" node="${currentNode}" name="picture"/>
    <c:if test="${not empty picture}">
        <div class="peoplePhoto">
            <jcr:nodeProperty node="${currentNode}" name="lastname" var="lastname"/>
            <img src="${picture.node.thumbnailUrls['thumbnail']}" alt="${lastname} picture"></div>
    </c:if>
    <div class="peopleBody">
        <h5><jcr:nodeProperty node="${currentNode}" name="peopleFirstname"/>&nbsp;<jcr:nodeProperty
                node="${currentNode}" name="lastname"/></h5>

        <p class="peopleFonction"><jcr:nodeProperty node="${currentNode}" name="function"/></p>

        <p class="peopleBusinessUnit"><jcr:nodeProperty node="${currentNode}" name="businessUnit"/></p>

        <jcr:nodeProperty node="${currentNode}" name="email" var="email"/>
        <p class="peopleEmail"><a href='mailto:${email.string}'>${email.string}</a></p>

        <div class="peopleAction">
            <a class="peopleDownload" href="${picture.node.url}" rel="facebox"><fmt:message
                    key='web_templates_peopleContainer.peopleViewFullSize'/></a>
            <a class="peopleBiographiy" href="javascript:;" onclick="ShowHideLayer('${currentNode.identifier}');"><fmt:message
                    key='web_templates_peopleContainer.biography'/></a>
        </div>
        <div id="collapseBox${currentNode.identifier}" class="collapsible">
            <jcr:nodeProperty node="${currentNode}" name="biography"/>
        </div>
        <!--stop collapsible -->
        <div class="clear"></div>
    </div>
    <!--stop peopleBody -->
    <div class="clear"></div>
</div>
