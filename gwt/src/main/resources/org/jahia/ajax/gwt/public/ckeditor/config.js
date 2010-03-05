/*
Copyright (c) 2003-2009, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
*/

CKEDITOR.editorConfig = function( config )
{
    config.contextPath = (typeof jahiaGWTParameters != 'undefined') ? jahiaGWTParameters.contextPath : '';
    config.language = (typeof jahiaGWTParameters != 'undefined') ? jahiaGWTParameters.lang : 'en';
	config.skin = 'jahia';
    config.toolbar = 'Engine';
    config.toolbar_Engine =
        [
            ['Source','-','Preview','-','Templates'],
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
    config.toolbar_User =
        [
         	['Cut','Copy','Paste','PasteText','PasteFromWord','-', 'SpellChecker', 'Scayt'],
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
    config.filebrowserWindowWidth = '810';
    config.filebrowserWindowHeight = '650';
    config.filebrowserLinkWindowHeight = '800';            
    config.filebrowserBrowseUrl = config.contextPath + '/engines/gwtcontentpicker/contentpicker.jsp';
    config.filebrowserImageBrowseUrl = config.contextPath + '/engines/gwtcontentpicker/contentpicker.jsp?mime=image%2F*';
    config.filebrowserFlashBrowseUrl = config.contextPath + '/engines/gwtcontentpicker/contentpicker.jsp?mime=application%2Fx-shockwave-flash%2Cvideo%2Fx-flv';
    config.filebrowserLinkBrowseUrl = config.contextPath + '/engines/gwtcontentpicker/contentpicker.jsp?type=linkpicker';
};
