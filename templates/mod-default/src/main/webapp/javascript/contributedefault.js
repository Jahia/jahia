function initEditFields() {
    $(".edit").editable(function (value, settings) {
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
        submit : 'OK',
        cancel : 'Cancel',
        tooltip : 'Click to edit'
    });

    $(".ckeditorEdit").editable(function (value, settings) {
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
        submit : 'OK',
        cancel : 'Cancel',
        tooltip : 'Click to edit'
    });

    $(".dateEdit").editable(function (value, settings) {
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
        submit : 'OK',
        cancel : 'Cancel',
        tooltip : 'Click to edit'
    });
};

$(document).ready(initEditFields);

function invert(source, target, urlbase, callbackId, callbackUrl) {
    var data = {};
    data["action"] = "moveBefore";
    data["target"] = target;
    data["source"] = source;
    var url = urlbase + source + ".move.do";
    $.ajax({
        type: "POST",
        url: url,
        data: data,
        complete: function(result) {
            replace(callbackId, callbackUrl, '');
        },
        dataType: 'json'
    });

}

function deleteNode(source, urlbase, callbackId, callbackUrl) {
    var data = {};
    data["methodToCall"] = "delete";
    var url = urlbase + source;
    $.ajax({
        type: "POST",
        url: url,
        data: data,
        complete: function(result) {
            replace(callbackId, callbackUrl, '');
        },
        dataType: 'json'
    });
}
