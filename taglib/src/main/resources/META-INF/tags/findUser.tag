<%@ tag body-content="empty" description="Create an input field to select jahia user calling a find user Servlet require an import of jquery-ui.min.css" %>
<%@ tag dynamic-attributes="displayedAttributes"%>
<%@ attribute name="fieldId" required="true" type="java.lang.String"
              description="The input field ID to bind widget. Should be unique" %>
<%@ attribute name="fieldName" required="true" type="java.lang.String"
              description="The target hidden input field name attribute." %>
<%@ attribute name="fieldValue" required="false" type="java.lang.String"
              description="The input field default value. for ex. the previous search paramter(param['the field name']} value or another value" %>
<%@ attribute name="formID" required="false" type="java.lang.String"
              description="The target input field form id attribute." %>

<%-- to review --%>
<%@ attribute name="selectedValue" required="false" type="java.lang.String"
              description="The property value from user field to use to set the hidden input that will be used to submit to a form. Use 'username' or 'userkey'." %>

<%@ attribute name="cssClass" required="false" type="java.lang.String"
              description="The set of css classes to apply to the widget." %>

<%@ attribute name="style" required="false" type="java.lang.String"
              description="The style to apply to the field. By default only the height is applied with 26px" %>

<%@ attribute name="cssfile" required="false" type="java.lang.String"
              description="The set of css files to import as resources to apply to the widget. The file list should be separated by a comma (,)" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<%-- Import javascript resources --%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>

<%-- Import css resources --%>
<template:addResources type="css" resources="jquery-ui.min.css,jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<c:if test="${not empty cssfile}">
    <template:addResources type="css" resources="${cssfile}"/>
</c:if>

<%-- The user property to use to set the input to send to the target app --%>
<c:set value="userKey" var="selectedUserProp" />
<c:if test="${not empty selectedValue}">
    <c:if test="${fn:contains(selectedValue, \"username\")}">
        <c:set value="username" var="selectedUserProp" />
    </c:if>
</c:if>

<%-- The output come here for visible input field --%>
<c:set target="${displayedAttributes}" property="type" value="text"/>
<c:set target="${displayedAttributes}" property="id" value="${fieldId}"/>
<c:set target="${displayedAttributes}" property="name" value="${fieldId}_${fieldName}"/>
<c:set target="${displayedAttributes}" property="class" value="${functions:default(cssClass,'text-input ui-autocomplete-input')}" />
<c:set var="appliedStyle" value="height: 26px" />

<c:if test="${not empty style}" >
    <c:set var="appliedStyle" value="${appliedStyle};${style}" />
</c:if>
<c:set target="${displayedAttributes}" property="style" value="${appliedStyle}"/>
<c:set var="inputDefaultValue" value="${fn:escapeXml(functions:default(fieldValue, param['${fieldName}']))}" />

<%-- the widget visible input field --%>
<input ${functions:attributes(displayedAttributes)} value="${inputDefaultValue}" />
<c:set var="hiddenFieldID" value="hidden_${fieldId}" />
<%--
    FIXME: As the value of the previous input might be set from request params the create the hidden field. To prevent missing param at the next submit
--%>
<input type="hidden" name="${fieldName}" id="${hiddenFieldID}" value="${inputDefaultValue}" />


<script type="text/javascript">
    /* <![CDATA[ */

    $(document).ready(function() {

        function getUserDisplayName(node) {
            var value = node['j:firstName'] || '';
            if (value.length != 0) {
                value += ' ';
            }
            value += node['j:lastName'] || '';
            var title = value.length > 0 ? value : node['username'];
            var username = node['username'];
            return username != title ? title + " (" + username + ")" : username;
        }

        $("\#${fieldId}").bind("change paste keyup", function(){
            if(!$(this).val() || $(this).val().length === 0){
                if($('\#${hiddenFieldID}').length){
                    $('\#${hiddenFieldID}').attr({value:''});
                }
            }
        });

        $("\#${fieldId}").autocomplete(
                {
                    source: function(request, response){
                        $.ajax({
                            url: '<c:url value="${url.server}${url.findUser}" />',
                            data: {q: request.term},
                            dataType: "json",
                            type: "get",
                            success: function(datas){
                                var fusers = [];
                                $.each(datas, function(idx, data){
                                    data.label = getUserDisplayName(data);
                                    <%-- data.id = data['userKey']; --%>
                                    data.id = data['${selectedUserProp}'];
                                    fusers.push(data);
                                });

                                response(fusers);
                            }
                        });
                    },
                    dataType: "json",
                    cacheLength: 1,
                    select: function(event, ui) {
                        /*if(event.type == 'click'){

                        }*/
                        console.log("selected item " + ui.item.data);
                        return false;
                    }
                }
        ).data("ui-autocomplete")._renderMenu = function(ul, items){
            if(items && items.length > 0) {
                $.each(items, function (index, value) {
                    $('<li>').attr({id: value.id, class:'ui-menu-item'}).click(
                            function(ev){
                                var $t_input = $('\#${fieldId}');
                                $t_input.val($(this).text());

                                if($('\#${hiddenFieldID}').length){
                                    $('\#${hiddenFieldID}').attr({value:$(this).attr('id')});
                                } else {
                                    <%-- create a hidden input --%>
                                    var $hidden_input =  $('<input>').attr(
                                            {
                                                type: 'hidden',
                                                id: '${hiddenFieldID}',
                                                name: '${fieldName}',
                                                value: $(this).attr('id')
                                            }
                                    );
                                    // add hidden input to formID if required
                                    <c:choose>
                                        <c:when test="${fn:length(formID) > 0}">
                                            $hidden_input.appendTo($('\#${formID}'));
                                        </c:when>
                                        <c:otherwise>
                                            $t_input.closest('form').append($hidden_input);
                                        </c:otherwise>
                                    </c:choose>
                                }
                                $(".ui-menu-item").hide();
                                $(".ui-menu-item").closest('ul').empty();
                            }
                    ).append(value.label).appendTo(ul);
                });
            }
        };
    });

    /* ]]> */
</script>