CKEDITOR.plugins.add('acheck',
{
    lang:['en'],
    init: function(editor)
    {
        var pluginName = 'acheck',
            commandName = 'acheck';
        editor.addCommand(commandName, 
            {
		        exec : function()
                {
                    var theCode = '<html><body onLoad="document.accessform.submit();"> \n';
                    theCode += '<h1>Submitting Code for Accessibility Checking.....</h1>\n';
                    theCode += '<form action="http://achecker.ca/checker/index.php" name="accessform" method="post"> \n';
                    theCode += '<input type="hidden" name="gid[]" value="8" /> \n';
                    theCode += '<textarea name="validate_content">' + editor.getData() + '</textarea>\n';
                    theCode += '<input type="submit" /></form> \n';  
                    theCode += '</body></html> \n';
                    accessWin = window.open('', 'accessWin',  '');
                    accessWin.document.writeln(theCode);
                    accessWin.document.close();
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