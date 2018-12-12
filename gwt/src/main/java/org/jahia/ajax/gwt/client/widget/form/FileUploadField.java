/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.messages.Messages;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.google.gwt.dom.client.*;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;

/**
 * 
 * User: toto
 * Date: Nov 26, 2008
 * Time: 2:23:02 PM
 * 
 */
public class FileUploadField extends AdapterField {
    public FileUploadField(String name) {
        super(new Uploader(name));

        setName(name);
        ((Uploader)getWidget()).addListener( new Listener() {
            public void onChange(Event event) {
                setUploadedValue(((Uploader)getWidget()).getKey());
//                setRawValue();
                // set the value ...
            }
        });
    }

    @Override
    public void setValue(Object o) {
        ((Uploader)getWidget()).setValue((String)o,(String)o);
    }

    public void setUploadedValue(String o) {
        super.setValue(o);
    }

    public String getRawValue() {
        String v = rendered ? getInputEl().getValue() : "";
        if (v == null || v.equals(emptyText)) {
          return "";
        }
        return v;
  }

    static class Uploader extends HorizontalPanel {
        private String name;
        private String key;
        private FormPanel form;
        private Text status;
        private Button clear;
        private NamedFrame target;

        private List<Listener> listeners = new ArrayList<Listener>();

        public Uploader(String defname) {
            super();

            status = new Text();
            status.setStyleName("x-form-field");
            target = new NamedFrame("target"+defname) {
                public void onBrowserEvent(Event event) {
                    Log.debug("LOADED");
                    Log.error(((FrameElement)getElement().cast()).getContentDocument().getClass().toString());
                    Document document = ((FrameElement) getElement().cast()).getContentDocument();
                    Log.error(document.getClass().toString());
                    Element elem = document.getElementById("uploaded");
                    if (elem != null) {
                        setValue(elem.getAttribute("name"), elem.getAttribute("key"));
                    }
                    super.onBrowserEvent(event);    
                }
            };
            target.sinkEvents(Event.ONLOAD);
            target.setVisible(false);
            form = new FormPanel(target);
            String entryPoint = JahiaGWTParameters.getServiceEntryPoint();
            if (entryPoint == null) {
                entryPoint = "/gwt/";
            }
            form.setAction(entryPoint + "fileupload"); // should do
            form.setEncoding(FormPanel.ENCODING_MULTIPART);
            form.setMethod(FormPanel.METHOD_POST);

            clear = new Button(Messages.get("label.stop", "Stop"), new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    setValue(null,"clear");
                }

            });
            clear.addStyleName("button-stop");

            name = null;
            key = null;

            initForm();
            status.setText("");
            clear.setVisible(false);

            add(status);
            add(form);
            add(clear);
            add(target);
        }

        public void setValue(String name, String key) {
            this.name = name;
            this.key = key;
            if (name == null) {
                initForm();
                status.setText("");
                clear.setVisible(false);
            } else {
                status.setText(name);
                clear.setText(Messages.get("label.clear", "Clear"));
                clear.setVisible(true);
                form.setVisible(false);
            }
            for (Listener listener : listeners) {
                listener.onChange(null);
            }
        }

        public void addListener(Listener list) {
            listeners.add(list);
        }

        public String getName() {
            return name;
        }

        public String getKey() {
            return key;
        }


        private void initForm() {
            final FileUpload upload = new FileUpload() {
                public final void onBrowserEvent(Event event) {
                    status.setText(Messages.get("message.uploading", "Uploading..."));
                    form.submit();
                    form.setVisible(false);
                    clear.setText(Messages.get("label.stop", "Stop"));
                    clear.setVisible(true);
                    super.onBrowserEvent(event);
                }
            };
            upload.sinkEvents(Event.ONCHANGE | Event.ONKEYUP);
            upload.setName("asyncupload");
            form.clear();
            form.add(upload);
            form.setVisible(true);
        }

    }

    interface Listener {
        public void onChange(Event event);
    }

}
