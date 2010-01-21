package org.jahia.ajax.gwt.client.widget.edit.contentengine;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 6:31:49 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class EditEngineTabItem extends AsyncTabItem {
    protected AbstractContentEngine engine;

    protected EditEngineTabItem(AbstractContentEngine engine) {
        this.engine = engine;
        setLayout(new FitLayout());
        setStyleName("x-panel-mc");
    }

    protected EditEngineTabItem(String title, AbstractContentEngine engine) {
        super(title);
        this.engine = engine;
        setLayout(new FitLayout());
        setStyleName("x-panel-mc");
    }

    /**
     * Create the tab item
     */
    public abstract void create(GWTJahiaLanguage locale);

    
}
