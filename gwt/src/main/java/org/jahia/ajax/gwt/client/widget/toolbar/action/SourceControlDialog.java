package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.HashMap;
import java.util.Map;

public class SourceControlDialog extends Dialog {

    private RadioGroup scmType;
    private TextField<String> uri;
    private TextField<String> moduleName;
    private TextField<String> branchOrTag;
    private Map<String,Radio> radios;

    public SourceControlDialog(boolean viewBranchOrTag) {
        setHeading("Get sources from source control");
        setButtons(Dialog.OKCANCEL);
        setModal(true);
        setHideOnButtonClick(true);
        setWidth(500);
        setHeight(200);

        setLayout(new FitLayout());

        final FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setFrame(false);
        form.setLabelWidth(125);

        scmType = new RadioGroup("scmType");
        scmType.setFieldLabel(Messages.get("label.scmType", "SCM type"));
        radios = new HashMap<String, Radio>();
        Radio git = new Radio();
        git.setBoxLabel(Messages.get("label.git", "GIT"));
        git.setValue(true);
        git.setValueAttribute("git");
        scmType.add(git);
        radios.put(git.getValueAttribute(), git);

        Radio svn = new Radio();
        svn.setBoxLabel(Messages.get("label.svn", "SVN"));
        svn.setValueAttribute("svn");
        scmType.add(svn);
        radios.put(svn.getValueAttribute(), svn);

        form.add(scmType);

        uri = new TextField<String>();
        uri.setName("uri");
        uri.setFieldLabel(Messages.get("label.uri", "URI"));
        form.add(uri);

        moduleName = new TextField<String>();
        moduleName.setName("moduleName");
        moduleName.setFieldLabel(Messages.get("label.moduleName", "Module name"));
        form.add(moduleName);

        if (viewBranchOrTag) {
            branchOrTag = new TextField<String>();
            branchOrTag.setName("branchOrTag");
            branchOrTag.setFieldLabel(Messages.get("label.branchOrTag", "Branch or tag"));
            form.add(branchOrTag);
        }

        add(form);
    }

    public String getScmType() {
        return scmType.getValue().getValueAttribute();
    }

    public void setScmType(String value) {
        scmType.setValue(radios.get(value));
    }


    public String getUri() {
        return uri.getValue();
    }

    public void setUri(String value) {
        this.uri.setValue(value);
    }

    public String getModuleName() {
        return moduleName.getValue();
    }

    public void setModuleName(String moduleName) {
        this.moduleName.setValue(moduleName);
    }

    public String getBranchOrTag() {
        return branchOrTag.getValue();
    }

    public void setBranchOrTag(String value) {
        branchOrTag.setValue(value);
    }

    public void addCallback(final Listener<WindowEvent> listener) {
        addListener(Events.Hide, new Listener<WindowEvent>() {
            @Override
            public void handleEvent(WindowEvent be) {
                if (be.getButtonClicked().getItemId().equalsIgnoreCase(Dialog.OK)) {
                    listener.handleEvent(be);
                }
            }
        });
    }
}
