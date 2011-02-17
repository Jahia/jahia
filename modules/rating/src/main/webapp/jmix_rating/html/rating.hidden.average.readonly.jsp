<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<jcr:nodeProperty node="${currentNode}" name="j:nbOfVotes" var="nbVotes"/>
<jcr:nodeProperty node="${currentNode}" name="j:sumOfVotes" var="sumVotes"/>
<c:set var="id" value="${currentNode.identifier}"/>
<c:if test="${nbVotes.long > 0}">
    <c:set var="avg" value="${sumVotes.long / nbVotes.long}"/>
</c:if>
<c:if test="${nbVotes.long == 0}">
    <c:set var="avg" value="0.0"/>
</c:if>
<template:addResources type="css" resources="uni-form.css,ui.stars.css"/>
<template:addResources type="javascript" resources="jquery.js,jquery-ui.min.js"/>
<script type="text/javascript">
    $(document).ready(function() {
        $("#avg${id}").children().not(":input").hide();
        // Create stars for: Average rating
        $("#avg${id}").stars();
    });
</script>
<div style="display:none;">${fn:substring(avg,0,3)}</div>
<form id="avg${id}" action="">
    <input type="radio" name="rate_avg" value="1" title="Poor"
           disabled="disabled"
           <c:if test="${avg >= 1.0}">checked="checked"</c:if> />
    <input type="radio" name="rate_avg" value="2" title="Fair"
           disabled="disabled"
           <c:if test="${avg >= 2.0}">checked="checked"</c:if> />
    <input type="radio" name="rate_avg" value="3" title="Average"
           disabled="disabled"
           <c:if test="${avg >= 3.0}">checked="checked"</c:if> />
    <input type="radio" name="rate_avg" value="4" title="Good"
           disabled="disabled"
           <c:if test="${avg >= 4.0}">checked="checked"</c:if> />
    <input type="radio" name="rate_avg" value="5" title="Excellent"
           disabled="disabled"
           <c:if test="${avg >= 5.0}">checked="checked"</c:if> />
</form>
