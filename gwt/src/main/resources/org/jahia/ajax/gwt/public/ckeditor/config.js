/*
Copyright (c) 2003-2010, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
*/

CKEDITOR.editorConfig = function( config )
{
    config.contextPath = (typeof jahiaGWTParameters != 'undefined') ? jahiaGWTParameters.contextPath : '';
    config.language = (typeof jahiaGWTParameters != 'undefined') ? jahiaGWTParameters.uilang : 'en';
    config.siteUuid = (typeof jahiaGWTParameters != 'undefined') ? jahiaGWTParameters.siteUuid : '';
    config.skin = 'jahia';
    
    config.filebrowserWindowWidth = '810';
    config.filebrowserWindowHeight = '650';
    config.filebrowserLinkWindowHeight = '800';
    config.filebrowserBrowseUrl = config.contextPath + '/engines/gwtcontentpicker/contentpicker.jsp?site=' + config.siteUuid;
    config.filebrowserImageBrowseUrl = config.contextPath + '/engines/gwtcontentpicker/contentpicker.jsp?mime=image%2F*&site=' + config.siteUuid;
    config.filebrowserFlashBrowseUrl = config.contextPath + '/engines/gwtcontentpicker/contentpicker.jsp?mime=application%2Fx-shockwave-flash%2Cvideo%2Fx-flv&site=' + config.siteUuid;
    config.filebrowserLinkBrowseUrl = config.contextPath + '/engines/gwtcontentpicker/contentpicker.jsp?type=linkpicker&site=' + config.siteUuid;
    config.image_previewText = '';

    config.toolbar = 'Full';
    config.toolbar_Full = [
        ['Source','-',/*'Save',*/'NewPage','Preview','-','Templates'],
        ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print', 'SpellChecker', 'Scayt', 'ACheck'],
        ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
        //['Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField'],
        '/',
        ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
        ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote','CreateDiv'],
        ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
        ['Link','Unlink','Anchor'],
        ['Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak'],
        '/',
        ['Styles','Format','Font','FontSize'],
        ['TextColor','BGColor'],
        ['Maximize', 'ShowBlocks','-','About']
    ];
    config.toolbar_Basic = [
        ['Source','-',/*'Save',*/'NewPage','Preview','-','Templates'],
        ['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print', 'SpellChecker', 'Scayt', 'ACheck'],
        ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
        //['Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField'],
        '/',
        ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
        ['NumberedList','BulletedList'/*,'-','Outdent','Indent','Blockquote','CreateDiv'*/],
        ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
        ['Link','Unlink','Anchor'],
        ['Image','Flash','Table','HorizontalRule','Smiley','SpecialChar'/*,'PageBreak'*/],
        '/',
        //['Styles','Format','Font','FontSize'],
        //['TextColor','BGColor'],
        ['Maximize', 'ShowBlocks','-','About']
    ];
    config.toolbar_Light = [
        ['Bold','Italic','Underline','Strike','-','NumberedList','BulletedList']
    ];
    config.toolbar_User = [
        ['Cut','Copy','Paste','PasteText','PasteFromWord','-', 'SpellChecker', 'Scayt', 'ACheck'],
        ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
        ['Link','Unlink','Anchor', 'Image','LinkFile'],
        ['HorizontalRule','Smiley','SpecialChar','PageBreak'],
        '/',
        ['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
        ['NumberedList','BulletedList','-','Outdent','Indent','Blockquote'],
        ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
        '/',
        ['Styles','Format','Font','FontSize'],
        ['TextColor','BGColor'],
        ['Maximize', 'ShowBlocks']
    ];
    
    config.extraPlugins = 'acheck';
};
