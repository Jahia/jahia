/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.usergroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;

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
                    public void addGroups(List<GWTJahiaGroup> groups) {
                        for (final GWTJahiaGroup group : groups) {
                            add(fieldId, callback, "g", group.getGroupKey(),
                                    group.getGroupname());
                        }
                    }

                    public void addUsers(List<GWTJahiaUser> users) {
                        for (final GWTJahiaUser user : users) {
                            add(fieldId, callback, "u", user.getUserKey(), user
                                    .getUsername());
                        }
                    }
                }, getViewMode(), "currentSite", singleSelectionMode);
            }
        });
    }
}
