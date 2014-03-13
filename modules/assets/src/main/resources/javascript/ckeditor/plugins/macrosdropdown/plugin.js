/**
 * Check macro plugin.
 * User: damien
 * Date: 11/15/13
 * Time: 3:27 PM
 */
CKEDITOR.plugins.add('macrosdropdown', {
            requires: ['richcombo'], //, 'styles' ],
            init: function (editor) {
                var tags = [];
                var path = contextJsParameters.siteUuid;
                // test if Macros is defined in Toolbar
                var isMacrosDefined = false;
                $(eval("editor.config.toolbar_"+editor.config.toolbar)).each(function (){
                    if(!isMacrosDefined && $.inArray('Macros',this)>-1) {
                        isMacrosDefined = true;
                    }
                });
                if (isMacrosDefined) {
                    var domainURL = window.location.protocol + "//" + window.location.host +
                                    (jahiaGWTParameters != undefined ? jahiaGWTParameters.contextPath : "");
                    var initializerURL = domainURL+ '/cms/initializers';
                    $.get(initializerURL,
                            {name: 'macros', nodeuuid: path, initializers: 'choicelistmacros'}, function (result) {
                                $.each(result, function (key, value) {
                                    tags.push([value['name'][0], value['name'][0], value['name'][0]]);
                                });
                            }, 'json');
                }

                editor.ui.addRichCombo('Macros', {
                            label: 'Macros',
                            title: 'Insert macros',
                            voiceLabel: 'Insert macros',
                            className: 'cke_format',
                            multiSelect: false,
                            panel: {
                                css: [ editor.config.contentsCss, CKEDITOR.skin.getPath('editor') ],
                                voiceLabel: editor.lang.panelVoiceLabel
                            },

                            init: function () {
                                this.startGroup("Insert Macros");
                                //this.add('value', 'drop_text', 'drop_label');
                                for (var this_tag in tags) {
                                    this.add(tags[this_tag][0], tags[this_tag][1], tags[this_tag][2]);
                                }
                            },

                            onClick: function (value) {
                                editor.focus();
                                editor.fire('saveSnapshot');
                                editor.insertHtml(value);
                                editor.fire('saveSnapshot');
                            }
                        });
            }
        });