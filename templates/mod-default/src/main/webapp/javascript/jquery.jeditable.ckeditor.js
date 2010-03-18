(function ($) {

    function get_id(element) {
        return $(element).attr('id') + '_ckeditor';
    }

    var editor = {};

    function remove_ck(id) {
        CKEDITOR.remove(editor[id]);
    }

    $.editable.addInputType('ckeditor', {
        element : function(settings, original) {
            var textarea = $('<textarea id="' + get_id(original) + '"/>');
            if (settings.rows) {
                textarea.attr('rows', settings.rows);
            } else {
                textarea.height(settings.height);
            }
            if (settings.cols) {
                textarea.attr('cols', settings.cols);
            } else {
                textarea.width(settings.width);
            }
            $(this).append(textarea);
            return(textarea);
        },
        plugin : function(settings, original) {
            var id = get_id(original);
            editor[id] = CKEDITOR.replace(id, { toolbar : 'User'});
        },
        submit : function(settings, original) {
            var id = get_id(original);
            $('#' + id).val(editor[id].getData());
            remove_ck(id);
        },
        reset : function(settings, original) {
            var id = get_id(original);
            editor[id] = CKEDITOR.replace(id, { toolbar : 'User'});
            original.reset();
        }
    });
})(jQuery);
