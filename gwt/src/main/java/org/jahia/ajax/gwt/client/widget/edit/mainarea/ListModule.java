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
package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.widget.Header;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 *
 * User: toto
 * Date: Aug 18, 2009
 * Time: 7:25:48 PM
 *
 */
public class ListModule extends SimpleModule {

    public ListModule(String id, String path, Element divElement, MainModule mainModule) {
        super(id, path, divElement, mainModule);
        head = new Header();

        if (editable) {
            add(head);
        }

        if (path.contains("/")) {
            head.setText(Messages.get("label.list") + " : " + path.substring(path.lastIndexOf('/') + 1));
        } else {
            head.setText(Messages.get("label.list") + " : " + path);
        }
        setBorders(false);
//        setBodyBorder(false);
        head.addStyleName("x-panel-header");
        head.addStyleName("x-panel-header-listmodule");
        html = new HTML(divElement.getInnerHTML());
        add(html);
    }

    @Override
    public void onParsed() {
        super.onParsed();
        addStyleName(mainModule.getConfig().getName()+"List");
    }
}
