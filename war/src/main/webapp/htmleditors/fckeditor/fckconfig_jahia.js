FCKConfig.AutoDetectLanguage	= false ;

FCKConfig.ToolbarSets["Full"] = [
	['Source','DocProps','-',/*'Save',*/'NewPage','Preview','-','Templates'],
	['Cut','Copy','Paste','PasteText','PasteWord','-','Print','SpellCheck'],
	['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
	//['Form','Checkbox','Radio','TextField','Textarea','Select','Button','ImageButton','HiddenField'],
	'/',
	['Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'],
	['OrderedList','UnorderedList','-','Outdent','Indent','Blockquote','CreateDiv'],
	['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
	['Link','Unlink','Anchor'],
	['Image','Flash','Table','Rule','Smiley','SpecialChar'/*,'PageBreak'*/],
	'/',
	['Style','FontFormat','FontName','FontSize'],
	['TextColor','BGColor'],
	['FitWindow','ShowBlocks','-','About']
] ;

FCKConfig.ToolbarSets["Basic"] = [
	['Source','DocProps','-',/*'Save',*/'NewPage','Preview','-'/*,'Templates'*/],
	['Cut','Copy','Paste','PasteText','PasteWord'/*,'-','Print','SpellCheck'*/],
	['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
	//['Form','Checkbox','Radio','TextField','Textarea','Select','Button','ImageButton','HiddenField'],
	'/',
	['Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'],
	['OrderedList','UnorderedList'/*,'-','Outdent','Indent','Blockquote','CreateDiv'*/],
	['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
	['Link','Unlink','Anchor'],
	['Image','Flash','Table','Rule','Smiley','SpecialChar'/*,'PageBreak'*/,
	//['Style','FontFormat','FontName','FontSize'],
	//['TextColor','BGColor'],
	'-', 'FitWindow','ShowBlocks'/*,'-','About'*/]
] ;

FCKConfig.ToolbarSets["Light"] = [
	['Bold','Italic','Underline','StrikeThrough','-','OrderedList','UnorderedList'/*,'-','Link','Unlink','-','About'*/]
] ;

FCKConfig.CustomStyles = {};

FCKConfig.LinkBrowserWindowWidth	= 727 ;
FCKConfig.LinkBrowserWindowHeight	= 415 ;

FCKConfig.FileBrowserWindowWidth	= 700 ;
FCKConfig.FileBrowserWindowHeight	= 510 ;

FCKConfig.ImageBrowserWindowWidth  = 700 ;
FCKConfig.ImageBrowserWindowHeight = 510 ;

FCKConfig.FlashBrowserWindowWidth  = 700 ;
FCKConfig.FlashBrowserWindowHeight = 510 ;