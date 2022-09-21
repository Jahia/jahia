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

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.tripanel.MyStatusBar;

import java.util.List;

/**
 * User: rfelden
 * Date: 16 janv. 2009 - 15:54:26
 */
public class FilterStatusBar extends MyStatusBar {

    public FilterStatusBar(List<String> filters, List<String> mimeTypes, List<String> nodeTypes) {
        // display filters to inform user
        StringBuilder disp = new StringBuilder() ;
        if (filters != null && filters.size()>0) {
            disp.append(Messages.get("filters.label")).append(" : ").append(filters) ;
        }
        if (mimeTypes != null && mimeTypes.size()>0) {
            if (disp.length() > 0) {
                disp.append(" - ") ;
            }
            disp.append(Messages.get("mimes.label")).append(" : ").append(mimeTypes) ;
        }
        if (nodeTypes != null && nodeTypes.size()>0) {
            if (disp.length() > 0) {
                disp.append(" - ") ;
            }
            disp.append(Messages.get("nodes.label")).append(" : ").append(nodeTypes) ;
        }
        setMessage(disp.toString());
    }

}
