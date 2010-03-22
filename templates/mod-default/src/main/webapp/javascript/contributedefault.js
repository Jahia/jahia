var richTextEditors = {};

function initEditFields(id) {
    $(".edit" + id).editable(function (value, settings) {
        var url = $(this).attr('jcr:url');
        var submitId = $(this).attr('jcr:id');
        var data = {};
        data[submitId] = value;
        data['methodToCall'] = 'put';
        $.post(url, data, null, "json");
        return(value);
    }, {
        type    : 'text',
        onblur : 'ignore',
        submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>OK</button>',
        cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>Cancel</button>',
        tooltip : 'Click to edit'
    });

    $(".ckeditorEdit" + id).editable(function (value, settings) {
        var url = $(this).attr('jcr:url');
        var submitId = $(this).attr('jcr:id');
        var data = {};
        data[submitId] = value;
        data['methodToCall'] = 'put';
        $.post(url, data, null, "json");
        return(value);
    }, {
        type : 'ckeditor',
        onblur : 'ignore',
        submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>OK</button>',
        cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>Cancel</button>',
        tooltip : 'Click to edit'
    });

    $(".dateEdit" + id).editable(function (value, settings) {
        var url = $(this).attr('jcr:url');
        var submitId = $(this).attr('jcr:id');
        var data = {};
        data[submitId] = value;
        data['methodToCall'] = 'put';
        $.post(url, data, function(result) {
        }, "json");
        return(value.replace("T", " "));
    }, {
        type : 'datetimepicker',
        onblur : 'ignore',
        submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>OK</button>',
        cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>Cancel</button>',
        tooltip : 'Click to edit'
    });

    $(".choicelistEdit" + id).editable(function (value, settings) {
        var url = $(this).attr('jcr:url');
        var submitId = $(this).attr('jcr:id').replace("_", ":");
        var data = {};
        data[submitId] = value;
        data['methodToCall'] = 'put';
        $.post(url, data, null, "json");
        return eval("values=" + $(this).attr('jcr:options'))[value];
    }, {
        type    : 'select',
        data   : function() {
            return $(this).attr('jcr:options');
        },
        onblur : 'ignore',
        submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>OK</button>',
        cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>Cancel</button>',
        tooltip : 'Click to edit'
    });

    $(".file" + id).editable('', {
        type : 'ajaxupload',
        onblur : 'ignore',
        submit : '<button type="submit"><span class="icon-contribute icon-accept"></span>OK</button>',
        cancel : '<button type="submit"><span class="icon-contribute icon-cancel"></span>Cancel</button>',
        tooltip : 'Click to edit',
        target : function() {
            return $(this).attr('jcr:url');
        },
        callback : function (data, status,original) {
            var datas = {};
            datas[$(original).attr('jcr:id').replace("_", ":")] = data.uuids[0];
            datas['methodToCall'] = 'put';
            $.post($(original).attr('jcr:url'), datas, function(result) {
                $(original).html($('<span>file uploaded click on preview to see the new file</span>'));
            }, "json");
        }
    });
}

function invert(source, target, urlbase, callbackId, callbackUrl) {
    var data = {};
    data["action"] = "moveBefore";
    data["target"] = target;
    data["source"] = source;
    var url = urlbase + source + ".move.do";
    $.post(
        url,
        data,
        function(result) {
            replace(callbackId, callbackUrl, '');
        },
        'json'
    );

}

function deleteNode(source, urlbase, callbackId, callbackUrl) {
    var data = {};
    data["methodToCall"] = "delete";
    var url = urlbase + source;
    $.post(
        url,
        data,
        function(result) {
            replace(callbackId, callbackUrl, '');
        },
        'json'
    );
}

function startWorkflow(source, process, urlbase, callbackId, callbackUrl) {
    var data = { process: process };
    var url = urlbase + source + ".startWorkflow.do";
    $.post(
        url,
        data,
        function(result) {
            replace(callbackId, callbackUrl, '');
        },
        'json'
    );
}

function executeTask(source, action, outcome, urlbase, callbackId, callbackUrl) {
    var data = { action:action , outcome:outcome };
    var url = urlbase + source + ".executeTask.do";
    $.post(
        url,
        data,
        function(result) {
            replace(callbackId, callbackUrl, '');
        },
        'json'
    );
}
