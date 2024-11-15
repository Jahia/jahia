/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.content.ImageResize;

import java.util.ArrayList;
import java.util.List;

/**
 * Item for "resize image" action.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:57:55 PM
 */
public class ResizeActionItem extends BaseActionItem   {

    private List<Integer[]> predefinedSizes;

    public void onComponentSelection() {
        GWT.runAsync(new RunAsyncCallback()  {
            public void onFailure(Throwable reason) {
            }

            public void onSuccess() {
                new ImageResize(linker, linker.getSelectionContext().getSingleSelection(), predefinedSizes).show();
            }
        });
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();

        setEnabled(lh.getSingleSelection() != null && lh.isParentWriteable() && lh.isFile() && lh.isImage()
                && hasPermission(lh.getSingleSelection())
                && lh.getSingleSelection().get("j:height") != null
                && lh.getSingleSelection().get("j:width") != null);
    }

    /**
     * Defines the list of predefined image sizes.
     * Each value is a String of format [width]x[height]
     * @param predefinedSizes list of predefined sizes
     */
    public void setPredefinedSizes(List<String> predefinedSizes) {
        if (predefinedSizes == null || predefinedSizes.isEmpty()) {
            this.predefinedSizes = null;
        } else {
            this.predefinedSizes = new ArrayList<Integer[]>(predefinedSizes.size());
            for (String size : predefinedSizes) {
                if (size.indexOf('x') != -1) {
                    String[] dimensions = size.split("x");
                    if (dimensions.length == 2) {
                        try {
                            this.predefinedSizes.add(new Integer[] { Integer.parseInt(dimensions[0]),
                                    Integer.parseInt(dimensions[1]) });
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }


}
