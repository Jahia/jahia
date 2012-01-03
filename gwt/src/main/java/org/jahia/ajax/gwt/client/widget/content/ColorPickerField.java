package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ColorPalette;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.menu.ColorMenu;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 1/3/12
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColorPickerField extends AdapterField {

    private HorizontalPanel panel = new HorizontalPanel();
    private final TextBox text = new TextBox();

    
    /**
     * Creates a new adapter field.
     */

    public ColorPickerField() {
        super(null);
        text.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> stringValueChangeEvent) {
                setValue(stringValueChangeEvent.getValue());
            }
        });
        Button colorButton = new Button("");
        ColorMenu colorMenu = new ColorMenu();
        colorMenu.getColorPalette().addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                String v = ((ColorPalette) be.getComponent()).getValue();
                text.setText(v);
                setValue(v);
            }
        });
        colorButton.setMenu(colorMenu);
        colorButton.setStyleAttribute("background-image", "none");
        panel.add(text);
        panel.add(colorButton);
        widget = panel;
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);
        if (value != null) {
            text.setValue(value.toString());
        }
    }
}
