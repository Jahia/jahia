CKEDITOR.plugins.add('acheck',
{
    lang:['en', 'de', 'fr'],
    init: function(editor)
    {
        var pluginName = 'acheck',
            commandName = 'acheck';
        editor.addCommand(commandName, 
            {
	        exec : function()
            {
			var form = document.createElement("form");
			form.setAttribute("id", "acheckForm");
			form.setAttribute("name", "acheckForm");
			form.setAttribute("method", "POST");
			form.setAttribute("action", "http://achecker.ca/checker/index.php");
//			form.setAttribute("action", editor.config.contextPath + "/AChecker/checker/index.php");			
			form.setAttribute("target", "ACheckAccessWin");

            var inputField = document.createElement("input");              
            inputField.setAttribute("name", "validate_content");
            inputField.setAttribute("value", editor.getData());
            form.appendChild(inputField);
            var hiddenField = document.createElement("input");              
			hiddenField.setAttribute("type", "hidden");
            hiddenField.setAttribute("name", "gid[]");
            hiddenField.setAttribute("value", "8");
            form.appendChild(hiddenField);				
            var submitButton = document.createElement("input");              				
			submitButton.setAttribute("type", "submit");
			submitButton.setAttribute("name", "acheckFormSubmit");
			form.appendChild(submitButton);
			
            document.body.appendChild(form);
            window.open('', 'ACheckAccessWin', 'scrollbars=yes,menubar=no,height=800,width=1400,resizable=yes,toolbar=no,status=no');

            $('#acheckForm').submit();
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