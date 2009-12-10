<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<jsp:useBean id="now" class="java.util.Date"/>
<template:addWrapper name="wrapper.dashboard"/>
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
<jcr:nodePropertyRenderer node="${currentNode}" name="j:title" renderer="resourceBundle" var="title"/>
<c:if test="${not empty title and not empty fields['j:firstName'] and not empty fields['j:lastName']}">
<c:set var="person" value="${title.displayName} ${fields['j:firstName']} ${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and not empty fields['j:firstName'] and not empty fields['j:lastName']}">
<c:set var="person" value="${fields['j:firstName']} ${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and empty fields['j:firstName'] and not empty fields['j:lastName']}">
<c:set var="person" value="${fields['j:lastName']}"/>
</c:if>
<c:if test="${empty title and not empty fields['j:firstName'] and empty fields['j:lastName']}">
<c:set var="person" value="${fields['j:firstName']}"/>
</c:if>
<c:if test="${empty title and empty fields['j:firstName'] and empty fields['j:lastName']}">
<c:set var="person" value=""/>
</c:if>
<jcr:nodeProperty node="${currentNode}" name="j:birthDate" var="birthDate"/>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="yyyy" var="birthYear"/>
    <fmt:formatDate value="${now}" pattern="yyyy" var="currentYear"/>
</c:if>
<c:if test="${not empty birthDate}">
    <fmt:formatDate value="${birthDate.date.time}" pattern="dd/MM/yyyy" var="editBirthDate"/>
</c:if>
<fmt:formatDate value="${now}" pattern="dd/MM/yyyy" var="editNowDate"/>
<jcr:propertyInitializers node="${currentNode}" name="j:gender" var="genderInit"/>
<jcr:propertyInitializers node="${currentNode}" name="j:title" var="titleInit"/>
<script type="text/javascript">

    var genderMap = "{<c:forEach items="${genderInit}" varStatus="status" var="gender"><c:if test="${status.index > 0}">,</c:if>'${gender.value.string}':'${gender.displayName}'</c:forEach>}";
    var titleMap = "{<c:forEach items="${titleInit}" varStatus="status" var="title"><c:if test="${status.index > 0}">,</c:if>'${title.value.string}':'${title.displayName}'</c:forEach>}";

    $(document).ready(function() {
        $(".edit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.base}${currentNode.path}", data, function(result) {
                var j_title = "";
                if(result && typeof result.j_title != 'undefined')
                j_title = eval("datas="+titleMap)[result.j_title];
                var j_firstname = "";
                if(result && typeof result.j_firstName != 'undefined')
                j_firstname = result.j_firstName;
                var j_lastname = "";
                if(result && typeof result.j_lastName != 'undefined')
                j_lastname = result.j_lastName;
                $("#personDisplay2").html(j_title + " " + j_firstname + " " + j_lastname);
                $("#personDisplay1").html(j_title + " " + j_firstname + " " + j_lastname);
                if(result && result.j_email != 'undefined')
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
                var input = $('<div class="itemImage itemImageRight"><img src="' + result.j_picture + '/avatar_120" width="60" height="60"/></div>');
                $("#portrait").html(input);
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
            tooltip : 'Click to edit',
            datepicker : {
                flat: true,
                date: '<c:if test="${not empty editBirthDate}">${editBirthDate}</c:if><c:if test="${empty editBirthDate}">${editNowDate}</c:if>',
                format: 'd/m/Y',
                view: 'years',
                current: '<c:if test="${not empty editBirthDate}">${editBirthDate}</c:if><c:if test="${empty editBirthDate}">${editNowDate}</c:if>',
                calendars: 1,
                starts: 1     }
        });

        $(".genderEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.base}${currentNode.path}", data, null, "json");
            return eval("values="+genderMap)[value];
        }, {
            type    : 'select',
            data   : genderMap,
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit'
        });

        $(".titleEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id').replace("_", ":");
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post("${url.base}${currentNode.path}", data, function(result) {
                var j_title = result.j_title;
                j_title = eval("datas="+titleMap)[j_title];
                $("#personDisplay2").html(j_title + " " + result.j_firstName + " " + result.j_lastName);
                $("#personDisplay1").html(j_title + " " + result.j_firstName + " " + result.j_lastName);
                $("#emailDisplay").html(result.j_email);
            }, "json");
            return eval("values="+titleMap)[value];
        }, {
            type    : 'select',
            data   : titleMap,
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
                <img src="${picture.node.thumbnailUrls['avatar_120']}" alt="${person}"/>
            </c:if>
            <c:if test="${empty picture}">
                <span><fmt:message key="jnt_user.profile.uploadPicture"/></span>
            </c:if>
        </div>
    </div>

    <div class="box"><!--start box -->
        <div class="boxshadow boxpadding16 boxmarginbottom16">
            <div class="box-inner">
                <div class="box-inner-border">

                    <h3 class="boxtitleh3" id="personDisplay1"><c:out value="${person}"/></h3>

                    <div class="list3 user-profile-list">
                        <ul class="list3 user-profile-list">
                            <li><span class="label"><fmt:message
                                    key="jnt_user.profile.age"/> : </span> ${currentYear - birthYear} <fmt:message key="jnt_user.profile.years"/>
                            </li>
                            <li><span class="label"><fmt:message key="jnt_user.profile.sexe"/> : </span> <span
                                    class="genderEdit"
                                    id="j_gender"><jcr:nodePropertyRenderer node="${currentNode}" name="j:gender" renderer="resourceBundle"/></span>
                                <span class="visibilityEdit j_genderPublicEdit" id="j_genderPublic">
            <c:if test="${fields['j:genderPublic'] eq 'true'}">
                <fmt:message key="jnt_user.profile.public"/>
            </c:if>
            <c:if test="${fields['j:genderPublic'] eq 'false' or empty fields['j:genderPublic']}">
                <fmt:message key="jnt_user.profile.nonpublic"/>
            </c:if>
            </span>
                            </li>

                            <li><span class="label"><fmt:message key="jnt_user.j_email"/> : </span> <span id="j_email"
                                                                                                          class="edit">${fields['j:email']}</span><br/>
                                <span class="visibilityEdit" id="j_emailPublic">
                                <c:if test="${fields['j:emailPublic'] eq 'true'}">
                                    <fmt:message key="jnt_user.profile.public"/>
                                </c:if>
            <c:if test="${fields['j:emailPublic'] eq 'false' or empty fields['j:emailPublic']}">
                <fmt:message key="jnt_user.profile.nonpublic"/>
            </c:if></span></li>
                        </ul>
                    </div>
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
                    <h3 class="boxtitleh3"><fmt:message key="jnt_user.yourPreferences"/></h3>

                    <div class="preferencesForm"><!--start preferencesForm -->
                        <jcr:preference name="preferredLanguage" var="prefLangNode"
                                        defaultValue="${renderContext.request.locale}"/>
                        <fieldset>
                            <legend><fmt:message key="jnt_user.profile.preferences.form.name"/></legend>

                                <script type="text/javascript">
                                    $(document).ready(function() {
                                        $(".prefEdit").editable(function (value, settings) {
                                            var submitId = $(this).attr('id').replace("_", ":");
                                            var data = {};
                                            data[submitId] = value;
                                            data['methodToCall'] = 'put';
                                            $.post("${url.base}${prefLangNode.path}", data, null, "json");
                                            if (value == "en")
                                                return "English"; else if (value == "de")
                                                return "Deutsch"; else if (value == "fr")
                                                    return "French";
                                        }, {
                                            type    : 'select',
                                            data   : "{'en':'English','fr':'French','de':'Deutsch'}",
                                            onblur : 'ignore',
                                            submit : 'OK',
                                            cancel : 'Cancel',
                                            tooltip : 'Click to edit'
                                        });
                                    });
                                </script>
                            <label class="left"><fmt:message
                                    key="jnt_user.preference.preferredLanguage"/></label>
                            <div class="prefEdit" id="j_prefValue">
                                <c:choose>
                                    <c:when test="${prefLangNode.prefValue eq 'en'}">English</c:when>
                                    <c:when test="${prefLangNode.prefValue eq 'de'}">Deustch</c:when>
                                    <c:when test="${prefLangNode.prefValue eq 'fr'}">French</c:when>
                                </c:choose>
                            </div>
                        </fieldset>
                    </div>
                    <!--stop sendMailForm -->

                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>

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

    <div class="box">
        <div class="boxpadding16 boxmarginbottom16">
            <div class="box-inner">
                <div class="box-inner-border"><!--start box -->

                    <h3 class="boxtitleh3"><fmt:message key="jnt_user.j_about"/></h3>

                    <div class="ckeditorEdit j_aboutEdit" id="j_about">${fields['j:about']}</div>
            <span class="visibilityEdit" id="j:aboutPublic">
            <c:if test="${fields['j:aboutPublic'] eq 'true'}">
                <fmt:message key="jnt_user.profile.public"/>
            </c:if>
            <c:if test="${fields['j:aboutPublic'] eq 'false' or empty fields['j:aboutPublic']}">
                <fmt:message key="jnt_user.profile.nonpublic"/>
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

    <h3 class="titleIcon"><a href="#"><fmt:message key="jnt_user.profile.groups"/><img title="" alt=""
                                                                                       src="${url.currentModule}/images/groups.png"/></a>
    </h3>
    <ul class="list2 group-list">
        <c:forEach items="${jcr:getUserMembership(currentNode)}" var="group" varStatus="status">
            <li <c:if test="${status.last}">class="last"</c:if>>
                <div class="thumbnail">
                    <a href="#"><img src="${url.currentModule}/images/group-icon.png" alt="group" border="0"/></a>

                </div>
                <h4>
                    <a href="${url.base}${group.value.properties['j:fullpath']}.html">${group.value.groupname}(${fn:length(group.value.members)})</a>
                </h4>

                <div class='clear'></div>
            </li>
        </c:forEach>
    </ul>

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
