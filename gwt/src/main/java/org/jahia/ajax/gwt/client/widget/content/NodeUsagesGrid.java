/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NodeUsagesGrid {
    private static final JahiaContentManagementServiceAsync instance = JahiaContentManagementService.App.getInstance();

    public static Grid<GWTJahiaNodeUsage> createUsageGrid(final List<GWTJahiaNode> nodes) {
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        ColumnConfig col = new ColumnConfig("pagePath", Messages.get("label.pageUrl", "Page URL"), 300);
        col.setRenderer(new GridCellRenderer<GWTJahiaNodeUsage>() {
            public Object render(
                    GWTJahiaNodeUsage gwtJahiaNodeUsage,
                    String s,
                    ColumnData columnData,
                    int i,
                    int i1,
                    ListStore<GWTJahiaNodeUsage> gwtJahiaNodeUsageListStore,
                    com.extjs.gxt.ui.client.widget.grid.Grid<GWTJahiaNodeUsage> gwtJahiaNodeUsageGrid) {
                if (gwtJahiaNodeUsage.getLanguage() == null) {
                return
                        "<a href=\"" + JahiaGWTParameters.getBaseUrl()+
                                gwtJahiaNodeUsage.getPagePath() + ".html\" target=\"_blank\">" +
                                gwtJahiaNodeUsage.getPageTitle()+"<a>";
                } else {
                    return
                            "<a href=\"" + JahiaGWTParameters.getBaseUrl()+ "/../"+gwtJahiaNodeUsage.getLanguage() +
                                    gwtJahiaNodeUsage.getPagePath() + ".html\" target=\"_blank\">" +
                                    gwtJahiaNodeUsage.getPageTitle()+" ("+gwtJahiaNodeUsage.getLanguage()+")<a>";

                }
            }
        });

        columns.add(col);

        col = new ColumnConfig("path", Messages.get("label.path", "URL"), 300);
        columns.add(col);
        ColumnModel cm = new ColumnModel(columns);
        final ListStore<GWTJahiaNodeUsage> usageStore = new ListStore<GWTJahiaNodeUsage>(new BaseListLoader(
                new RpcProxy() {
                    @Override
                    protected void load(Object loadConfig, final AsyncCallback asyncCallback) {
                    	List<String> paths = new ArrayList();
                        for (GWTJahiaNode selectedNode : nodes) {
                            paths.add(selectedNode.getPath());
                        }
                        instance.getUsages(paths, new BaseAsyncCallback<List<GWTJahiaNodeUsage>>() {
                            public void onApplicationFailure(Throwable throwable) {
                                Log.debug("Cannot retrieve usages", throwable);
                            }

                            public void onSuccess(List<GWTJahiaNodeUsage> gwtJahiaNodeUsages) {
                                asyncCallback.onSuccess(gwtJahiaNodeUsages);
                            }
                        });                        
                    }
                }));
        final Grid<GWTJahiaNodeUsage> tbl = new Grid<GWTJahiaNodeUsage>(usageStore, cm);

        tbl.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            public void handleEvent(GridEvent tableEvent) {
                Object url = tableEvent.getModel().get("path");
                if (url != null && url instanceof String) {
                    instance.getRenderedContent((String) url, null, JahiaGWTParameters.getUILanguage(), null,
                            "module", null, false, null, null, null, new BaseAsyncCallback<GWTRenderResult>() {
                                public void onSuccess(GWTRenderResult result) {
                                    HTML html = new HTML(result.getResult());
                                    Window w = new Window();
                                    w.setScrollMode(Style.Scroll.AUTO);
                                    w.setModal(true);
                                    w.setClosable(true);
                                    w.setMaximizable(true);
                                    w.setSize(800, 600);
                                    w.setBlinkModal(true);
                                    w.setPlain(true);
                                    w.setLayout(new CenterLayout());
                                    w.add(html);
                                    w.show();
                                }

                            });
                }
            }
        });
        usageStore.getLoader().load();
        return tbl;
    }
}