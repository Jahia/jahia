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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.content.ImageCrop;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:56:09 PM
 * 
 */
public class CropActionItem extends BaseActionItem {

    private List<Integer[]> predefinedSizes;

    public void onComponentSelection() {
        GWT.runAsync(new RunAsyncCallback()  {
            public void onFailure(Throwable reason) {
            }

            public void onSuccess() {
                new ImageCrop(linker, linker.getSelectionContext().getSingleSelection(), predefinedSizes).show();
            }
        });
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.getSingleSelection() != null && hasPermission(lh.getSelectionPermissions()) && lh.isParentWriteable() && lh.isFile() && lh.isImage());
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
