CKEDITOR.config.toolbar_Engine =
[
    ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print','SpellChecker'],
    ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
    '/',
    ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
    ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote'],
    ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
    ['Link','Unlink','Anchor'],
    ['Image','Flash','Table','HorizontalRule','SpecialChar'],
    '/',
    ['Styles','Format','Font','FontSize'],
    ['TextColor','BGColor'],
    ['Maximize','ShowBlocks','-','About']
];
CKEDITOR.config.toolbar_Full = [
    ['Source','-','Save','NewPage','Preview','-','Templates'],
    ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print','SpellChecker','Scayt'],
    ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
    ['Form','Checkbox','Radio','TextField','Textarea','Select','Button','ImageButton','HiddenField'],
    '/',
    ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
    ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote'],
    ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
    ['Link','Unlink','Anchor'],
    ['Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak'],
    '/',
    ['Styles','Format','Font','FontSize'],
    ['TextColor','BGColor'],
    ['Maximize','ShowBlocks','-','About']
];


CKEDITOR.editorConfig = function(config)
{
    config.toolbarCanCollapse = true;
    config.toolbarStartupExpanded = true;
    config.filebrowserBrowseUrl = '/engines/gwtcontentpicker/contentpicker.jsp?type=filepicker';
    config.filebrowserImageBrowseUrl = '/engines/gwtcontentpicker/contentpicker.jsp?type=filepicker&mime=image/*';
    config.filebrowserFlashBrowseUrl = '/engines/gwtcontentpicker/contentpicker.jsp?type=flashpicker';
    config.filebrowserLinkBrowseUrl = '/engines/gwtcontentpicker/contentpicker.jsp?type=linkpicker';

    config.toolbar = 'Engine';

    config.toolbar_Engine =
    [
        ['Source','-','Preview'],
        ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print','SpellChecker'],
        ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
        '/',
        ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
        ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote'],
        ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
        ['Link','Unlink','Anchor'],
        ['Image','Flash','Table','HorizontalRule','SpecialChar'],
        '/',
        ['Styles','Format','Font','FontSize'],
        ['TextColor','BGColor'],
        ['Maximize','ShowBlocks','-','About']
    ];
};
