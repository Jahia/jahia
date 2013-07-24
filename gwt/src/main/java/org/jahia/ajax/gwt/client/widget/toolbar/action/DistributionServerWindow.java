package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.messages.Messages;

public abstract class DistributionServerWindow extends Window {

    private String id;
    private String url;

    protected DistributionServerWindow() {
    }

    protected DistributionServerWindow(String id, String url) {
        this.id = id;
        this.url = url;
    }

    protected abstract void callback(String id, String url);

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);

        setLayout(new FitLayout());
        setHeading(Messages.get("label.releaseModule.distributionServer", "Distribution server (Maven)"));
        setModal(true);
        setWidth(500);
        setHeight(270);

        VerticalPanel p = new VerticalPanel();
        p.add(new Label(Messages.get("label.releaseModule.distributionServer.notProvided",
                "No target distribution server configured for this module yet.")));
        p.add(new HTML("<br/>"));
        p.add(new Label(Messages.get("label.releaseModule.distributionServer.purpose",
                "A target distribution server is a Maven repository,"
                        + " where built module artifacts (module JAR file)"
                        + " are pushed to during module release process.")));
        p.add(new Label(Messages.get("label.releaseModule.distributionServer.authentication",
                "If your distribution server requires authentication, please, provide the corresponding"
                        + " <server/> section in your Maven's settings.xml file.")));
        p.add(new HTML("<br/>"));
        p.add(new Label(Messages.get("label.releaseModule.distributionServer.provideNow",
                "Would you like to configure the distribution server now?")));

        final FormPanel formPanel = new FormPanel();
        formPanel.setHeaderVisible(false);
        formPanel.setLabelWidth(50);
        formPanel.setFieldWidth(380);
        formPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        formPanel.setBorders(false);

        final TextField<String> tfRepoId = new TextField<String>();
        tfRepoId.setFieldLabel(Messages.get("label.id", "ID"));
        tfRepoId.setAllowBlank(false);
        if (id != null) {
            tfRepoId.setValue(id);
        }
        formPanel.add(tfRepoId);

        final TextField<String> tfRepoUrl = new TextField<String>();
        tfRepoUrl.setFieldLabel(Messages.get("label.url", "URL"));
        tfRepoUrl.setAllowBlank(false);
        if (url != null) {
            tfRepoUrl.setValue(url);
        }
        formPanel.add(tfRepoUrl);

        final Window w = this;
        formPanel.addButton(new Button(Messages.get("label.save", "Save"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                w.hide();
                callback(tfRepoId.getValue(), tfRepoUrl.getValue());
            }
        }));
        formPanel.addButton(new Button(Messages.get("label.skip", "Skip"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                w.hide();
                callback(null, null);
            }
        }));

        p.add(formPanel);

        add(p, new MarginData(5));
    }
}
