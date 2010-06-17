<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<jsp:useBean id="now" class="java.util.Date"/>
<template:addResources type="css" resources="userProfile.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ajaxupload.js"/>
<template:addResources type="javascript" resources="jquery.ajaxfileupload.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<template:addResources type="javascript" resources="jquery-ui.datepicker.min.js,jquery.jeditable.datepicker.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>

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
<%--map all display values --%>
<jsp:useBean id="userProperties" class="java.util.HashMap"/>

<script type="text/javascript">
    $(document).ready(function() {

        /**
         * As any property can match the query, we try to intelligently display properties that either matched or make
         * sense to display.
         * @param node
         */
        function getText(node) {
            if (node.matchingProperties.length > 0) {
                var firstMatchingProperty = node.matchingProperties[0];
                return node[firstMatchingProperty];
            }
            if (node["jcr:title"] != null) {
                return node["jcr:title"];
            } else if (node["text"] != null) {
                return node["text"];
            } else if (node["j:nodename"] != null) {
                return node["j:nodename"];
            }
        }

        function format(result) {
            return getText(result["node"]);
        }

        $("#searchUsersTerm").autocomplete("${url.findPrincipal}", {
            dataType: "json",
            cacheLength: 1,
            parse: function parse(data) {
                return $.map(data, function(row) {
                    return {
                        data: row,
                        value: getText(row["node"]),
                        result: getText(row["node"])
                    }
                });
            },
            formatItem: function(item) {
                return format(item);
            },
            extraParams: {
                principalType : "users",
                wildcardTerm : "*"
            }
        });

        function searchUsers(term) {
            $.ajax({
                url: '${url.findPrincipal}',
                type: 'post',
                dataType : 'json',
                data : "principalType=users&wildcardTerm=" + term + "*",
                success: function(data) {
                    $("#searchUsersResult").html("");
                    $.each(data, function(i, item) {
                        $("#searchUsersResult").append(
                           $("<tr/>").append( $("<td/>").append($("<img/>").attr("src", item.properties['j:picture'])) )
                                   .append( $("<td/>").text(item.properties['j:firstName'] + " " + item.properties['j:lastName']))
                                   .append( $("<td/>").append( $("<a/>").attr("href", "").click(function () { requestConnection('${currentNode.path}.startWorkflow.do',item['userKey']); return false; }).text("Add as friend") ) )
                        );
                        if (i == 10) return false;
                    });
                }
            });
        }

        function requestConnection(actionURL, toUserKey) {
            $.ajax({
                url : '${url.base}' + actionURL,
                type : 'post',
                data : 'process=jBPM:user-connection&userkey=' + toUserKey,
                success : function (data) {
                    alert("Request completed successfully!");
                }
            });
        }

        $("#searchUsersSubmit").click(function() {
            // validate and process form here
            var term = $("input#searchUsersTerm").val();
            searchUsers(term);
            return false;
        });

    });
</script>


<div class='grid_4 alpha'><!--start grid_4-->

    <h3><fmt:message key="userSearch" /></h3>

    <form method="get" class="simplesearchform" action="">

        <jcr:nodeProperty name="jcr:title" node="${currentNode}" var="title"/>
        <c:if test="${not empty title.string}">
            <label for="searchUsersTerm">${fn:escapeXml(title.string)}:&nbsp;</label>
        </c:if>
        <fmt:message key='userSearch' var="startSearching"/>
        <input type="text" id="searchUsersTerm" value="${startSearching}"
               onfocus="if(this.value==this.defaultValue)this.value='';"
               onblur="if(this.value=='')this.value=this.defaultValue;" class="text-input"/>
        <input class="searchsubmit" id="searchUsersSubmit" type="submit" title="<fmt:message key='search.submit'/>"/>

    </form>
    <br class="clear"/>

    <div>
        <table width="100%" class="table">
            <thead>
                <tr>
                    <th><fmt:message key="userIcon"/></th>
                    <th><fmt:message key="userInfo"/></th>
                    <th><fmt:message key="userActions"/></th>
                </tr>
            </thead>
            <tbody id="searchUsersResult">

            </tbody>
        </table>
    </div>

    <jcr:sql var="userConnections"
         sql="select * from [jnt:userConnection] as uC where isdescendantnode(uC,['${currentNode.path}'])"/>
    
    <h3 class="titleIcon">Friends<img title="" alt="" src="img-text/friends.png"/></h3>
    <ul class="friends-list">
        <c:forEach items="${userConnections.nodes}" var="userConnection">
        <li>
            <!--
              From : ${userConnection.properties['j:connectedFrom'].node.properties['j:nodename'].string}
              To : ${userConnection.properties['j:connectedTo'].node.properties['j:nodename'].string}
              Type : ${userConnection.properties['j:type'].string}
            -->
            <div class="thumbnail">
                <a href="user.myconnections.jsp#"><img src="img-text/friend.png" alt="friend" border="0"/></a></div>
            <h4><a href="user.myconnections.jsp#">${userConnection.properties['j:connectedTo'].node.properties['j:firstName'].string} ${userConnection.properties['j:connectedTo'].node.properties['j:lastName'].string}</a></h4>

            <div class='clear'></div>
        </li>
        </c:forEach>
    </ul>


    <div class='clear'></div>


</div>
<!--stop grid_4-->

<div class='clear'></div>
