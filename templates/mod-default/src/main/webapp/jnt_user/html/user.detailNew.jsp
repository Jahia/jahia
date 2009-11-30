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

<h3 class="boxtitleh3" id="personDisplay2"><c:out value="${person}"/></h3>

<div class="clear"></div>
<!-- twoCol clear -->
<ul class="list3 user-profile-list">
    <c:forTokens
            items="j:firstName,j:lastName,j:email,j:organization,j:function,j:phoneNumber,j:faxNumber,j:skypeID,j:twitterID,j:facebookID,j:linkedinID"
            delims="," var="key">
        <script type="text/javascript">
            $(document).ready(function() {
                $(".${fn:replace(key,":","_")}Edit").editInPlace({
                    show_buttons: true,
                    callback: function(original_element, html, original) {
                        var value = html;
                        $.post("${url.base}${currentNode.path}", {'${key}': value, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, function(result){
                            $("#personDisplay2").html(result.j_title+" "+result.j_firstName+" "+result.j_lastName);
                            $("#personDisplay1").html(result.j_title+" "+result.j_firstName+" "+result.j_lastName);
                            $("#emailDisplay").html(result.j_email);
                        }, "json");
                        return(html);
                    }
                });
            });
            $(document).ready(function() {
                $(".${fn:replace(key,":","_")}PublicEdit").editInPlace({
                    show_buttons: true,
                    field_type: "select",
                    select_options: "true,false",
                    callback: function(original_element, html, original) {
                        $.post("${url.base}${currentNode.path}", {'${key}Public': html, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, null, "json");
                        if (html == "true")
                            return "Public"; else
                            return "Non Public";
                    }
                });
            });
        </script>
        <li>
            <span class="label"><fmt:message key='${fn:replace(key,":","_")}'/></span>

            <div class="${fn:replace(key,":","_")}Edit">
                <c:if test="${empty fields[key]}">add property</c:if>
                <c:if test="${!empty fields[key]}">${fields[key]}</c:if>
            </div>
            <c:set var="pubKey" value="${key}Public"/>
            <span class="visibility ${fn:replace(key,":","_")}PublicEdit">
            <c:if test="${fields[pubKey] eq 'true'}">
                Public
            </c:if>
            <c:if test="${fields[pubKey] eq 'false' or empty fields[pubKey]}">
                Non Public
            </c:if>
            </span>
        </li>
    </c:forTokens>
    <script type="text/javascript">
            $(document).ready(function() {
                $(".j_birthDateEdit").editInPlace({
                    show_buttons: true,
                    callback: function(original_element, html, original) {
                        var value = html;
                        if(value.match("[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9]")) {
                           var split = value.split("/");
                            var birth = new Date();
                            birth.setFullYear(split[2],split[1],split[0]);
                            var month = "";
                            if(birth.getMonth()<10) {
                                month = "0"+birth.getMonth();
                            } else month = birth.getMonth();
                            value = birth.getFullYear()+'-'+month+'-'+birth.getDate()+'T00:00:00';
                        $.post("${url.base}${currentNode.path}", {'j:birthDate': value, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, function(result){
                            //todo update age
                        }, "json");
                        return(html);
                        } return "error date must be day/month/year";
                    }
                });
            });
            $(document).ready(function() {
                $(".j_birthDatePublicEdit").editInPlace({
                    show_buttons: true,
                    field_type: "select",
                    select_options: "true,false",
                    callback: function(original_element, html, original) {
                        $.post("${url.base}${currentNode.path}", {'j:birthDatePublic': html, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, null, "json");
                        if (html == "true")
                            return "Public"; else
                            return "Non Public";
                    }
                });
            });
        </script>
        <li>
            <span class="label"><fmt:message key="j_birthDate"/></span>
            <jcr:nodeProperty node="${currentNode}" name="j:birthDate" var="birthDate"/>
            <c:if test="${not empty birthDate}">
                <fmt:formatDate value="${birthDate.date.time}" pattern="dd/MM/yyyy" var="displayBirthDate"/>
            </c:if>
            <c:if test="${empty birthDate}">
                <jsp:useBean id="now" class="java.util.Date"/>
                <fmt:formatDate value="${now}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
            </c:if>
            <div class="j_birthDateEdit">${displayBirthDate}</div>
        </li>
    <script type="text/javascript">
            $(document).ready(function() {
                $(".j_genderEdit").editInPlace({
                    show_buttons: true,
                    field_type: "select",
                    select_options: "male,female,other",
                    callback: function(original_element, html, original) {
                        var value = html;
                        $.post("${url.base}${currentNode.path}", {'j:gender': value, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, function(result){
                            //todo update age
                        }, "json");
                        return(html);
                    }
                });
            });
            $(document).ready(function() {
                $(".j_genderPublicEdit").editInPlace({
                    show_buttons: true,
                    field_type: "select",
                    select_options: "true,false",
                    callback: function(original_element, html, original) {
                        $.post("${url.base}${currentNode.path}", {'j:genderPublic': html, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, null, "json");
                        if (html == "true")
                            return "Public"; else
                            return "Non Public";
                    }
                });
            });
        </script>
        <li>
            <span class="label"><fmt:message key="j_gender"/></span>
            <div class="j_genderEdit">${fields['j:gender']}</div>
            <span class="visibility j_genderPublicEdit">
            <c:if test="${fields['j:genderPublic'] eq 'true'}">
                Public
            </c:if>
            <c:if test="${fields['j:genderPublic'] eq 'false' or empty fields['j:genderPublic']}">
                Non Public
            </c:if>
            </span>
        </li>
    <script type="text/javascript">
            $(document).ready(function() {
                $(".j_titleEdit").editInPlace({
                    show_buttons: true,
                    field_type: "select",
                    select_options: "mister,master,professor,doctor,miss,madam",
                    callback: function(original_element, html, original) {
                        var value = html;
                        $.post("${url.base}${currentNode.path}", {'j:title': value, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, function(result){
                            $("#personDisplay2").html(result.j_title+" "+result.j_firstName+" "+result.j_lastName);
                            $("#personDisplay1").html(result.j_title+" "+result.j_firstName+" "+result.j_lastName);
                        }, "json");
                        return(html);
                    }
                });
            });
            $(document).ready(function() {
                $(".j_genderPublicEdit").editInPlace({
                    show_buttons: true,
                    field_type: "select",
                    select_options: "true,false",
                    callback: function(original_element, html, original) {
                        $.post("${url.base}${currentNode.path}", {'j:titlePublic': html, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, null, "json");
                        if (html == "true")
                            return "Public"; else
                            return "Non Public";
                    }
                });
            });
        </script>
        <li>
            <span class="label"><fmt:message key="j_title"/></span>
            <div class="j_titleEdit">${fields['j:title']}</div>
            <span class="visibility j_titlePublicEdit">
            <c:if test="${fields['j:titlePublic'] eq 'true'}">
                Public
            </c:if>
            <c:if test="${fields['j:titlePublic'] eq 'false' or empty fields['j:titlePublic']}">
                Non Public
            </c:if>
            </span>
        </li>
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
