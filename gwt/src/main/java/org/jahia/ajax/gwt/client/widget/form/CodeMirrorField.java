/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Element;

public class CodeMirrorField extends TextArea {

    private Object codeMirror;
    private String mode = "jsp";
    private boolean readOnly = false;

    public void setMode(String mode) {
        this.mode = mode;
        if (codeMirror != null) {
            updateMode(mode);
        }
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                codeMirror = initEditor(getInputEl().dom, mode, readOnly?"nocursor":"");
                updateSize();
            }
        });
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        updateSize();
    }

    private native Object initEditor(Element textArea, String mode, String readOnly)/*-{
        var myCodeMirror = $wnd.CodeMirror.fromTextArea(textArea, {mode:mode, lineNumbers:true, matchBrackets:true, readOnly:readOnly});
        return myCodeMirror;
    }-*/;

    @Override
    public String getRawValue() {
        if (codeMirror == null) {
            return super.getRawValue();
        }
        return getCodeMirrorValue();
    }

    @Override
    public boolean isDirty() {
        return isCodeMirrorDirty();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    private native String getCodeMirrorValue()/*-{
        var myCodeMirror = this.@org.jahia.ajax.gwt.client.widget.form.CodeMirrorField::codeMirror;
        return myCodeMirror.getValue();
    }-*/;

    public void updateSize() {
        if (codeMirror != null) {
            setCodeMirrorSize(getWidth()-2, getHeight()-2);
        }
    }

    private native void setCodeMirrorSize(int width, int height)/*-{
        var myCodeMirror = this.@org.jahia.ajax.gwt.client.widget.form.CodeMirrorField::codeMirror;
        return myCodeMirror.setSize(width, height);
    }-*/;

    public void insertProperty(String value) {
        if(codeMirror!=null && !"".equals(value)) {
            insertPropertyAtCursor(value);
        }
    }

    private native void insertPropertyAtCursor(String value)/*-{
        var myCodeMirror = this.@org.jahia.ajax.gwt.client.widget.form.CodeMirrorField::codeMirror;
        myCodeMirror.replaceSelection(value);
        myCodeMirror.setCursor(myCodeMirror.getCursor("end"));
        myCodeMirror.focus();

    }-*/;

    public native void indent() /*-{
        var myCodeMirror = this.@org.jahia.ajax.gwt.client.widget.form.CodeMirrorField::codeMirror;
        var last = myCodeMirror.lineCount();
        myCodeMirror.operation(function() {
            for (var i = 0; i < last; ++i) myCodeMirror.indentLine(i);
        });
    }-*/;

    private native boolean isCodeMirrorDirty()/*-{
        var myCodeMirror = this.@org.jahia.ajax.gwt.client.widget.form.CodeMirrorField::codeMirror;
        return myCodeMirror.isClean();
    }-*/;

    private native void updateMode(String newMode)/*-{
        var myCodeMirror = this.@org.jahia.ajax.gwt.client.widget.form.CodeMirrorField::codeMirror;
        myCodeMirror.setOption("mode", newMode);
    }-*/;

}
