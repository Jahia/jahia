(function ($) {

    function get_id(element) {
        return $(element).attr('id') + '_ckeditor';
    }

    function remove_ck(id) {
    	try {
	    	var editor = CKEDITOR.instances[id];
	    	if (editor) {
	    		try {
	    			editor.destroy();
	    		} catch (de) {}
		        CKEDITOR.remove(editor);
		    	delete editor;
	    	}
    	} catch (e) {};
    }

    $.editable.addInputType('ckeditor', {
        element : function(settings, original) {
        	var id = get_id(original);
            var textarea = $('<textarea id="' + id + '"/>');
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
            remove_ck(id);
            var ckeditorType = settings.ckeditorType || (typeof contextJsParameters != 'undefined' ? 'User' : 'Mini');
            ckeditorType = $(original).attr('ckeditor:type') || ckeditorType;
            var ckeditorConfig = ckeditorType == 'Mini' ? {toolbar: 'Mini', filebrowserBrowseUrl: null, filebrowserFlashBrowseUrl: null, filebrowserImageBrowseUrl: null, filebrowserLinkBrowseUrl: null} : {toolbar: ckeditorType};
            eval('var originalConfig = ' + $(original).attr('ckeditor:config'));
          	ckeditorConfig = $.extend(ckeditorConfig, settings.ckeditorConfig, originalConfig);
            if((typeof settings.ckeditorToolbar) != 'undefined') {
            	ckeditorConfig = eval(settings.ckeditorToolbar);
            }
            var ckeditorInstance=CKEDITOR.replace(id, ckeditorConfig);
            if (typeof wcagCompliant == 'function') {
            	ckeditorInstance.checkWCAGCompliance=wcagCompliant;
            }
            ckeditorInstance.resetDirty();
        },
        submit : function(settings, original) {
            var id = get_id(original);
            $('#' + id).val(CKEDITOR.instances[id].getData());
            remove_ck(id);
        },
        reset : function(settings, original) {
            var id = get_id(original);
            remove_ck(id);
            $('#' + id).remove();
            original.reset();
        }
    });
})(jQuery);
