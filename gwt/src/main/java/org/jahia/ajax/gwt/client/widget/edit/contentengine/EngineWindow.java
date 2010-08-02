package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 2, 2010
 * Time: 3:55:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class EngineWindow extends Window implements EngineContainer {
    private AbstractContentEngine engine;

    public EngineWindow() {
        setSize(750, 480);
        setBodyBorder(false);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setIcon(StandardIconsProvider.STANDARD_ICONS.engineLogoJahia());
        setLayout(new FitLayout());
    }

    public ContentPanel getPanel() {
        return this;
    }

    public void setEngine(AbstractContentEngine engine) {
        add(engine);
        this.engine = engine;
    }

    public void showEngine() {
        show();
    }

    public void closeEngine() {
        hide();
    }
}
