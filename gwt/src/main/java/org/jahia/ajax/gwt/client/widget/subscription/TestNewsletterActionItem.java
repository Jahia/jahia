package org.jahia.ajax.gwt.client.widget.subscription;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.http.client.*;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.BaseActionItem;

import java.util.List;

public class TestNewsletterActionItem extends BaseActionItem {
    protected List<GWTJahiaLanguage> gwtJahiaLanguages;
    protected GWTJahiaLanguage selectedLang;

    public void setLanguages(List<GWTJahiaLanguage> gwtJahiaLanguages) {
        this.gwtJahiaLanguages = gwtJahiaLanguages;
    }

    public void setSelectedLang(GWTJahiaLanguage selectedLang) {
        this.selectedLang = selectedLang;
    }

    @Override public void onComponentSelection() {
        new EmailTestWindow(linker, linker.getSelectionContext().getSingleSelection()).show();
    }

    @Override public void handleNewLinkerSelection() {
        setEnabled(linker.getSelectionContext().getSingleSelection() != null &&
                linker.getSelectionContext().getSingleSelection().getNodeTypes().contains("jnt:newsletterIssue"));
    }

    class EmailTestWindow extends Window {

        private Linker m_linker;

        public EmailTestWindow(final Linker linker, final GWTJahiaNode n) {
            super();
            setLayout(new FitLayout());

            m_linker = linker;

            setHeading(Messages.get("label.testNewsletter"));
            setSize(500, 170);
            setResizable(false);
            setModal(true);
            ButtonBar buttons = new ButtonBar();

            final FormPanel form = new FormPanel();
            form.setFrame(false);
            form.setHeaderVisible(false);
            form.setBorders(false);
            form.setBodyBorder(false);
            form.setLabelWidth(200);

            final TextField<String> mail = new TextField<String>();
            mail.setFieldLabel(Messages.get("label.email", "Email"));
            mail.setName("testemail");
            form.add(mail);

            final TextField<String> user = new TextField<String>();
            user.setFieldLabel(Messages.get("label.user", "User"));
            user.setName("user");
            user.setValue("guest");
            form.add(user);

            final ComboBox<GWTJahiaLanguage> locale = new ComboBox<GWTJahiaLanguage>();
            locale.setStore(new ListStore<GWTJahiaLanguage>());
            locale.getStore().add(gwtJahiaLanguages);
            locale.setDisplayField("displayName");
            locale.setTypeAhead(true);
            locale.setTriggerAction(ComboBox.TriggerAction.ALL);
            locale.setForceSelection(true);
            locale.setEditable(false);
            locale.setFieldLabel(Messages.get("label.locale", "Locale"));
            locale.setValue(selectedLang);
            locale.setName("locale");
            form.add(locale);

            Button submit = new Button(Messages.get("label.ok"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    mask();
                    String data = "testemail=" + URL.encodeComponent(mail.getValue());
                    data += "&type=html&user=" + URL.encodeComponent(user.getValue());
                    data += "&locale=" + URL.encodeComponent(locale.getValue().getLanguage());
                    doAction(linker.getSelectionContext().getSingleSelection(), data);
                }
            });

            Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    hide();
                }
            });

            buttons.add(submit);
            buttons.add(cancel);
            setButtonAlign(Style.HorizontalAlignment.CENTER);
            setBottomComponent(buttons);
            add(form);
        }

        public void doAction(GWTJahiaNode gwtJahiaNode, final String requestData) {
            Log.debug("action");
            String baseURL = org.jahia.ajax.gwt.client.util.URL.getAbsoluteURL(
                                JahiaGWTParameters.getContextPath() + "/cms/render");
            String localURL = baseURL + "/default/" + JahiaGWTParameters.getLanguage() + gwtJahiaNode.getPath();
            linker.loading("Executing action ...");
            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, localURL + ".sendAsNewsletter.do");
            try {
                builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
                Request response = builder.sendRequest(requestData, new RequestCallback() {
                    public void onError(Request request, Throwable exception) {
                        com.google.gwt.user.client.Window.alert("Cannot create connection");
                        linker.loaded();
                        unmask();
                        hide();
                    }

                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() != 200) {
                            com.google.gwt.user.client.Window.alert("Cannot contact remote server : error "+response.getStatusCode());
                        }
                        linker.loaded();
                        unmask();
                        hide();
                    }
                });


            } catch (RequestException e) {
                // Code omitted for clarity
            }

        }


    }
}
