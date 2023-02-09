/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.util.Point;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.event.DragListener;
import com.extjs.gxt.ui.client.event.DragEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.dnd.StatusProxy;
import com.extjs.gxt.ui.client.dnd.GridDragSource;
import com.google.gwt.core.client.GWT;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

/**
 *
 * User: toto
 * Date: Aug 24, 2009
 * Time: 11:12:43 AM
 *
 */
public class EditModeGridDragSource extends GridDragSource {
    public EditModeGridDragSource(Grid c) {
        super(c);
        DragListener listener = new DragListener() {
            public void dragEnd(DragEvent de) {
                DNDEvent e = new DNDEvent(EditModeGridDragSource.this, de.getEvent());
                e.setData(data);
                e.setDragEvent(de);
                e.setComponent(component);
                e.setStatus(statusProxy);

                onDragEnd(e);
            }

            @Override
            public void dragMove(DragEvent de) {
                GWT.log("drag to  : " + de.getX());
                if (MainModule.getInstance().isInframe()) {
                    Point position = MainModule.getInstance().getContainer().getPosition(false);
                    de.setX(de.getX() + position.x);
                    de.setY(de.getY() + position.y);
                    GWT.log("drag changed to  : " + de.getX());
                }
            }

        };
        draggable.addDragListener(listener);

    }

    @Override
    protected void onDragCancelled(DNDEvent dndEvent) {
        super.onDragCancelled(dndEvent);
        onDragEnd(dndEvent);
    }

    protected void onDragEnd(DNDEvent e) {
        StatusProxy sp = e.getStatus();
        sp.setData(EditModeDNDListener.SOURCE_TYPE, null);
        sp.setData(EditModeDNDListener.CONTENT_SOURCE_TYPE, null);
        sp.setData(EditModeDNDListener.TARGET_TYPE, null);
        sp.setData(EditModeDNDListener.TARGET_NODE, null);
        sp.setData(EditModeDNDListener.TARGET_PATH, null);
        sp.setData(EditModeDNDListener.SOURCE_NODES, null);
        sp.setData(EditModeDNDListener.SOURCE_QUERY, null);
        sp.setData(EditModeDNDListener.SOURCE_TEMPLATE, null);
        sp.setData(EditModeDNDListener.SOURCE_NODETYPE, null);
        sp.setData(EditModeDNDListener.OPERATION_CALLED, null);
        e.setData(null);
    }
}
