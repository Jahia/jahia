package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.ModalPanel;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 2, 2010
 * Time: 4:04:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnginePanel extends ContentPanel implements EngineContainer {
    private AbstractContentEngine engine;
    private ModalPanel modalPanel;

    public EnginePanel() {
        setBodyBorder(false);
        setLayout(new FitLayout());
    }

    public ContentPanel getPanel() {
        return this;
    }

    public void setEngine(AbstractContentEngine engine) {
        this.engine = engine;
        add(engine);
    }

    public void showEngine() {

        ((EditLinker)engine.getLinker()).replaceMainAreaComponent(this);

    }

    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);

        setZIndex(1040);
        modalPanel = ModalPanel.pop();
        modalPanel.setBlink(false);
        modalPanel.show(this);

    }

    public void closeEngine() {
        ModalPanel.push(modalPanel);
        modalPanel = null;

        ((EditLinker)engine.getLinker()).restoreMainArea();
    }

}
