
/**
 * As any property can match the query, we try to intelligently display properties that either matched or make
 * sense to display.
 * @param node
 */
function getText(node) {
	var props = node.properties;
	if (props['jcr:primaryType'] == 'jnt:user') {
		var result = "";
		if (props['j:firstName'] && props['j:firstName'].length > 0) {
			result += props['j:firstName'];
		}
		if (props['j:lastName'] && props['j:lastName'].length > 0) {
			if (result.length > 0) {
				result += " ";
			}
			result += props['j:lastName'];
		}
		return result.length > 0 ? result + " (" + props['j:nodename'] + ")" : props['j:nodename']; 
	}
	
    if (node.matchingProperties && node.matchingProperties.length > 0) {
        var firstMatchingProperty = node.matchingProperties[0];
        return props[firstMatchingProperty];
    }
    if (props["jcr:title"] != null) {
        return props["jcr:title"];
    } else if (props["text"] != null) {
        return props["text"];
    } else if (props["j:nodename"] != null) {
        return props["j:nodename"];
    }
}

function format(result) {
    return getText(result);
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
                                .append($("<td/>").attr("title", item.properties['j:nodename']).text(getUserDisplayName(item.properties)))
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

function getUserDisplayName(props) {
	var value =  props['j:firstName'] || '';
	if (value.length != 0) {
		value += ' ';
	}
	value += props['j:lastName'] || '';
	return value.length > 0 ? value : props['j:nodename'];	
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
        data: {
    		"fromUserId": fromUserId,
    		"toUserId": toUserId,
    		"connectionType": connectionType
    	},
        success : function (data) {
    		connectionRemovedCallback(data, fromUserId, toUserId, connectionType);
        }
    });

}

function removeSocialMessage(userURL, msgId, callback) {
    $.ajax({
        url: userURL + '.removesocialmessage.do',
        type: 'post',
        dataType : "json",
        data: {
    		"messageId": msgId
    	},
        success : function (data) {
    		callback(data, msgId);
        }
    });
}