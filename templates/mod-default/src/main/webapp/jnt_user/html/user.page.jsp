<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<jsp:useBean id="now" class="java.util.Date"/>
<template:addResources type="css" resources="960.css,userProfile.css"/>
<template:addResources type="css" resources="datepicker.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/ckeditor/ckeditor.js,ckeditor_init.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="datepicker.js,jquery.jeditable.datepicker.js"/>

<c:set var="fields" value="${currentNode.propertiesAsString}"/>
<c:set var="person" value="${fields['j:title']} ${fields['j:firstName']} ${fields['j:lastName']}"/>
<jcr:nodeProperty node="${currentNode}" name="j:birthDate" var="birthDate"/>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="yyyy" var="birthYear"/>
    <fmt:formatDate value="${now}" pattern="yyyy" var="currentYear"/>
</c:if>
<script type="text/javascript">
    $(document).ready(function() {
        $(".edit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.base}${currentNode.path}", data, function(result) {
                $("#personDisplay2").html(result.j_title + " " + result.j_firstName + " " + result.j_lastName);
                $("#personDisplay1").html(result.j_title + " " + result.j_firstName + " " + result.j_lastName);
                $("#emailDisplay").html(result.j_email);
            }, "json");
            return(value);
        }, {
            type    : 'text',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit'
        });
        $(".visibilityEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.base}${currentNode.path}", data, null, "json");
            if (value == "true")
                return "Public"; else
                return "Private";
        }, {
            type    : 'select',
            data   : "{'true':'Public','false':'Private'}",
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit'
        });

        $(".imageEdit").editable('${url.base}${currentNode.path}', {
            type : 'ajaxupload',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit',
            callback : function (data, status) {
                uploadedImageCallback(data, status);
            }
        });

        function uploadedImageCallback(data, status) {
            var datas = {};
            datas['j:picture'] = data.uuids[0];
            datas['methodToCall'] = 'put';
            $.post('${url.base}${currentNode.path}', datas, function(result) {
                var input = $('<div class="itemImage itemImageRight"><img src="' + result.j_picture + '" width="60" height="60"/></div>');
                $("#portrait").html(input);
                $(".imageEdit").editable('${url.base}${currentNode.path}', {
                    type : 'ajaxupload',
                    onblur : 'ignore',
                    submit : 'OK',
                    cancel : 'Cancel',
                    tooltip : 'Click to edit',
                    callback : function (data, status) {
                        uploadedImageCallback(data, status);
                    }
                });
            }, "json");
        }

        $(".ckeditorEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.base}${currentNode.path}", data, function(result) {
            }, "json");
            return(value);
        }, {
            type : 'ckeditor',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit'
        });

        $(".dateEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            if (value.match("[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9]")) {
                var split = value.split("/");
                var birth = new Date();
                birth.setFullYear(split[2], split[1], split[0]);
                var month = "";
                if (birth.getMonth() < 10) {
                    month = "0" + birth.getMonth();
                } else month = birth.getMonth();
                data[submitId] = birth.getFullYear() + '-' + month + '-' + birth.getDate() + 'T00:00:00';
                data['methodToCall'] = 'put';
                $.post("${url.base}${currentNode.path}", data, function(result) {
                }, "json");
            }
            return(value);
        }, {
            type : 'datepicker',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit'
        });

        $(".genderEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.base}${currentNode.path}", data, null, "json");
            if (value == "male")
                return "Male"; else
                return "Female";
        }, {
            type    : 'select',
            data   : "{'male':'Male','female':'Female'}",
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit'
        });

    });
</script>
<%--map all display values --%>
<jsp:useBean id="userProperties" class="java.util.HashMap"/>
<div class="container container_16"> <!--start container_16-->
<div class='grid_4'><!--start grid_4-->
    <div class="image imageEdit" id="portrait">
        <div class="itemImage itemImageRight"><jcr:nodeProperty var="picture" node="${currentNode}" name="j:picture"/>
            <c:if test="${not empty picture}">
                <img src="${picture.node.url}" alt="${person}" width="60"
                     height="60"/>
            </c:if></div>
    </div>

    <div class="box"><!--start box -->
        <div class="boxshadow boxpadding16 boxmarginbottom16">
            <div class="box-inner">
                <div class="box-inner-border">

                    <h3 class="boxtitleh3" id="personDisplay1"><c:out value="${person}"/></h3>

                    <div class="list3 user-profile-list">
                        <ul class="list3 user-profile-list">
                            <li><span class="label">Age : </span> ${currentYear - birthYear} ans</li>
                            <li><span class="label">Sexe : </span> <span class="genderEdit"
                                                                         id="j_gender">${fields['j:gender']}</span>
                                <span class="visibilityEdit j_genderPublicEdit" id="j_genderPublic">
            <c:if test="${fields['j:genderPublic'] eq 'true'}">
                Public
            </c:if>
            <c:if test="${fields['j:genderPublic'] eq 'false' or empty fields['j:genderPublic']}">
                Non Public
            </c:if>
            </span>
                            </li>

                            <li><span class="label">Email Perso: </span> <span id="j_email"
                                                                               class="edit">${fields['j:email']}</span> <span
                                    class="visibilityEdit" id="j_emailPublic"><c:if
                                    test="${fields['j:emailPublic'] eq 'true'}">
                                Public
                            </c:if>
            <c:if test="${fields['j:emailPublic'] eq 'false' or empty fields['j:emailPublic']}">
                Non Public
            </c:if></span></li>
                        </ul>
                    </div>
                    <div class="AddItemForm">
                        <!--start AddItemForm -->
                        <form method="post" action="#">

                            <fieldset>
                                <legend>AddItemForm</legend>
                                <p class="field">
                                    <label for="label">Label :</label>

                                    <input type="text" name="label" id="label" class="AddItemFormLabel" value="Label"
                                           tabindex="9"/><span> : </span>
                                    <label for="value">Value :</label>

                                    <input type="text" name="value" id="value" class="AddItemFormValue" value="Value"
                                           tabindex="10"/>
                                    <input class="png gobutton" type="image" src="img/more.png" alt="Sidentifier"
                                           tabindex="11"/>
                                </p>

                            </fieldset>
                        </form>
                    </div>
                    <div class="divButton">
                        <a class="aButton" href="#"><span>Sauvegarder</span></a>

                        <a class="aButton" href="#"><span>Annuler</span></a>

                        <div class="clear"></div>
                    </div>
                    <!--stop sendMailForm -->

                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop box -->

    <%--

            <h3 class="titleIcon">Friends<img title="" alt="" src="img-text/friends.png"/></h3>
            <ul class="list2 friends-list">
    <li>
                <div class="thumbnail">
                  <a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a>            </div>
                <h4><a href="#"> Follower</a></h4>
    <div class='clear'></div></li>
    <li><div class="thumbnail"><a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a></div>

                <h4><a href="#">Follower</a></h4>
                <div class='clear'></div></li>
                <li><div class="thumbnail"><a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a></div>
                <h4><a href="#">Follower</a></h4>
                <div class='clear'></div></li>
                <li><div class="thumbnail"><a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a></div>
                <h4><a href="#">Follower</a></h4>

                <div class='clear'></div></li>
                <li><div class="thumbnail"><a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a></div>
                <h4><a href="#">Follower</a></h4>
                <div class='clear'></div></li>
                <li><div class="thumbnail"><a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a></div>
                <h4><a href="#">Follower</a></h4>
                <div class='clear'></div></li>
    <li class="last">

                <div class="thumbnail">
                <a href="#"><img src="img-text/friend.png" alt="friend" border="0"/></a>            </div><h4><a href="#"> Follower</a></h4>
                <div class='clear'></div></li>
            </ul>

    --%>


    <!--stop box -->
    <div class='clear'></div>
</div>
<!--stop grid_4-->


<div class='grid_8'><!--start grid_8-->

    <div class="box"><!--start box -->
        <div class="arrow-white-shadow-left"></div>
        <div class="boxshadow boxpadding16 boxmarginbottom16">
            <div class="box-inner">
                <div class="box-inner-border">
                    <template:module node="${currentNode}" template="detailNew"/>
                    <!--stop box -->
                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop box -->
    <div class="box"><!--start box -->
        <div class="boxshadow boxpadding16 boxmarginbottom16">
            <div class="box-inner">

                <div class="box-inner-border">
                    <h3 class="boxtitleh3">Preferences</h3>

                    <div class="preferencesForm"><!--start preferencesForm -->

                        <form method="post" action="#">
                            <fieldset>
                                <legend>Preferences Form</legend>
                                <p><label for="languages" class="left">Modifier la langue par default:</label>

                                    <select name="languages" id="languages" class="combo" tabindex="9">
                                        <option value="choose"> Langue par default</option>
                                        <option value="langue1" selected="selected">Langue 1</option>
                                        <option value="langue2">Langue 2</option>
                                        <option value="langue3">Langue 3</option>
                                    </select>

                                </p>
                            </fieldset>
                        </form>
                    </div>
                    <!--stop preferencesForm -->
                    <div class="divButton">
                        <a class="aButton" href="#"><span>Sauvegarder</span></a>
                        <a class="aButton" href="#"><span>Annuler</span></a>

                        <div class="clear"></div>
                    </div>

                    <!--stop sendMailForm -->

                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop box -->
    <div class="box">
        <div class="boxpadding16 boxmarginbottom16">
            <div class="box-inner">
                <div class="box-inner-border"><!--start box -->

                    <h3 class="boxtitleh3">About Me</h3>

                    <div class="ckeditorEdit j_aboutEdit" id="j_about">${fields['j:about']}</div>
            <span class="visibilityEdit" id="j:aboutPublic">
            <c:if test="${fields['j:aboutPublic'] eq 'true'}">
                Public
            </c:if>
            <c:if test="${fields['j:aboutPublic'] eq 'false' or empty fields['j:aboutPublic']}">
                Non Public
            </c:if>
            </span>

                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop box -->


</div>
<!--stop grid_8-->
<div class='grid_4'><!--start grid_4-->
    <%--<div class="box">
              <div class="boxshadow boxgrey boxpadding16 boxmarginbottom16">

                  <div class="box-inner">
                      <div class="box-inner-border"><!--start box -->







              <div class="thumbnail">
                <a href="#"><img src="img-text/rss.png" alt="" border="0"/></a>
              <div class='clear'></div></div>
              <h3 class="boxtitleh3"><a href="#">Follow me</a></h3>
              <p>dolor sit amet, consectetuer adipiscing elit. Morbi adipiscing, metus non ultricies pharetra</p>

                          <div class="clear"></div>

                    </div>
              </div>
          </div>
  </div>--%><!--stop box -->

    <h3 class="titleIcon"><a href="#">Groupes<img title="" alt="" src="img-text/groups.png"/></a></h3>
    <ul class="list2 group-list">
        <li>
            <div class="thumbnail">
                <a href="#"><img src="img-text/group.png" alt="group" border="0"/></a>

            </div>
            <h4><a href="#">Nom de mon Groupe</a></h4>

            <div class='clear'></div>
        </li>
        <li class="last">
            <div class="thumbnail">
                <a href="#">
                    <img src="img-text/group.png" alt="group" border="0"/> </a></div>
            <h4><a href="#">Nom de mon Groupe</a></h4>

            <div class='clear'></div>
        </li>
    </ul>


    <!--stop box -->
    <%--<div class="box">
        <div class="boxpadding16 boxmarginbottom16">
            <div class="box-inner">
                <div class="box-inner-border"><!--start box -->
                    <h3 class="boxtitleh3">M’envoyer un email</h3>

                    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. In ut sapien at nulla ultrices volutpat
                        vel nec velit. Ut vel tortor tellus.
                    </p>

                    <div class="sendMailForm">
                        <!--start sendMailForm -->
                        <form method="post" action="#">
                            <fieldset>
                                <legend>Send mail</legend>
                                <p class="field">
                                    <label for="from">From :</label>
                                    <input type="text" name="from" id="from" class="sendMailFormFrom"
                                           value="Votre email" tabindex="6"/>

                                </p>

                                <p class="field">
                                    <label for="message">Message :</label>
                                    <textarea rows="7" cols="35" id="message" name="message" tabindex="7">Votre
                                        message</textarea>
                                </p>

                                <div class="divButton">
                                    <a class="aButton" tabindex="8" href="#"><span>Send mail</span></a>

                                    <div class="clear"></div>
                                </div>
                            </fieldset>
                        </form>
                    </div>
                    <!--stop sendMailForm -->
                    <div class="clear"></div>
                </div>
            </div>
        </div>

    </div>--%>
    <!--stop box -->
    <div class='clear'></div>
</div>
<!--stop grid_4-->


<div class='clear'></div>
</div>
<!--stop container_16-->

<div class="clear"></div>
