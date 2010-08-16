<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="javascript" resources="jquery.min.js,jquery.validate.js,jquery.maskedinput-1.2.2.js"/>
<template:addResources type="css" resources="poll.css"/>

<script type="text/javascript">
function doVote(answers) {

    var answersList = document.forms['form_${currentNode.name}'].voteAnswer;
    answerUUID = null;

    for (i=0; i< answersList.length; i++) {
    	answer = answersList[i];
    	if (answer.checked) {
    		answerUUID = answer.value;
    		break;
    	}
    }


    if (answerUUID == null) {
        alert("Please select an answer");
    }

    var data = {};
    data["answerUUID"] = answerUUID;
    $.post("${url.base}${currentNode.path}.vote.do", data, function(result) {


        var answers = result.answerNodes;
        /* strAnswers = "";
        for (i=0; i<answers.length; i++) {
            strAnswers += "\nAnswer["+[i]+"] label : " + answers[i].label + "\nAnswer["+[i]+"] votes: " + answers[i].nbOfVotes;
        }

        alert("Question: " + result.question + "\nTotal votes: " + result.totalOfVotes + "\nanswers: " + strAnswers);
           */

	statDivTest = document.getElementById("statContainer_${currentNode.name}");
	if (statDivTest != null) {
	    statDivTest.parentNode.removeChild(statDivTest);
	}


        var statDiv = document.createElement("div");
        statDiv.id = "statContainer_${currentNode.name}";
        // statDiv.style.zIndex = 99999;
	pollVotes = Math.floor(result.totalOfVotes);


        for (i=0; i<answers.length; i++) {
            var statAnswerLabel = document.createElement("div");
            statAnswerLabel.id = "statContainer_${currentNode.name}_label_a"+[i];
            statAnswerLabel.innerHTML = answers[i].label;


            var statAnswerValue = document.createElement("div");
            statAnswerValue.id = "statContainer_${currentNode.name}_value_a"+[i];
            statAnswerValue.innerHTML = answers[i].nbOfVotes;
	    answerVotes = Math.floor(answers[i].nbOfVotes);
	    percentage = (answerVotes == 0 || pollVotes == 0)?0:answerVotes/pollVotes*100;
            statAnswerValue.style.width = (percentage * 1) + "%";
			statAnswerValue.className  = "barPoll barPollColor"+[i%8];

            statDiv.appendChild(statAnswerLabel);
            statDiv.appendChild(statAnswerValue);

        }

       document.getElementById("stats_${currentNode.name}").appendChild(statDiv);


    }, "json");


}

</script>

<div class=poll>

    <h3>
        ${currentNode.propertiesAsString['question']}
    </h3>


    <c:if test="${not renderContext.editMode}">
        <div id="formContainer_${currentNode.name}">
        <form id="form_${currentNode.name}" name="form_${currentNode.name}" method="post" >
    </c:if>
            <c:if test="${renderContext.editMode}">
                <div class="addanswers">
                <span>Add the answers here</span>
            </c:if>

            <template:area path="answers" areaType="jnt:answersList" editable="true"/>

            <c:if test="${renderContext.editMode}">
                </div>
            </c:if>

    <c:if test="${not renderContext.editMode}">
        <div class="validation"></div>
        <input class="button" type="button" value="Vote" onclick="doVote($('${currentNode.name}_voteAnswer').value);" />
        </form>
        </div>
    </c:if>

    <div id="stats_${currentNode.name}">

    </div>
</div>
