<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<jsp:useBean id="now" class="java.util.Date"/>
<template:addResources type="css" resources="960.css,userProfile.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript" resources="/gwt/resources/ckeditor/ckeditor.js,ckeditor_init.js,jquery.jeditable.ckeditor.js" />
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js" />

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
                var submitId = $(this).attr('id');
                var data = {};
                data[submitId] = value;
                data['methodToCall'] = 'put';
                $.post("${url.base}${currentNode.path}",
                       data,
                        function(result){
                    // $("#personDisplay2").html(result.j_title+" "+result.j_firstName+" "+result.j_lastName);
                    // $("#personDisplay1").html(result.j_title+" "+result.j_firstName+" "+result.j_lastName);
                    // $("#emailDisplay").html(result.j_email);
                }, "json");
                return(value);
        }, {
                type    : 'text'
            tooltip : 'Click to edit'
        });
        $(".visibilityEdit").editable(function (value, settings) {
                var submitId = $(this).attr('id');
                var data = {};
                data[submitId] = value;
                data['methodToCall'] = 'put';
                $.post("${url.base}${currentNode.path}", data, null, "json");
                if (value == "true")
                    return "Public"; else
                    return "Private";
            },{
                type    : 'select',
                data   : "{'true':'Public','false':'Private'}"
            tooltip : 'Click to edit'
        });

        $(".imageEdit").editable(function (value, settings) {
            var submitId = $(this).attr('id');
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
                $.post("${url.base}${currentNode.path}", data, function(result){
                }, "json");
                return(value);
        }, {
            type : 'ajaxupload',
            onblur : 'ignore',
            submit : 'OK',
            cancel : 'Cancel',
            tooltip : 'Click to edit'
        });


    });
</script>

<script type="text/javascript">
            $(document).ready(function() {



                $(".j_emailPublicEdit").editable(function (value, settings) {
                        $.post("${url.base}${currentNode.path}", {'j:emailPublic': value, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, null, "json");
                        if (value == "true")
                            return "Public"; else
                            return "Non Public";
                    },{
                        type    : 'text',
                        submit  : 'OK'
                });
                /*
                $(".j_emailPublicEdit").editInPlace({
                    show_buttons: true,
                    field_type: "select",
                    select_options: "true,false",
                    callback: function(original_element, html, original) {
                        $.post("${url.base}${currentNode.path}", {"j:emailPublic": html, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, null, "json");
                        if (html == "true")
                            return "Public"; else
                            return "Non Public";
                    }
                });
                */
            });
        </script>
<%--map all display values --%>
<jsp:useBean id="userProperties" class="java.util.HashMap"/>
<div class="container container_16"> <!--start container_16-->
<div class='grid_4'><!--start grid_4-->
<div class="image imageEdit">
		<div class="itemImage itemImageRight"><a href="#"><img alt="" src="img-text/user.gif"/></a></div>
</div>

<div class="box"><!--start box -->
  <div class="boxshadow boxpadding16 boxmarginbottom16">
                <div class="box-inner">
                    <div class="box-inner-border">

                    <h3 class="boxtitleh3" id="personDisplay1"><c:out value="${person}"/></h3>
                    <div class="list3 user-profile-list">
                    <ul class="list3 user-profile-list">
                    <li><span class="label">Age : </span> ${currentYear - birthYear} ans</li>
                    <li><span class="label">Sexe : </span> ${fields['j:gender']}</li>

                    <li><span class="label">Email Perso: </span> <span id="j:email" class="edit">${fields['j:email']}</span> <span class="visibility j_emailPublicEdit"><c:if test="${fields['j:emailPublic'] eq 'true'}">
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

                          <input type="text" name="label" id="label" class="AddItemFormLabel" value="Label" tabindex="9" /><span> : </span>
                            <label for="value" >Value :</label>

                            <input type="text" name="value" id="value" class="AddItemFormValue" value="Value" tabindex="10" />
                                                        <input class="png gobutton" type="image" src="img/more.png" alt="Sidentifier" tabindex="11"/>
                          </p>

                        </fieldset>
                      </form>
                    </div>
                    <div class="divButton">
	<a class="aButton" href="#"><span>Sauvegarder</span></a>

    <a class="aButton" href="#"><span>Annuler</span></a>
<div class="clear"></div></div>
                    <!--stop sendMailForm -->

                        <div class="clear"></div>
                  </div>
			</div>
		</div>
</div><!--stop box -->


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



<!--stop box -->
<div class='clear'></div></div><!--stop grid_4-->


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
</div><!--stop box -->
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
                     <option value="choose"> Langue par default </option>
                     <option value="langue1" selected="selected">Langue 1</option>
                     <option value="langue2">Langue 2</option>
                <option value="langue3">Langue 3</option>
                 </select>

                </p>
                </fieldset>
                </form>
                </div><!--stop preferencesForm -->
                    <div class="divButton">
	<a class="aButton" href="#"><span>Sauvegarder</span></a>
    <a class="aButton" href="#"><span>Annuler</span></a>
<div class="clear"></div></div>

                    <!--stop sendMailForm -->

                        <div class="clear"></div>
                  </div>
			</div>
		</div>
</div><!--stop box -->
<div class="box">
            <div class="boxpadding16 boxmarginbottom16">
                <div class="box-inner">
                    <div class="box-inner-border"><!--start box -->

                    <h3 class="boxtitleh3">About Me</h3>
                        <script type="text/javascript">
            $(document).ready(function() {
                $(".j_aboutEdit").editable(function (value, settings) {
                        $.post("${url.base}${currentNode.path}",
                               {'j:about': value, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, function(result){
                        }, "json");
                        return(value);
                }, {
                    type : 'ckeditor',
                    onblur : 'ignore',
                    submit : 'OK',
                    cancel : 'Cancel',
                    tooltip : 'Click to edit',
                    id : 'aboutMe'
                });
                /*
                $(".j_aboutEdit").editInPlace({
                    show_buttons: true,
                    field_type: "textarea",
                    textarea_rows: "40",
                    textarea_cols: "60",
                    callback: function(original_element, html, original) {
                        var value = html;
                        $.post("${url.base}${currentNode.path}", {'j:about': value, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, null, "json");
                        return(html);
                    }
                });
                */
            });
            $(document).ready(function() {
                $(".j_aboutPublicEdit").editable(function (value, settings) {
                        $.post("${url.base}${currentNode.path}", {'j:aboutPublic': value, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, null, "json");
                        if (value == "true")
                            return "Public"; else
                            return "Non Public";
                    },{
                        type    : 'text',
                        submit  : 'OK'
                });
                /*
                $(".j_aboutPublicEdit").editInPlace({
                    show_buttons: true,
                    field_type: "select",
                    select_options: "true,false",
                    callback: function(original_element, html, original) {
                        $.post("${url.base}${currentNode.path}", {'j:aboutPublic': html, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, null, "json");
                        if (html == "true")
                            return "Public"; else
                            return "Non Public";
                    }
                });
                */
            });
        </script>
                        <div class="j_aboutEdit">${fields['j:about']}</div>
            <span class="visibility j_aboutPublicEdit">
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
</div><!--stop box -->


	</div><!--stop grid_8-->
<div class='grid_4'><!--start grid_4-->
<div class="box">
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
</div><!--stop box -->

		<h3 class="titleIcon"><a href="#">Groupes<img title="" alt="" src="img-text/groups.png"/></a></h3>
		<ul class="list2 group-list">
<li>
        	<div class="thumbnail">
              <a href="#"><img src="img-text/group.png" alt="group" border="0"/></a>

            </div>
            <h4><a href="#">Nom de mon Groupe</a></h4><div class='clear'></div>
            </li>


<li><div class="thumbnail"><a href="#"><img src="img-text/group.png" alt="group" border="0"/></a></div>
        	<h4><a href="#">Nom de mon Groupe</a></h4><div class='clear'></div></li>
            <li><div class="thumbnail"><a href="#"><img src="img-text/group.png" alt="group" border="0"/></a></div>
        	<h4><a href="#">Nom de mon Groupe</a></h4><div class='clear'></div></li>

            <li><div class="thumbnail"><a href="#"><img src="img-text/group.png" alt="group" border="0"/></a></div>
        	<h4><a href="#">Nom de mon Groupe</a></h4><div class='clear'></div></li>
<li class="last">
        	<div class="thumbnail">
              <a href="#">
            <img src="img-text/group.png" alt="group" border="0"/>              </a>            </div><h4><a href="#">Nom de mon Groupe</a></h4><div class='clear'></div></li>
        </ul>



<!--stop box -->
<div class="box">
            <div class="boxpadding16 boxmarginbottom16">
                <div class="box-inner">
                    <div class="box-inner-border"><!--start box -->
                    <h3 class="boxtitleh3">M’envoyer un email</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. In ut sapien at nulla ultrices volutpat vel nec velit. Ut vel tortor tellus.
                      </p>

                      <div class="sendMailForm">
                      <!--start sendMailForm -->
                      <form method="post" action="#">
                        <fieldset>
                          <legend>Send mail</legend>
                        <p class="field">
                            <label for="from">From :</label>
                            <input type="text" name="from" id="from" class="sendMailFormFrom" value="Votre email" tabindex="6" />

                          </p>
                          <p class="field">
                            <label for="message" >Message :</label>
                            <textarea rows="7" cols="35" id="message" name="message" tabindex="7">Votre message</textarea>
                          </p>
                        <div class="divButton">
	<a class="aButton" tabindex="8" href="#"><span >Send mail</span></a>

<div class="clear"></div></div>
                        </fieldset>
                      </form>
                    </div>
                    <!--stop sendMailForm -->
                    <div class="clear"></div>
                    </div>
			</div>
		</div>

</div><!--stop box -->
	  <div class='clear'></div></div><!--stop grid_4-->


	<div class='clear'></div>
</div><!--stop container_16-->

<div class="clear"></div>
