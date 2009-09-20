<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:if test="${property.name == 'j:peoplePicture'}"><c:set var="peoplePicture" value="${property.string}"/></c:if>
<c:if test="${property.name == 'j:peopleLastname'}"><c:set var="peopleLastname" value="${property.string}"/></c:if>
<c:if test="${property.name == 'j:peopleFirstname'}"><c:set var="peopleFirstname" value="${property.string}"/></c:if>
<c:if test="${property.name == 'j:peopleFunction'}"><c:set var="peopleFunction" value="${property.string}"/></c:if>
<c:if test="${property.name == 'j:peopleBusinessUnit'}"><c:set var="peopleBusinessUnit" value="${property.string}"/></c:if>
<c:if test="${property.name == 'j:peopleEmail'}"><c:set var="peopleEmail" value="${property.string}"/></c:if>
<c:if test="${property.name == 'j:peopleBiography'}"><c:set var="peopleBiography" value="${property.string}"/></c:if>
<c:if test="${property.name == 'j:peopleTelephone'}"><c:set var="peopleTelephone" value="${property.string}"/></c:if>
<c:if test="${property.name == 'j:peopleCellular'}"><c:set var="peopleCellular" value="${property.string}"/></c:if>
<c:if test="${property.name == 'j:peopleFax'}"><c:set var="peopleFax" value="${property.string}"/></c:if>


    <div id="illustration2" style="background:transparent url(${background.node.url}) no-repeat top left;">
        <div class="illustration2-text" style='margin-top:${positionTop.string}px; margin-left:${positionLeft.string}px'>
            <h2>${title}</h2>
            <p>${cast.string}</p>
        <div class="clear"> </div></div>
    </div>

	<div class="peopleListItem">
		<div class="peoplePhoto">

        <img src="${peoplePicture.image.thumbnailUrl}" alt="${peopleFirstname}&nbsp;${peopleLastname} picture"></div>
        <div class="peopleBody">
            <h5>${peopleFirstname} ${peopleLastname.string}</h5>
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




<!-- unused fields ${peopleTelephone.string} ${peopleCellular.string} ${peopleFax.string} -->

<script type='text/javascript'>
jQuery(document).ready(function($) {
  $('a[rel*=facebox]').facebox({
                loadingImage:"<utility:resolvePath value='images/facebox/loading.gif'/>",
                closeImage :"<utility:resolvePath value='images/facebox/closelabel.gif'/>",
                overlay:true });
})
</script>

