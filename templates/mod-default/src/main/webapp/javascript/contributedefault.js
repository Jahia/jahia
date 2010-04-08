var richTextEditors = {};
var contributionI18n = {
    'ok': 'OK',
    'cancel': 'Cancel',
    'edit': 'Click to edit',
    'uploaded': 'file uploaded click on preview to see the new file'
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
        tooltip : contributionI18n['edit']
    });

    $(".dateEdit" + id).editable(function (value, settings) {
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
        tooltip : contributionI18n['edit']
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
        target : function() {
            return $(this).attr('jcr:url');
        },
        callback : function (data, status,original) {
            var datas = {'methodToCall':'put'};
            datas[$(original).attr('jcr:id').replace("_", ":")] = data.uuids[0];
            $.post($(original).attr('jcr:url'), datas, function(result) {
                $(original).html($('<span>' + contributionI18n['uploaded'] + '</span>'));
            }, "json");
        }
    });
}

function invert(source, target, urlbase, callbackId, callbackUrl) {
    $.post(urlbase + source + ".move.do", {"action":"moveBefore", "target":target, "source":source},
        function(result) {
            replace(callbackId, callbackUrl, '');
        },
        'json'
    );

}

function deleteNode(source, urlbase, callbackId, callbackUrl) {
    $.post(urlbase + source, {"methodToCall":"delete"},
        function(result) {
            replace(callbackId, callbackUrl, '');
        },
        'json'
    );
}

function startWorkflow(source, process, urlbase, callbackId, callbackUrl) {
    $.post(urlbase + source + ".startWorkflow.do", {"process": process},
        function(result) {
            replace(callbackId, callbackUrl, '');
        },
        'json'
    );
}

function executeTask(source, action, outcome, urlbase, callbackId, callbackUrl) {
    $.post(urlbase + source + ".executeTask.do", {"action":action, "outcome":outcome},
        function(result) {
            replace(callbackId, callbackUrl, '');
        },
        'json'
    );
}