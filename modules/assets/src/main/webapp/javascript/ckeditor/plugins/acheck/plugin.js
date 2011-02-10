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