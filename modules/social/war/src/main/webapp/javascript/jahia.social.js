
/**
 * As any property can match the query, we try to intelligently display properties that either matched or make
 * sense to display.
 * @param node
 */
function getText(node) {
	var props = node.properties;
	if (props) {
		var result = "";
		if (props['jcr:primaryType'] == 'jnt:user') {
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
	} else {
		return node['username'];
	}
}

function format(result) {
    return getText(result);
}


function searchUsers(findPrincipalURL, userURL, term, i18nAdd) {
	$.ajax({
        url: findPrincipalURL,
        type: 'post',
        dataType : 'json',
        data : {
			q: term,
	        principalType: "users",
	        propertyMatchRegexp: "{$q}.*",
	        includeCriteriaNames: "username,j:nodename,j:firstName,j:lastName",
	        username: "{$q}*",
	        "j:nodename": "{$q}*",
	        "j:firstName": "{$q}*",
	        "j:lastName": "{$q}*",
	        removeDuplicatePropValues: "true"                
		},
        success: function(data) {
            $("#searchUsersResult").html("");
            $.each(data, function(i, item) {
                $("#searchUsersResult").append(
                        $("<tr/>").append($("<td/>").append($("<img/>").attr("src", item.properties['j:picture'])))
                                .append($("<td/>").attr("title", item['username']).text(getUserDisplayName(item)))
                                .append($("<td/>").attr("align", "center").append($("<a/>").attr("href", "#add")
                                .attr("class", "social-add").attr("title", i18nAdd ? i18nAdd : '').click(function () {
                            requestConnection(userURL + '.requestsocialconnection.do', item['userKey']);
                            return false;
                        })))
                        );
                if (i == 10) return false;
            });
        }
    });
}

function getUserDisplayName(node) {
	var props = node.properties;
	if (props) {
		var value =  props['j:firstName'] || '';
		if (value.length != 0) {
			value += ' ';
		}
		value += props['j:lastName'] || '';
		return value.length > 0 ? value : props['j:nodename'];
	} else {
		return node['username'];
	}
}

function requestConnection(userURL, toUserKey) {
    $.ajax({
        url : userURL,
        type : 'post',
        data : {
    		'connectionType': 'colleague',
    		'to': toUserKey
    	}, 
        success : function (data) {
            alert("Request completed successfully!");
        }
    });
}


function submitStatusUpdate(base, modulePath, userPath, userId, updateText) {
    $.ajax({
        url: base + userPath + '/activities/*',
        type : 'post',
        dataType : 'json',
        data : {
	    	nodeType: 'jnt:socialActivity',
	    	'j:message': updateText,
	    	'j:from': userId
    	},
        success : function (data) {
            loadActivities(base, modulePath, userPath);
        }
    });
}

function loadActivities(base, moduleUrl, userPath) {
    $.ajax({
        url: base + moduleUrl + '.activities.html.ajax?user='+userPath,
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