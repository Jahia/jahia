var richTextEditors = {};
var contributionI18n = {
    'ok': 'OK',
    'cancel': 'Cancel',
    'edit': 'Click to edit',
    'uploaded': 'file uploaded click on preview to see the new file',
    'error' : 'an error occurred, please check the file you try to upload',
    'wcag.close': 'Close',
    'wcag.context': 'Context',
    'wcag.description': 'Description',
    'wcag.error': 'Error',
    'wcag.example': 'Example',
    'wcag.ignore': 'Ignore error(s)',
    'wcag.information': 'Information',
    'wcag.ok': 'WCAG Compliance: OK',
    'wcag.warning': 'Warning'
}
function initEditFields(id) {
    $(".edit" + id).editable(function (value, settings) {
        var data = {'methodToCall':'put'};
        var submitId = $(this).attr('jcr:id');
        data[submitId] = value;
        $.post($(this).attr('jcr:url'), data, null, "json");
        return(value);
    }, {
        type    : 'text',
        onblur : 'ignore',
        submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>' + contributionI18n['ok'] + '</button>',
        cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>' + contributionI18n['cancel'] + '</button>',
        tooltip : contributionI18n['edit']
    });

    $(".ckeditorEdit" + id).editable(function (value, settings) {
        var submitId = $(this).attr('jcr:id');
        var data = {'methodToCall':'put'};
        data[submitId] = value;
        $.post($(this).attr('jcr:url'), data, null, "json");
        return(value);
    }, {
        type : 'ckeditor',
        onblur : 'ignore',
        submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>' + contributionI18n['ok'] + '</button>',
        cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>' + contributionI18n['cancel'] + '</button>',
        tooltip : contributionI18n['edit'],
        onreset: function (settings, original) {
        	$('#wcag-' + $(original).attr('id') + '_ckeditor').remove();
        	return true;
        },
        onsubmit: function (settings, original) {
        	return wcagCompliant($(original).attr('id') + '_ckeditor', original);
        }
    });

    $(".dateEdit" + id).editable(function (value, settings) {
        var submitId = $(this).attr('jcr:id');
        var data = {'methodToCall':'put'};
        data[submitId] = value;
        $.post($(this).attr('jcr:url'), data, function(result) {
        }, "json");
        return(value.replace("T", " "));
    }, {
        type : 'datepicker',
        onblur : 'ignore',
        submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>' + contributionI18n['ok'] + '</button>',
        cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>' + contributionI18n['cancel'] + '</button>',
        tooltip : contributionI18n['edit'],
        loaddata : {defaultValue:$(".dateEdit" + id).attr('jcr:value')}
    });

    $(".dateTimeEdit" + id).editable(function (value, settings) {
        var submitId = $(this).attr('jcr:id');
        var data = {'methodToCall':'put'};
        data[submitId] = value;
        $.post($(this).attr('jcr:url'), data, function(result) {
        }, "json");
        return(value.replace("T", " "));
    }, {
        type : 'datetimepicker',
        onblur : 'ignore',
        submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>' + contributionI18n['ok'] + '</button>',
        cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>' + contributionI18n['cancel'] + '</button>',
        tooltip : contributionI18n['edit'],
        loaddata : {defaultValue:$(".dateTimeEdit" + id).attr('jcr:value')}
    });

    $(".choicelistEdit" + id).editable(function (value, settings) {
        var submitId = $(this).attr('jcr:id').replace("_", ":");
        var data = {'methodToCall':'put'};
        data[submitId] = value;
        $.post($(this).attr('jcr:url'), data, null, "json");
        return eval("values=" + $(this).attr('jcr:options'))[value];
    }, {
        type    : 'select',
        data   : function() {
            return $(this).attr('jcr:options');
        },
        onblur : 'ignore',
        submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>' + contributionI18n['ok'] + '</button>',
        cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>' + contributionI18n['cancel'] + '</button>',
        tooltip : contributionI18n['edit']
    });

    $(".file" + id).editable('', {
        type : 'ajaxupload',
        onblur : 'ignore',
        submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>' + contributionI18n['ok'] + '</button>',
        cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>' + contributionI18n['cancel'] + '</button>',
        tooltip : contributionI18n['edit'],
        target:$(".file" + id).attr('jcr:url'),
        callback : function (data, status,original) {
            var datas = {'methodToCall':'put'};
            var callableUrl = $(original).attr('jcr:url');
            datas[$(original).attr('jcr:id').replace("_", ":")] = data.uuids[0];
            $.post($(original).attr('jcr:url'), datas, function(result) {
                jreplace("renderingOfFile"+id, callableUrl+".html.ajax",null, null);
            }, "json");
        }
    });


    $(".fileSelector" + id).editable(function (value, settings) {
        var data = {'methodToCall':'put'};
        var submitId = $(this).attr('jcr:id');
        data[submitId] = value;
        var callableUrl = $(this).attr('jcr:url');
        $.post(callableUrl, data, function(result){
            jreplace("renderingOfFile"+id, callableUrl+".html.ajax",null, null);
        }, "json");
        return(value);
    }, {
        type    : 'treeItemSelector',
        onblur : 'ignore',
        submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>' + contributionI18n['ok'] + '</button>',
        cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>' + contributionI18n['cancel'] + '</button>',
        tooltip : contributionI18n['edit'],
        nodeTypes : $(".fileSelector" + id).attr('jeditabletreeselector:nodetypes'),
        selectableNodeTypes : $(".fileSelector" + id).attr('jeditabletreeselector:selectablenodetypes'),
        baseURL : $(".fileSelector" + id).attr('jeditabletreeselector:baseURL'),
        root : $(".fileSelector" + id).attr('jeditabletreeselector:root'),
        selectorLabel : $(".fileSelector" + id).attr('jeditabletreeselector:selectorLabel'),
        preview : $(".fileSelector" + id).attr('jeditabletreeselector:preview'),
        previewPath : $(".fileSelector" + id).attr('jeditabletreeselector:previewPath')
    });
}

function wcagCompliant(id, richTextElement, userTriggered) {
	var ctx = typeof jahiaJsParameters != 'undefined' ? jahiaJsParameters.contextPath : '';
	if (!userTriggered) {
		var wcag = typeof jahiaJsParameters != 'undefined' ? jahiaJsParameters.wcag : false;
		if (!wcag) {
			return true;
		}
	}
	var wcagOk = false;
    $.ajax({
    	async: false,
  	  	type: 'POST',
  	  	url:  ctx + '/cms/wcag/validate',
  	  	data: {'text':CKEDITOR.instances[id].getData()},
  	  	error: function (xhr, textStatus, errorThrown) {
  	  		wcagOk = true;
  	  	},
  	  	success: function (data, textStatus) {
  	  		var ignore = $('#wcag-ignore-' + id);
  	  		if ((userTriggered || ignore.length == 0 || ignore.attr('checked') == false) && (data.errors.length + data.warnings.length + data.infos.length > 0)) {
  	  			if (ignore.length > 0) {
  	  				$('#wcag-' + id).remove();
  	  			}
  	        	var myDiv = $('<div class="wcag-warnings" id="wcag-' + id + '">' +
      	        			'<div class="wcag-warnings-ignore"><input type="checkbox" id="wcag-ignore-' + id + '" value="true" name="ignore"/>&nbsp;<label for="wcag-ignore-' + id + '">' + (userTriggered ? contributionI18n['wcag.close'] : contributionI18n['wcag.ignore']) + '</label></div>' +
      	        			'<table class="table" width="100%" border="1" cellpadding="0" cellspacing="3">' +
      	        			'<thead><tr><th width="5%">#</th><th width="5%"> </th><th width="75%">' +
      	        			contributionI18n['wcag.description'] + '</th>' + 
      	        			/*
      	        			'<th width="10%">' + contributionI18n['wcag.context'] + '</th>' +
      	        			*/
      	        			'<th width="10%">' + contributionI18n['wcag.example'] + '</th></tr></thead>' + 
      	        			'<tbody id="wcag-violations-' + id + '">' +
      	        			'</tbody>' +
      	        			'</table>' +
      	        			'</div>');
  	        	$(richTextElement).before(myDiv);
  	        	if (userTriggered) {
  	        		$('#wcag-ignore-' + id).click(function() {
  	        			$('#wcag-' + id).remove();
  	        		})
  	        	}
  	        	var placeholder = $('#wcag-violations-' + id);
  	        	var count = 0;
  	        	$.each($.merge([], $.merge($.merge([], data.errors), data.warnings), data.infos), function(index, violation) {
  	        		var violationType = violation.type.toLowerCase();
  	        		var row = '<tr><td align="center">' + (++count) + '</td>';
  	        		row = row + '<td align="center"><img src="' + ctx + '/modules/default/images/icons/' +
  	        			violationType + '.png" height="16" width="16" alt="' + contributionI18n['wcag.' + violationType] + '"' + 
  	        			' title="' + contributionI18n['wcag.' + violationType] + '"/></td>';
  	        		row = row + '<td>' + violation.message + '</td>';
  	        		/*
  	        		row = row + '<td align="center">' +
  	        			'<a href="#wcag-context-' + id + '-' + count + '" class="wcag-context">' +
  	        			'<img src="' + ctx + '/modules/default/images/icons/about.png" height="16" width="16" alt="' + contributionI18n['wcag.context'] + '"' + 
  	        			' title="' + contributionI18n['wcag.context'] + '"/></a>' +
  	        			'<div style="display:none"><div id="wcag-context-' + id + '-' + count + '">' + 'ctx' + '</div></div>' +
  	        			'</td>';
  	        		*/
  	        		
  	        		row = row + '<td align="center">' + violation.example + '</td></tr>';
  	        		placeholder.append($(row));
  	            });
	        		$('a.wcag-context').fancybox({'titleShow': false, 'autoDimensions' : false, 'width' : 500, 'height' : 300});
  	  			wcagOk = false;
  	  		} else {
  	  			wcagOk = true;
  	        	$('#wcag-' + id).remove();
  	        	if (userTriggered && (data.errors.length + data.warnings.length + data.infos.length == 0)) {
  	        		alert(contributionI18n['wcag.ok']);
  	        	}
  	  		}
  	  	},
  	  	dataType: 'json'
 	});
	return wcagOk;
}

function checkWCAGCompliace(richTexts) {
	var wcagOk = true;
	$.each(richTexts, function (index, richTextElement) {
		if (!wcagCompliant($(richTextElement).attr('id'), richTextElement)) {
			wcagOk = false;
			return false;
		}
		return true;
	});
	
	return wcagOk;
}

function invert(source, target, urlbase, callbackId, callbackUrl,callbackJS) {
    $.post(urlbase + source + ".move.do", {"action":"moveBefore", "target":target, "source":source},
        function(result) {
            jreplace(callbackId, callbackUrl,null, callbackJS);
        },
        'json'
    );

}

function deleteNode(source, urlbase, callbackId, callbackUrl,callbackJS) {
    $.post(urlbase + source, {"methodToCall":"delete"},
        function(result) {
            jreplace(callbackId, callbackUrl,null, callbackJS);
        },
        'json'
    );
}

function startWorkflow(source, process, urlbase, callbackId, callbackUrl,callbackJS) {
    $.post(urlbase + source + ".startWorkflow.do", {"process": process},
        function(result) {
            jreplace(callbackId, callbackUrl,null, callbackJS);
        },
        'json'
    );
}

function executeTask(source, action, outcome, urlbase, callbackId, callbackUrl, callbackJS) {
    $.post(urlbase + source + ".executeTask.do", {"action":action, "outcome":outcome},
        function(result) {
            jreplace(callbackId, callbackUrl,null, callbackJS);
        },
        'json'
    );
}