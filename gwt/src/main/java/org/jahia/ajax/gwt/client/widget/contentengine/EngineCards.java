package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 16, 2010
 * Time: 2:11:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class EngineCards extends LayoutContainer implements EngineContainer {
    private Linker linker;
    private EngineContainer mainContainer;

    private List<Component> components = new ArrayList<Component>();
    private List<String> headers = new ArrayList<String>();
    private List<ButtonBar> bars = new ArrayList<ButtonBar>();
    private ButtonBar bar;
    private int i = 0;

    public EngineCards(EngineContainer mainContainer, Linker linker) {
        super(new CardLayout());
        this.linker = linker;
        this.mainContainer = mainContainer;
        this.bar = new ButtonBar();
        this.bar.setAlignment(Style.HorizontalAlignment.CENTER);
        mainContainer.setEngine(this, "", bar, linker);
    }

    public ContentPanel getPanel() {
        return mainContainer.getPanel();
    }

    public void setEngine(Component component, String header, ButtonBar buttonsBar, Linker linker) {
        components.add(component);
        bars.add(buttonsBar);
        headers.add(header);
        add(component);
    }

    public void showEngine() {
        updateView();
        mainContainer.showEngine();
    }

    public void closeEngine() {
        if (++i < components.size()) {
            updateView();
        } else {
            mainContainer.closeEngine();
        }
    }

    private void updateView() {
        ((CardLayout)getLayout()).setActiveItem(components.get(i));
        mainContainer.getPanel().setHeading(headers.get(i));
        bar.removeAll();
        List<Component> items = new ArrayList<Component>(bars.get(i).getItems());
        for (Component component : items) {
            bar.add(component);
        }
    }


}
