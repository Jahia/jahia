package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.ArrayList;
import java.util.List;

public class MultipleTextField<T> extends MultiField<List<T>> {

    public MultipleTextField() {
        super();
        setOrientation(Style.Orientation.VERTICAL);
    }
    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        Button add = new Button(Messages.get("label.add"));
        lc.add(add);
        add.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                addField(null);
            }
        });

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                lc.setWidth(getWidth());
                for (Field<?> field : fields) {
                    field.setWidth(getWidth());
                }
                lc.layout();
            }
        });
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);    //To change body of overridden methods use File | Settings | File Templates.
    }

    private void addField(T value) {
        ItemField field = new ItemField();
        field.setWidth(getWidth());
        if (value != null) {
            field.setValue(value);
        }
        fields.add(field);

        if (afterRender) {
            TableData data = (TableData) ComponentHelper.getLayoutData(field);
            if (data == null) {
                data = new TableData();
            }
            String style = "position: static;";

            if (spacing > 0) {
                style += "paddingTop:" + spacing + "px;";
            }

            data.setStyle(style);
            lc.insert(field, fields.size()-1, data);
            lc.layout();
        }
    }

    @Override
    public List<T> getValue() {
        List<T> values = new ArrayList<T>();
        for (Field<?> field : getAll()) {
            values.add(((Field<T>)field).getValue());
        }
        return values;
    }

    @Override
    public void setValue(List<T> value) {
        super.setValue(value);
        for (Field<?> field : fields) {
            lc.remove(field);
        }
        fields.clear();
        for (T t : value) {
            addField(t);
        }
    }

    @Override
    public void setRawValue(String value) {
        //
    }

    class ItemField extends TriggerField<T> {

        ItemField() {
            setEditable(true);
            setTriggerStyle("x-form-clear-trigger");
        }

        @Override
        protected void onTriggerClick(ComponentEvent ce) {
            fields.remove(this);
            removeFromParent();
        }
    }

}
