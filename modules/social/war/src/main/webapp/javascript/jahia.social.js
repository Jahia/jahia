function getText(node) {
	var title = getUserDisplayName(node);
	var username = node['username'];
	return username != title ? title + " (" + username + ")" : username; 
}

function searchUsers(findPrincipalURL, userURL, term, i18nAdd) {
	$.ajax({
        url: findPrincipalURL,
        type: 'post',
        dataType : 'json',
        data : {
			q: term
		},
        success: function(data) {
            $("#searchUsersResult").html("");
            $.each(data, function(i, item) {
                $("#searchUsersResult").append(
                        $("<tr/>")
                        		//.append($("<td/>").append($("<img/>").attr("src", item.properties['j:picture'])))
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
	var value =  node['j:firstName'] || '';
	if (value.length != 0) {
		value += ' ';
	}
	value += node['j:lastName'] || '';
	return value.length > 0 ? value : node['username'];
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


function submitStatusUpdate(base, modulePath, userPath, updateText) {
    $.ajax({
        url: base + userPath + '.addActivity.do',
        type : 'post',
        dataType : 'json',
        data : {
	    	'text': updateText
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