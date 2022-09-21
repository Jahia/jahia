/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.form.Field;

/**
 * Number field with multiple values
 *
 * Created by kevan inspired by {@link MultipleTextField}
 */
public class MultipleNumberField<T extends Number> extends AbstractMultipleField<T> {

    private boolean allowDecimals = true;
    private Class<?> type = null;

    public MultipleNumberField() {
        super();
    }

    @Override
    Field getNewField() {
        ItemField field = new ItemField();
        field.setAllowDecimals(allowDecimals);
        if(type != null) {
            field.setPropertyEditorType(type);
        }
        return field;
    }

    public boolean isAllowDecimals() {
        return allowDecimals;
    }

    public void setAllowDecimals(boolean allowDecimals) {
        this.allowDecimals = allowDecimals;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    /**
     * Field for one value
     */
    private class ItemField extends NumberTriggerField {

        ItemField() {
            setEditable(true);
            setTriggerStyle("x-form-clear-trigger");
        }

        @Override
        protected void onTriggerClick(ComponentEvent ce) {
            fields.remove(this);
            this.removeAllListeners();
            MultipleNumberField.this.fireEvent(Events.Change, ce);
            removeFromParent();
        }

        @Override
        public void setReadOnly(boolean readOnly) {
            super.setReadOnly(readOnly);
            setHideTrigger(readOnly);
        }
    }

}
