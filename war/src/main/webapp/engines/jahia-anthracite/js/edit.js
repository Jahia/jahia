(function (exposeAs) {
	var app = DX_app;
	// INITIALISE
	/**
	 *
	 */
	var init = function () {
		// Get UI Language from GWT parameters
		app.data.UILanguage = jahiaGWTParameters.uilang.toUpperCase();

		// use Dex to cache an Dex Object
		DexV2('body').cache('body');

		// Register CK Editor version ( needed by CSS )
		DexV2.getCached('body').setAttribute('data-CKEDITOR-VERSION', app.data.ckeditorVersion);

		// This is a content picker, not main app.
		if (app.data.HTTP.app == 'contentpicker') {
			// This is a full page picker, not edit engine
			app.picker.data.standalone = true;
			app.picker.data.ID = app.picker.data.standaloneID;

			// Need to "open" the picker manually ...
			DexV2.tag('body').onOpen('#JahiaGxtContentPicker', function () {
				app.picker.onOpen();
			});

			DexV2.tag('body').onOpen('#JahiaGxtContentPickerWindow', function () {
				app.picker.onOpenSubPicker();
			});

			DexV2.getCached('body').setAttribute('data-indigo-is-manager', 'true');
		}

		// This is a manager, not main app.
		if (app.data.HTTP.app == 'manager') {
			// This is a manager, not edit engine
			app.picker.data.standalone = true;
			app.picker.data.ID = app.picker.data.standaloneManagerID;

			// Need to "open" the picker manually ...
			DexV2.tag('body').onOpen('#contentmanager > .x-viewport', function () {
				app.picker.onOpen();

				if (app.data.HTTP.picker == 'portletmanager-anthracite') {
					// Select list view
					DexV2('#contentmanager #JahiaGxtManagerToolbar .action-bar-tool-item.toolbar-item-listview').trigger('click');
				}
			});

			DexV2.tag('body').onOpen('#JahiaGxtContentPickerWindow', function () {
				app.picker.onOpenSubPicker();
			});

			DexV2.tag('body').onClose('#JahiaGxtContentPickerWindow', function () {
				app.picker.onCloseSubPicker();
			});

			DexV2.getCached('body').setAttribute('data-indigo-is-manager', 'true');
		}

		// Attach event listeners
		DX_eventListeners.attach();
	};

	// Start when DOM is ready
	document.addEventListener('DOMContentLoaded', function () {
		init();
	});

	// Expose DX to window
	if (exposeAs) {
		window[exposeAs] = app;
	}

})('DX');
