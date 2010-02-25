

CKEDITOR.plugins.add( 'linkfile',
{
	init : function( editor )
	{
        // Add the link and unlink buttons.
		editor.addCommand( 'linkfile', new CKEDITOR.dialogCommand( 'linkfile' ) );
		editor.ui.addButton( 'LinkFile',
			{
				label : editor.lang.link.toolbar,
				icon : this.path + '/images/file.png',
				command : 'linkfile'
			} );

		CKEDITOR.dialog.add( 'linkfile', this.path + 'dialogs/linkfile.js' );


		// If the "menu" plugin is loaded, register the menu items.
		if ( editor.addMenuItems )
		{
			editor.addMenuItems(
				{
					linkfile :
					{
						label : editor.lang.link.menu,
						command : 'linkfile',
						group : 'linkfile',
						order : 1
					}
				});
		}

		// If the "contextmenu" plugin is loaded, register the listeners.
		if ( editor.contextMenu )
		{
			editor.contextMenu.addListener( function( element, selection )
				{
					if ( !element )
						return null;

					var isAnchor = ( element.is( 'img' ) && element.getAttribute( '_cke_real_element_type' ) == 'anchor' );

					if ( !isAnchor )
					{
						if ( !( element = element.getAscendant( 'a', true ) ) )
							return null;

						isAnchor = ( element.getAttribute( 'name' ) && !element.getAttribute( 'href' ) );
					}

					return isAnchor ?
							{ anchor : CKEDITOR.TRISTATE_OFF } :
							{ link : CKEDITOR.TRISTATE_OFF, unlink : CKEDITOR.TRISTATE_OFF };
				});
		}
	},

	afterInit : function( editor )
	{
		// Register a filter to displaying placeholders after mode change.

		var dataProcessor = editor.dataProcessor,
			dataFilter = dataProcessor && dataProcessor.dataFilter;

		if ( dataFilter )
		{
			dataFilter.addRules(
				{
					elements :
					{
						a : function( element )
						{
							var attributes = element.attributes;
							if ( attributes.name && !attributes.href )
								return editor.createFakeParserElement( element, 'cke_anchor', 'anchor' );
						}
					}
				});
		}
	},

	requires : [ 'fakeobjects' ]
} );


CKEDITOR.tools.extend( CKEDITOR.config,
{
	linkfileShowAdvancedTab : true,
	linkfileShowTargetTab : true
} );
