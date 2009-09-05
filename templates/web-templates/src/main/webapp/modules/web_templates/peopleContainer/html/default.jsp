<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>


<jcr:nodeProperty node="${currentNode}" name="peoplePicture" var="peoplePicture"/>
<jcr:nodeProperty node="${currentNode}" name="peopleLastname" var="peopleLastname"/>
<jcr:nodeProperty node="${currentNode}" name="peopleFirstname" var="peopleFirstname"/>
<jcr:nodeProperty node="${currentNode}" name="peopleFunction" var="peopleFunction"/>
<jcr:nodeProperty node="${currentNode}" name="peopleBusinessUnit" var="peopleBusinessUnit"/>
<jcr:nodeProperty node="${currentNode}" name="peopleEmail" var="peopleEmail"/>
<jcr:nodeProperty node="${currentNode}" name="peopleBiography" var="peopleBiography"/>
<jcr:nodeProperty node="${currentNode}" name="peopleTelephone" var="peopleTelephone"/>
<jcr:nodeProperty node="${currentNode}" name="peopleCellular" var="peopleCellular"/>
<jcr:nodeProperty node="${currentNode}" name="peopleFax" var="peopleFax"/>

    <div id="illustration2" style="background:transparent url(${background.node.url}) no-repeat top left;">
        <div class="illustration2-text" style='margin-top:${positionTop.string}px; margin-left:${positionLeft.string}px'>
            <h2>${title.string}</h2>
            <p>${cast.string}</p>
        <div class="clear"> </div></div>
    </div>

	<div class="peopleListItem">
		<div class="peoplePhoto">
        <img src="${peoplePicture.file.thumbnailUrl}" alt="${peopleLastname.string} picture"></div>
        <div class="peopleBody">
            <h5>${peopleFirstname.string} ${peopleLastname.string}</h5>
            <p class="peopleFonction">${peopleFunction.string}</p>
            <p class="peopleBusinessUnit">${peopleBusinessUnit.string}</p>

				<p class="peopleEmail"><a href='mailto:${peopleEmail.string}'>${peopleEmail.string}</a></p>

				<div class="peopleAction">
<a class="peopleDownload" href="${peoplePicture.file.downloadUrl}" rel="facebox" alt="${peopleLastname.string}"><fmt:message
                            key='web_templates_peopleContainer.peopleViewFullSize'/></a>
<a class="peopleBiographiy" href="javascript:;" onclick="ShowHideLayer(${peopleContainer.ID});"><fmt:message
                            key='web_templates_peopleContainer.peopleBiography'/></a>
				</div>
				<div id="collapseBox${peopleContainer.ID}" class="collapsible">
							 ${peopleBiography.string}
				</div><!--stop collapsible -->
					<div class="clear"> </div></div><!--stop peopleBody -->
				<div class="clear"> </div></div><!--stop peopleListItem -->
    </ui:actionMenu>



<!-- unused fields ${peopleTelephone} ${peopleCellular} ${peopleFax} -->
    </template:container>
<script type='text/javascript'>
jQuery(document).ready(function($) {
  $('a[rel*=facebox]').facebox({
                loadingImage:"<utility:resolvePath value='images/facebox/loading.gif'/>",
                closeImage :"<utility:resolvePath value='images/facebox/closelabel.gif'/>",
                overlay:true });
})
</script>

