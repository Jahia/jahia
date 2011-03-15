<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:set var="cookieName" value="rated${currentNode.identifier}"/>
<c:choose>
    <c:when test="${renderContext.loggedIn and (empty cookie[cookieName])}">
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
                $("#rat${id}").children().not("select").hide();

                // Create stars for: Average rating
                $("#avg${id}").stars();

                // Create stars for: Rate this
                $("#rat${id}").stars({
                    inputType: "select",
                    cancelShow: false,
                    captionEl: $("#caption${id}"),
                    callback: function(ui, type, value) {
                        // Disable Stars while AJAX connection is active
                        ui.disable();

                        // Display message to the user at the begining of request
                        $("#messages${id}").text("Saving...").stop().css("opacity", 1).fadeIn(30);

                        // Send request to the server using POST method
                        $.post("${url.base}${currentNode.path}",
                        {'j:lastVote': value,methodToCall:"put",cookieName:"rated${currentNode.identifier}",cookieValue:"${currentNode.identifier}"},
                                function(result) {
                                    // Select stars from "Average rating" control to match the returned average rating value
                                    $("#avg${id}").stars("select",
                                            Math.round(result.j_sumOfVotes / result.j_nbOfVotes));
                                    // Update other text controls...
                                    $("#all_votes${id}").text(result.j_nbOfVotes);
                                    $("#all_avg${id}").text(('' + result.j_sumOfVotes / result.j_nbOfVotes).substring(0,
                                            3));
                                    // Display confirmation message to the user
                                    $("#messages${id}").html("<br/>Rating saved (" + value +
                                                             "). Thanks!").stop().css("opacity", 1).fadeIn(30);
                                    // Hide confirmation message and enable stars for "Rate this" control, after 2 sec...
                                    setTimeout(function() {
                                        $("#messages${id}").fadeOut(1000, function() {
                                            ui.enable();
                                        });
                                    }, 2000);
                                }, "json");
                    }
                });

                // Since the <option value="3"> was selected by default, we must remove selection from Stars.
                $("#rat${id}").stars("selectID", -1);

                // Create element to use for confirmation messages
                $('<div id="messages${id}"/>').appendTo("#rat${id}");
            });
        </script>

        <div class="ratings">

            <div class="rating-L"><strong><fmt:message key="label.AverageRating"/></strong>
        <span>(<span id="all_votes${id}">${nbVotes.long}</span> votes; <span
                id="all_avg${id}">${fn:substring(avg,0,3)}</span>)</span>

                <form id="avg${id}" style="width: 200px" action="">


                    <input type="radio" name="rate_avg" value="1" title="Poor"
                           disabled="disabled"
                           <c:if test="${avg > 1.0}">checked="checked"</c:if> />
                    <input type="radio" name="rate_avg" value="2" title="Fair"
                           disabled="disabled"
                           <c:if test="${avg > 2.0}">checked="checked"</c:if> />
                    <input type="radio" name="rate_avg" value="3" title="Average"
                           disabled="disabled"
                           <c:if test="${avg > 3.0}">checked="checked"</c:if> />
                    <input type="radio" name="rate_avg" value="4" title="Good"
                           disabled="disabled"
                           <c:if test="${avg > 4.0}">checked="checked"</c:if> />
                    <input type="radio" name="rate_avg" value="5" title="Excellent"
                           disabled="disabled"
                           <c:if test="${avg > 5.0}">checked="checked"</c:if> />

                </form>
            </div>


            <div class="rating-R"><strong><fmt:message key="label.rateThis"/>:</strong> <span id="caption${id}"></span>

                <form id="rat${id}" action="${url.base}${currentNode.path}" method="post">
                    <select name="j:lastVote">
                        <option value="1"><fmt:message key="label.rateThis.poor"/></option>
                        <option value="2"><fmt:message key="label.rateThis.fair"/></option>
                        <option value="3"><fmt:message key="label.rateThis.average"/></option>
                        <option value="4"><fmt:message key="label.rateThis.good"/></option>
                        <option value="5"><fmt:message key="label.rateThis.excellent"/></option>
                    </select>
                    <input type="submit" value="Rate it!"/>
                </form>
            </div>

        </div>
    </c:when>
    <c:otherwise>
        <%@include file="rating.hidden.average.readonly.jsp" %>
    </c:otherwise>
</c:choose>