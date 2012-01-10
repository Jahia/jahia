/*
Copyright (c) 2003-2010, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
*/

CKEDITOR.editorConfig = function( config )
{
    config.contextPath = (typeof contextJsParameters != 'undefined') ? contextJsParameters.contextPath : '';
    config.language = (typeof contextJsParameters != 'undefined') ? contextJsParameters.uilang : 'en';
    config.contentlanguage = (typeof contextJsParameters != 'undefined') ? contextJsParameters.lang : 'en';
    config.siteUuid = (typeof contextJsParameters != 'undefined') ? contextJsParameters.siteUuid : '';
    config.skin = 'jahia';
    
    config.filebrowserWindowWidth = '810';
    config.filebrowserWindowHeight = '650';
    config.filebrowserLinkWindowHeight = '800';
    config.filebrowserWindowFeatures = 'location=no,menubar=no,toolbar=no,dependent=yes,minimizable=no,modal=yes,alwaysRaised=yes,resizable=yes,scrollbars=yes';
    config.filebrowserWindowName = 'JahiaFileBrowser'; 
    config.filebrowserBrowseUrl = config.contextPath + '/engines/contentpicker.jsp?site=' + config.siteUuid + '&lang='+ config.contentlanguage + '&uilang='+ config.language;
    config.filebrowserImageBrowseUrl = config.contextPath + '/engines/contentpicker.jsp?type=imagepicker&site=' + config.siteUuid + '&lang='+ config.contentlanguage + '&uilang='+ config.language;
    config.filebrowserFlashBrowseUrl = config.contextPath + '/engines/contentpicker.jsp?mime=application%2Fx-shockwave-flash%2Cvideo%2Fx-flv&site=' + config.siteUuid + '&lang='+ config.contentlanguage + '&uilang='+ config.language;
    config.filebrowserLinkBrowseUrl = config.contextPath + '/engines/contentpicker.jsp?type=editoriallinkpicker&site=' + config.siteUuid + '&lang='+ config.contentlanguage + '&uilang='+ config.language;
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
    config.toolbar_Mini = [
        ['Bold','Italic','Underline','Strike'],
        ['NumberedList','BulletedList','-','Blockquote'],
        ['Link','Unlink'],
        ['Image','Smiley'],
        ['TextColor','BGColor']
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