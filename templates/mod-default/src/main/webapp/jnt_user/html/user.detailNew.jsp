<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<template:addResources type="css" resources="960.css,userProfile.css"/>


<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<c:set var="person" value="${fields['j:title']} ${fields['j:firstName']} ${fields['j:lastName']}"/>

<c:set var="userProperties" property="propertyName" value="${fn:escapeXml(fields['j:function'])}"/>

<h3 class="boxtitleh3" id="personDisplay2"><c:out value="${person}"/></h3>

<div class="clear"></div>
<!-- twoCol clear -->
<ul class="list3 user-profile-list twoCol">
    <c:forTokens
            items="j:firstName,j:lastName,j:organization,j:function,j:skypeID,j:twitterID,j:facebookID,j:linkedinID"
            delims="," var="key">
        <li>
            <span class="label"><fmt:message key='jnt_user.${fn:replace(key,":","_")}'/></span>

            <div class="edit" id="${fn:replace(key,":","_")}"><c:if
                    test="${empty fields[key]}">Click here to edit</c:if><c:if
                    test="${!empty fields[key]}">${fields[key]}</c:if></div>
            <c:set var="pubKey" value="${key}Public"/>
            <span class="visibilityEdit" id="${fn:replace(key,":","_")}Public">
            <c:if test="${fields[pubKey] eq 'true'}"><fmt:message key="jnt_user.profile.public"/></c:if>
            <c:if test="${fields[pubKey] eq 'false' or empty fields[pubKey]}"><fmt:message
                    key="jnt_user.profile.nonpublic"/></c:if>
            </span>
        </li>
    </c:forTokens>
    <li>
        <span class="label"><fmt:message key="jnt_user.j_birthDate"/></span>
        <jcr:nodeProperty node="${currentNode}" name="j:birthDate" var="birthDate"/>
        <c:if test="${not empty birthDate}">
            <fmt:formatDate value="${birthDate.date.time}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
        </c:if>
        <c:if test="${empty birthDate}">
            <jsp:useBean id="now" class="java.util.Date"/>
            <fmt:formatDate value="${now}" pattern="dd, MMMM yyyy" var="displayBirthDate"/>
        </c:if>
        <div class="dateEdit" id="j_birthDate">${displayBirthDate}</div>
    </li>
    <li>
        <span class="label"><fmt:message key="jnt_user.j_title"/></span>

        <div class="edit" id="j_title">${fields['j:title']}</div>
            <span class="visibilityEdit j_titlePublicEdit" id="j_titlePublic">
            <c:if test="${fields['j:titlePublic'] eq 'true'}">
                <fmt:message key="jnt_user.profile.public"/>
            </c:if>
            <c:if test="${fields['j:titlePublic'] eq 'false' or empty fields['j:titlePublic']}">
                <fmt:message key="jnt_user.profile.nonpublic"/>
            </c:if>
            </span>
    </li>
</ul>

<%-- Phone Numbers--%>
<ul class="list3 user-profile-list twoCol">
    <jcr:node var="phones" path="${currentNode.path}/j:phones"/>
    <script type="text/javascript">
        function newPhone() {
            var data = {};
            data['j:number'] = $("#newPhoneNumber")[0].value;
            data['j:numberType'] =$("#newPhoneType")[0].value;
            data['nodeType'] = "jnt:phoneNumber";
            $.post("${url.base}${phones.path}/*", data, null, "json");
        }
    </script>
    <c:forEach items="${phones.children}" var="phone" varStatus="status">
        <script type="text/javascript">
            $(document).ready(function() {
                $(".phoneNumber${status.index}").editable(function (value, settings) {
                    var submitId = $(this).attr('id').replace("_", ":");
                    var data = {};
                    data[submitId] = value;
                    data['methodToCall'] = 'put';
                    $.post("${url.base}${phone.path}", data, null, "json");
                    return(value);
                }, {
                    type    : 'text',
                    onblur : 'ignore',
                    submit : 'OK',
                    cancel : 'Cancel',
                    tooltip : 'Click to edit'
                });
            });
        </script>
        <li><span
                class="label">${phone.propertiesAsString['j:numberType']} : </span>

            <div class="phoneNumber${status.index}" id="j_number">${phone.propertiesAsString['j:number']}</div>
        </li>
    </c:forEach>
    <li>
        <form action="" id="newPhone">
            <input type="text" id="newPhoneNumber" size="20" value="Phone Number"/>
            <input type="text" id="newPhoneType" size="20" value="Phone Type"/>
            <button type="button" onclick="newPhone();">Add</button>
        </form>
    </li>
    <jcr:node var="addresses" path="${currentNode.path}/j:addresses"/>
    <script type="text/javascript">
        function addNewAddress() {
            var data = {};
            data['j:street'] = $("#newStreet")[0].value;
            data['j:zipcode'] = $("#newZipCode")[0].value;
            data['j:town'] = $("#newTown")[0].value;
            data['nodeType'] = "jnt:address";
            $.post("${url.base}${addresses.path}/*", data, null, "json");
        }
    </script>
    <c:forEach items="${addresses.children}" var="address" varStatus="status">
        <script type="text/javascript">
            $(document).ready(function() {
                $(".address${status.index}").editable(function (value, settings) {
                    var submitId = $(this).attr('id').replace("_", ":");
                    var data = {};
                    data[submitId] = value;
                    data['methodToCall'] = 'put';
                    $.post("${url.base}${address.path}", data, null, "json");
                    return(value);
                }, {
                    type    : 'text',
                    onblur : 'ignore',
                    submit : 'OK',
                    cancel : 'Cancel',
                    tooltip : 'Click to edit'
                });
            });
        </script>
        <li><span
                class="label"><div class="address${status.index}" id="j_street">${address.propertiesAsString['j:street']}</div>
            <div class="address${status.index}" id="j_zipcode">${address.propertiesAsString['j:zipcode']}</div>
            <div class="address${status.index}" id="j_town">${address.propertiesAsString['j:town']}</div>
            <jcr:nodePropertyRenderer node="${address}" name="j:country" renderer="country"/></span></li>
    </c:forEach>
    <li>
        <form action="" id="newAddress">
            <input type="text" id="newStreet" size="20" value="Street"/>
            <input type="text" id="newZipCode" size="20" value="Zipcode"/>
            <input type="text" id="newTown" size="20" value="Town"/>
            <button type="button" onclick="addNewAddress();">Add</button>
        </form>
    </li>
</ul>
<%--
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
            </p>

        </fieldset>
    </form>
</div>--%>
