$(document).ready(function() {
    $(".edit").editable(function (value, settings) {
        var url = $(this).attr('jcr:url');
        var submitId = $(this).attr('id').replace("_", ":");
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
        var submitId = $(this).attr('id').replace("_", ":");
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
        var submitId = $(this).attr('id').replace("_", ":");
        var data = {};
        if (value.match("[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9]")) {
            var split = value.split("/");
            var birth = new Date();
            birth.setFullYear(split[2], split[1], split[0]);
            var month = "";
            if (birth.getMonth() < 10) {
                month = "0" + birth.getMonth();
            } else month = birth.getMonth();
            var hour = '00';
            if ($("#hour" + submitId).length) {
                hour = $("#hour" + submitId).text().trim();
            }
            var min = '00';
            if ($("#min" + submitId).length) {
                min = $("#min" + submitId).text().trim();
            }
            data[submitId] = birth.getFullYear() + '-' + month + '-' + birth.getDate() + 'T' + hour + ':' + min + ':00';
            data['methodToCall'] = 'put';
            $.post(url, data, function(result) {
            }, "json");
        }
        return(value);
    }, {
        type : 'datepicker',
        onblur : 'ignore',
        submit : 'OK',
        cancel : 'Cancel',
        tooltip : 'Click to edit',
        datepicker : {
            flat: true,
            date: '${not empty editBirthDate ? editBirthDate : editNowDate}',
            format: 'd/m/Y',
            view: 'years',
            current: '${not empty editBirthDate ? editBirthDate : editNowDate}',
            calendars: 1,
            starts: 1
        }
    });
});