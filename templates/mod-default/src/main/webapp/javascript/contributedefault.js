$(document).ready(function() {
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
});