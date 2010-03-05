(function ($){ 
  
  function get_id(element) {
    return $(element).attr('id') + '_ckeditor';
  }
  
  var editor = undefined;
  
  function remove_ck() {
   if(editor !== undefined) {
      CKEDITOR.remove(editor);
      editor = undefined;
    }
  }
  
  $.editable.addInputType('ckeditor', {
     element : function(settings, original) {
        var textarea = $('<textarea id="' + get_id(original) +'"/>');
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
        editor = CKEDITOR.replace(get_id(original), { toolbar : 'User'});
      },
      submit : function(settings, original) {
        $('#' + get_id(original)).val(editor.getData());
        remove_ck();
      },
      reset : function(settings, original) {
        replace_ck();
        original.reset();
      }
  });
})(jQuery);
