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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.usergroup;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Renders a button for opening user/group picker.
 *
 * @author Sergiy Shyrkov
 */
public class UserGroupSelectButton extends InlineHTML {

    private static final Set<String> MODES = new HashSet<String>(3);

    static {
        MODES.add("both");
        MODES.add("users");
        MODES.add("groups");
    }

    public static native void add(String fieldId, String callback,
            String principalType, String principalKey, String principalName) /*-{
      var callbackResult = true;
      if (callback) {
          try {
              callbackResult = eval('$wnd.' + callback)(principalType, principalKey, principalName);
          } catch (e) {};
      }
      try {
          if (callbackResult && fieldId && $wnd.document.getElementById(fieldId)) {
              var target = $wnd.document.getElementById(fieldId);
              if (typeof target.options != 'undefined') {
                  var found = false;
                  for (var i=0; i < target.length; i++) {
                      if (target.options[i].value == principalKey) {
                          found = true;
                          break;
                      }
                  }
                  if (!found) {
                      target.options[target.length] = new Option(principalName, principalKey, true);
                  }
              } else {
                  target.value = principalName;
              }
          }
      } catch (e) { };
    }-*/;

    private static String getAttribute(Element elem, String attributeName,
            String defaultValue) {
        return elem.getAttribute(attributeName).length() > 0 ? elem
                .getAttribute(attributeName) : defaultValue;
    }

    private String callback;

    private String fieldId;

    private String mode;

    private boolean showSiteSelector;

    private boolean singleSelectionMode;

    /**
     * Initializes an instance of this class.
     *
     * @param elem
     */
    public UserGroupSelectButton(Element elem) {
        super(getAttribute(elem, "label", ""));
        mode = getAttribute(elem, "mode", "both");
        mode = MODES.contains(mode) ? mode : "both";
        setStyleName(getAttribute(elem, "styleName",
                "usergroup-button usergroup-mode-" + mode));
        showSiteSelector = Boolean.valueOf(getAttribute(elem,
                "showSiteSelector", "false"));
        singleSelectionMode = Boolean.valueOf(getAttribute(elem,
                "singleSelectionMode", "false"));

        callback = getAttribute(elem, "onSelect", null);
        fieldId = getAttribute(elem, "fieldId", null);

        init();
    }

    private int getViewMode() {
        int viewMode = UserGroupSelect.VIEW_TABS;
        if ("users".equals(mode)) {
            viewMode = UserGroupSelect.VIEW_USERS;
        } else if ("groups".equals(mode)) {
            viewMode = UserGroupSelect.VIEW_GROUPS;
        }
        return viewMode;
    }

    private void init() {
        addClickListener(new ClickListener() {
            public void onClick(Widget sender) {

                new UserGroupSelect(new UserGroupAdder() {
                    public void addUsersGroups(List<GWTJahiaNode> users) {
                        for (final GWTJahiaNode user : users) {
                            add(fieldId, callback, user.isNodeType("jnt:user") ? "u" : "g", user.getPath(), user
                                    .getDisplayName());
                        }
                    }
                }, getViewMode(), JahiaGWTParameters.getSiteKey(), singleSelectionMode);
            }
        });
    }
}
