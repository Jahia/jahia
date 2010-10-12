/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:30:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class RightsTabItem extends EditEngineTabItem {
    private AclEditor rightsEditor;

    public RightsTabItem(NodeHolder engine) {
        super(Messages.get("label.engineTab.rights", "Rights"), engine);
    }

    @Override
    public void create(String locale) {
        if (engine.getAcl() != null) {
            setProcessed(true);

            GWTJahiaNode node;
            if (engine.getNode() != null) {
                node = engine.getNode();
            } else {
                node = engine.getTargetNode();
            }

            rightsEditor = new AclEditor(engine.getAcl(), node.getAclContext());
            rightsEditor.setAclGroup(JCRClientUtils.AUTHORIZATIONS_ACL); //todo parameterize
            rightsEditor.setCanBreakInheritance(false);
            if (!(node.getProviderKey().equals("default") || node.getProviderKey().equals("jahia"))) {
                rightsEditor.setReadOnly(true);
            } else {
                rightsEditor.setReadOnly(!node.isWriteable() || node.isLocked());
            }

            setLayout(new FitLayout());
            rightsEditor.addNewAclPanel(RightsTabItem.this);
        }
    }

//    private void getACL(final GWTJahiaNode node) {
//        mask(Messages.get("label.loading","Loading..."), "x-mask-loading");
//        JahiaContentManagementService.App.getInstance().getACL(node.getPath(), new BaseAsyncCallback<GWTJahiaNodeACL>() {
//            /**
//             * onsuccess
//             * @param gwtJahiaNodeACL
//             */
//            public void onSuccess(final GWTJahiaNodeACL gwtJahiaNodeACL) {
//                unmask();
//                // auth. editor
//                layout();
//            }
//
//            /**
//             * On failure
//             * @param throwable
//             */
//            public void onApplicationFailure(Throwable throwable) {
//                Log.debug("Cannot retrieve acl", throwable);
//            }
//        });
//    }

    public AclEditor getRightsEditor() {
        return rightsEditor;
    }

}
