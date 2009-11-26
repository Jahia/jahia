<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<template:addResources type="css" resources="960.css,userProfile.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.editinplace.packed.js"/>

<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<c:set var="person" value="${fields['j:title']} ${fields['j:firstName']} ${fields['j:lastName']}"/>

<c:set var="userProperties" property="propertyName" value="${fn:escapeXml(fields['j:function'])}"/>

<h3 class="boxtitleh3"><c:out value="${person}"/></h3>

<div class="clear"></div>
<!-- twoCol clear -->
<ul class="list3 user-profile-list">
    <c:forTokens items="j:firstName,j:lastName,j:gender,j:title,j:birthDate,j:organization,j:function,j:about,j:email,j:phoneNumber,j:faxNumber,j:skypeID,j:twitterID,j:facebookID,j:linkedinID,j:picture"
            delims="," var="key">
        <script type="text/javascript">
        $(document).ready(function(){
            $(".${fn:replace(key,":","_")}Edit").editInPlace({
                url: "${url.base}${currentNode.path}",
                show_buttons: true,
                params: 'newNodeOutputFormat=html&methodToCall=put',
                update_value: '${key}',
                callback: function(ui, type, value)
				{
					ui.disable();

					// Display message to the user at the begining of request
					$("#messages${id}").text("Saving...").stop().css("opacity", 1).fadeIn(30);

					// Send request to the server using POST method
					$.post("${url.base}${currentNode.path}", {'${key}': value, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, null, "json");
				}
            });
        });
        </script>
            <li>
                <span class="label"><fmt:message key="${key}"/></span>
                <div class="${fn:replace(key,":","_")}Edit">
                        <c:if test="${empty fields[key]}">add property</c:if>
                        <c:if test="${!empty fields[key]}">${fields[key]}</c:if>
                </div>
                <c:set var="keyPublic" value="${fields[key]}Public"/>
            <span class="visibility">
            <c:if test="${fields[keyPublic]}">
                Visible
            </c:if>
            <c:if test="${!fields[keyPublic]}">
                <span class="visibility">Non Visible</span>
            </c:if>
            | <a title="" href="#" class="main" onclick="javascript:changeContent('${key}ID');">Changer</a></span>
            </li>
    </c:forTokens>
</ul>
<div class="clear"></div>
<!-- twoCol clear -->


<div class="AddItemForm">
    <!--start AddItemForm -->
    <form method="post" action="#">
        <fieldset>
            <legend>AddItemForm</legend>
            <p class="field">

                <label for="label2">Label :</label>
                <input type="text" name="label2" id="label2" class="AddItemFormLabel" value="Label" tabindex="9"/>
                <span> : </span>
                <label for="value2">Value :</label>
                <input type="text" name="value2" id="value2" class="AddItemFormValue" value="Value" tabindex="10"/>

                <input class="png gobutton" type="image" src="img/more.png" alt="Sidentifier" tabindex="11"/>
            </p>

        </fieldset>
    </form>
</div>
<!--stop sendMailForm -->
<div class="divButton">
    <a class="aButton" href="#"><span>Sauvegarder</span></a>
    <a class="aButton" href="#"><span>Annuler</span></a>

    <div class="clear"></div>
</div>
