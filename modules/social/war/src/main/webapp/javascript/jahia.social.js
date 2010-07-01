
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


function searchUsers(findPrincipalURL, userURL, term) {
    $.ajax({
        url: findPrincipalURL,
        type: 'post',
        dataType : 'json',
        data : "principalType=users&wildcardTerm=" + term + "*",
        success: function(data) {
            $("#searchUsersResult").html("");
            $.each(data, function(i, item) {
                $("#searchUsersResult").append(
                        $("<tr/>").append($("<td/>").append($("<img/>").attr("src", item.properties['j:picture'])))
                                .append($("<td/>").text(item.properties['j:firstName'] + " " + item.properties['j:lastName']))
                                .append($("<td/>").attr("align", "center").append($("<a/>").attr("href", "").attr("class", "social-add").click(function () {
                            requestConnection(userURL + '.startWorkflow.do', item['userKey']);
                            return false;
                        }).append($("<span/>").text("<fmt:message key='addAsFriend'/>"))))
                        );
                if (i == 10) return false;
            });
        }
    });
}

function requestConnection(userURL, toUserKey) {
    $.ajax({
        url : userURL,
        type : 'post',
        data : 'process=jBPM:user-connection&userkey=' + toUserKey,
        success : function (data) {
            alert("Request completed successfully!");
        }
    });
}


function submitStatusUpdate(userURL, userId, updateText) {
    $.ajax({
        url: userURL + '/activities/*',
        type : 'post',
        data : 'nodeType=jnt:socialActivity&newNodeOutputFormat=html&j:message=' + updateText + "&j:from=" + userId,
        success : function (data) {
            // alert("Status update submitted successfully");
            loadActivities(userURL);
        }
    });
}

function loadActivities(userURL) {
    $.ajax({
        url: userURL + '.activities.html',
        type: 'get',
        dataType : "html",
        success : function (data) {
            $(".activitiesList").html(data);
            initCuteTime();
        }

    });
    return false;
}


function custom_confirm(prompt, action, title){
    if (title === undefined) title = "Are you sure?";
        if ($("#confirm").length == 0){
                $("#main div.inner").append('<div id="confirm" title="' + title + '">' + prompt + '</div>');
                $("#confirm").dialog({buttons: {'Proceed': function(){ $(this).dialog('close'); action(); }, Cancel: function(){ $(this).dialog('close'); }}});
        }
        else {
                $("#confirm").html(prompt);
                $("#confirm").dialog('open');
    }
}

function removeSocialConnection(userURL, fromUserId, toUserId, connectionType, connectionRemovedCallback) {
    $.ajax({
        url: userURL + '.removesocialconnection.do',
        type: 'post',
        dataType : "json",
        success : function (data) {
            connectionRemovedCallBack(data, fromUserId, toUserId, connectionType);
        }

    });

}