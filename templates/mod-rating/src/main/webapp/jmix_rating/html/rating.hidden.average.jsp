<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<jcr:nodeProperty node="${currentNode}" name="j:nbOfVotes" var="nbVotes"/>
<jcr:nodeProperty node="${currentNode}" name="j:sumOfVotes" var="sumVotes"/>
<c:if test="${nbVotes.long > 0}">
    <c:set var="avg" value="${sumVotes.long / nbVotes.long}"/>
</c:if>
<c:if test="${nbVotes.long == 0}">
    <c:set var="avg" value="0"/>
</c:if>
<template:addResources type="css" resources="uni-form.css,ui.stars.css" nodetype="jmix:rating"/>
<template:addResources type="javascript" resources="jquery.min.js,ui.core.min.js,ui.stars.js" nodetype="jmix:rating"/>
<script type="text/javascript">
    $(function() {
        $("#avg${currentNode.name}").children().not(":input").hide();
        $("#rat${currentNode.name}").children().not("select").hide();

        // Create stars for: Average rating
        $("#avg${currentNode.name}").stars();

        // Create stars for: Rate this
        $("#rat${currentNode.name}").stars({
			inputType: "select",
            cancelShow: false,
            captionEl: $("#caption"),
			callback: function(ui, type, value)
				{
					// Disable Stars while AJAX connection is active
					ui.disable();

					// Display message to the user at the begining of request
					$("#messages").text("Saving...").stop().css("opacity", 1).fadeIn(30);

					// Send request to the server using POST method
					/* NOTE:
						The same PHP script is used for the FORM submission when Javascript is not available.
						The only difference in script execution is the returned value.
						For AJAX call we expect an JSON object to be returned.
						The JSON object contains additional data we can use to update other elements on the page.
						To distinguish the AJAX request in PHP script, check if the $_SERVER['HTTP_X_REQUESTED_WITH'] header variable is set.
						(see: demo5.php)
					*/
					$.post("${url.base}${currentNode.path}", {'j:lastVote': value, stayOnNode:"${url.base}${renderContext.mainResource.node.path}",newNodeOutputFormat:"html",methodToCall:"put"}, function(result)
					{
							// Select stars from "Average rating" control to match the returned average rating value
							$("#avg").stars("select", Math.round(result.${currentNode.name}.j_sumOfVotes/result.${currentNode.name}.j_nbOfVotes));

							// Update other text controls...
							$("#all_votes").text(result.${currentNode.name}.j_nbOfVotes);
							$("#all_avg").text(result.${currentNode.name}.j_sumOfVotes/result.${currentNode.name}.j_nbOfVotes);

							// Display confirmation message to the user
							$("#messages").text("Rating saved (" + value + "). Thanks!").stop().css("opacity", 1).fadeIn(30);

							// Hide confirmation message and enable stars for "Rate this" control, after 2 sec...
							setTimeout(function(){
								$("#messages").fadeOut(1000, function(){ui.enable();});
							}, 2000);
					}, "json");
				}
        });

        // Since the <option value="3"> was selected by default, we must remove selection from Stars.
        $("#rat${currentNode.name}").stars("selectID", -1);

        // Create element to use for confirmation messages
        $('<div id="messages"/>').appendTo("#rat${currentNode.name}");
    });
</script>

<div class="ratings">

    <div class="rating-L"><strong>Average rating</strong>
        <span>(<span id="all_votes">${nbVotes.long}</span> votes; <span
                id="all_avg">${avg}</span>)</span>

        <form id="avg${currentNode.name}" style="width: 200px" action="">


            <input type="radio" name="rate_avg" value="1" title="Poor"
                   disabled="disabled"
                   <c:if test="${avg > 1}">checked="checked"</c:if> />
            <input type="radio" name="rate_avg" value="2" title="Fair"
                   disabled="disabled"
                   <c:if test="${avg > 2}">checked="checked"</c:if> />
            <input type="radio" name="rate_avg" value="3" title="Average"
                   disabled="disabled"
                   <c:if test="${avg > 3}">checked="checked"</c:if> />
            <input type="radio" name="rate_avg" value="4" title="Good"
                   disabled="disabled"
                   <c:if test="${avg > 4}">checked="checked"</c:if> />
            <input type="radio" name="rate_avg" value="5" title="Excellent"
                   disabled="disabled"
                   <c:if test="${avg > 5}">checked="checked"</c:if> />

        </form>
    </div>


    <div class="rating-R"><strong>Rate this:</strong> <span id="caption"></span>

        <form id="rat${currentNode.name}" action="${url.base}${currentNode.path}" method="post">
            <select name="j:lastVote">
                <option value="1">Poor</option>
                <option value="2">Fair</option>
                <option value="3">Average</option>
                <option value="4">Good</option>
                <option value="5">Excellent</option>
            </select>
            <input type="submit" value="Rate it!"/>
        </form>
    </div>

</div>
