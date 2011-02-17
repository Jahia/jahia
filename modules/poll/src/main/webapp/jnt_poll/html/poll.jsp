<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="javascript" resources="jquery.js,jquery.validate.js,jquery.maskedinput-1.2.2.js"/>
<template:addResources type="css" resources="poll.css"/>

<script type="text/javascript">

    function setCookie(c_name,value,expiredays)
    {
        var exdate=new Date();
        exdate.setDate(exdate.getDate()+expiredays);
        document.cookie=c_name+ "=" +escape(value)+
                ((expiredays==null) ? "" : ";expires="+exdate.toUTCString());
    }

    function getCookie(c_name)
    {
        if (document.cookie.length>0)
        {
            c_start=document.cookie.indexOf(c_name + "=");
            if (c_start!=-1)
            {
                c_start=c_start + c_name.length+1;
                c_end=document.cookie.indexOf(";",c_start);
                if (c_end==-1) c_end=document.cookie.length;
                return unescape(document.cookie.substring(c_start,c_end));
            }
        }
        return "";
    }
    if (getCookie('poll${currentNode.identifier}-${renderContext.user.username}') == 'true') {
        var data = {};
        $.post("${url.base}${currentNode.path}.pollResults.do", data, function(result) {


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
           $('#pollForm${currentNode.identifier}').hide();
        }, "json");


    }

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
        $.post("${url.base}${currentNode.path}.pollVote.do", data, function(result) {


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
           setCookie('poll${currentNode.identifier}-${renderContext.user.username}','true',365);
            $('#pollForm${currentNode.identifier}').hide();
        }, "json");


    }

</script>

<div class=poll>

    <h3>
        ${currentNode.propertiesAsString['question']}
    </h3>

    <div id="pollForm${currentNode.identifier}">
        <c:if test="${not renderContext.editMode}">
        <div id="formContainer_${currentNode.name}">
            <form id="form_${currentNode.name}" name="form_${currentNode.name}" method="post" >
                </c:if>
                <c:if test="${renderContext.editMode}">
                <div class="addanswers">
                    <span><fmt:message key="jnt_poll.addAnswers"/></span>
                    </c:if>

                    <template:list path="answers" listType="jnt:answersList" editable="true"/>

                    <c:if test="${renderContext.editMode}">
                </div>
                </c:if>

                <c:if test="${not renderContext.editMode}">
                <div class="validation"></div>
                <input class="button" type="button" value="Vote" onclick="doVote($('${currentNode.name}_voteAnswer').value);" />
            </form>
        </div>
        </c:if>
    </div>

    <div id="stats_${currentNode.name}">

    </div>
</div>
