/*
 This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 For licensing, see LICENSE.jahia.txt
 */

CKEDITOR.plugins.add('acheck',
{
    lang:['en', 'de', 'fr'],
    init: function(editor)
    {
        var pluginName = 'acheck',
            commandName = 'acheck';
        editor.addCommand(commandName, 
            {
        		modes : { wysiwyg : 1, source : 1 },
		        exec : function(theEditor)
	            {
					if (typeof theEditor.checkWCAGCompliance == 'function') {
						theEditor.checkWCAGCompliance(theEditor.name, document.getElementById(theEditor.name), true);
					}
	            },
	            canUndo : false
        });
        
        editor.ui.addButton('ACheck',
            {
        	label:editor.lang.acheck.title,
        	command:commandName,
        	icon:this.path+'acheck.gif'
            });
    }
});