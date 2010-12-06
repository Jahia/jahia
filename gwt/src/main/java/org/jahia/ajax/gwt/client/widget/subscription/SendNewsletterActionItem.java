package org.jahia.ajax.gwt.client.widget.subscription;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.http.client.*;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;
import org.jahia.ajax.gwt.client.widget.toolbar.action.BaseActionItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SendNewsletterActionItem extends BaseActionItem {
    protected List<GWTJahiaLanguage> gwtJahiaLanguages;
    protected GWTJahiaLanguage selectedLang;

    public void setLanguages(List<GWTJahiaLanguage> gwtJahiaLanguages) {
        this.gwtJahiaLanguages = gwtJahiaLanguages;
    }

    public void setSelectedLang(GWTJahiaLanguage selectedLang) {
        this.selectedLang = selectedLang;
    }

    @Override public void onComponentSelection() {
        new SendMailWindow(linker, linker.getSelectionContext().getSingleSelection()).show();
    }

    @Override public void handleNewLinkerSelection() {
        final GWTJahiaNode n = linker.getSelectionContext().getSingleSelection();
        setEnabled(n != null && n.getNodeTypes().contains("jnt:newsletterIssue") &&
                (n.getAggregatedPublicationInfo().getStatus() != GWTJahiaPublicationInfo.NOT_PUBLISHED));
    }

    class SendMailWindow extends Window {

        private Linker m_linker;

        public SendMailWindow(final Linker linker, final GWTJahiaNode n) {
            super();

            m_linker = linker;

            setHeading(Messages.get("label.sendNewsletter"));
            setSize(500, 120);
            setResizable(false);
            setModal(true);
            ButtonBar buttons = new ButtonBar();

            final FormPanel form = new FormPanel();
            form.setFrame(false);
            form.setHeaderVisible(false);
            form.setBorders(false);
            form.setBodyBorder(false);
            form.setLabelWidth(200);

            final CalendarField date = new CalendarField();
            date.setFieldLabel(Messages.get("label.scheduled", "Scheduled"));
            form.add(date);

            Button schedule = new Button(Messages.get("label.scheduleAsBackgroundJob"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    mask();
                    doSchedule(date.getValue(), n);
                }
            });

            Button now = new Button(Messages.get("label.sendNow"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    mask();
                    doSend(n);
                }
            });

            Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    hide();
                }
            });

            buttons.add(schedule);
            buttons.add(now);
            buttons.add(cancel);

            setButtonAlign(Style.HorizontalAlignment.CENTER);
            setBottomComponent(buttons);
            add(form);
        }

        private void doSend(GWTJahiaNode gwtJahiaNode) {
            Log.debug("action");
            String baseURL = org.jahia.ajax.gwt.client.util.URL.getAbsoluteURL(
                    JahiaGWTParameters.getContextPath() + "/cms/render");
            String localURL = baseURL + "/default/" + JahiaGWTParameters.getLanguage() + gwtJahiaNode.getPath();
            linker.loading("Executing action ...");
            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, localURL + ".sendAsNewsletter.do");
            try {
                builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
                Request response = builder.sendRequest(null, new RequestCallback() {
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
                        linker.refresh(EditLinker.REFRESH_MAIN);
                    }
                });


            } catch (RequestException e) {
                // Code omitted for clarity
            }

        }

        private void doSchedule(Date date, GWTJahiaNode gwtJahiaNode) {
            List<GWTJahiaNodeProperty> props = new ArrayList<GWTJahiaNodeProperty>();
            String propValueString = String.valueOf(date.getTime());
            props.add(new GWTJahiaNodeProperty("j:scheduled", new GWTJahiaNodePropertyValue(propValueString, GWTJahiaNodePropertyType.DATE)));

            List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
            nodes.add(gwtJahiaNode);
            JahiaContentManagementService.App.getInstance().saveProperties(nodes, props, new BaseAsyncCallback() {
                public void onSuccess(Object result) {
                    linker.loaded();
                    unmask();
                    hide();
                    linker.refresh(EditLinker.REFRESH_MAIN);
                }

                @Override public void onApplicationFailure(Throwable caught) {
                    super.onApplicationFailure(caught);
                    unmask();
                    hide();
                }
            });
        }
    }



}
