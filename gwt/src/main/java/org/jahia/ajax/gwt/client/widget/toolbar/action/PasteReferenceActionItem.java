package org.jahia.ajax.gwt.client.widget.toolbar.action;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.edit.ContentTypeWindow;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.CreateContentEngine;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:57:42 PM
* To change this template use File | Settings | File Templates.
*/
public class PasteReferenceActionItem extends BaseActionItem  {
    protected transient List<String> allowedRefs;

    public void onComponentSelection() {
        if (CopyPasteEngine.getInstance().getCopiedPaths().size() == 1) {
        JahiaContentDefinitionService.App.getInstance().getNodeTypes(allowedRefs, new BaseAsyncCallback<List<GWTJahiaNodeType>>() {
            public void onApplicationFailure(Throwable caught) {
                Window.alert("Cannot retrieve node type 'jnt:navMenuNodeLink'. Cause: " + caught.getLocalizedMessage());
                Log.error("Cannot retrieve node type 'jnt:navMenuNodeLink'. Cause: " + caught.getLocalizedMessage(), caught);
            }

            public void onSuccess(List<GWTJahiaNodeType> result) {
                GWTJahiaNode copiedNode = CopyPasteEngine.getInstance().getCopiedPaths().get(0);
                Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>(2);
                props.put("jcr:title", new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(linker.getSelectedNode().getDisplayName(), GWTJahiaNodePropertyType.STRING)));
                props.put("j:node", new GWTJahiaNodeProperty("j:node", new GWTJahiaNodePropertyValue(copiedNode, GWTJahiaNodePropertyType.WEAKREFERENCE)));
                if (result.size() == 1) {
                    new CreateContentEngine(linker, linker.getSelectedNode(), result.get(0), props, copiedNode.getName(), false).show();
                } else {
                    Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> m = new HashMap<GWTJahiaNodeType, List<GWTJahiaNodeType>>();
                    m.put(null, result);
                    new ContentTypeWindow(linker, linker.getSelectedNode(), m, props, copiedNode.getName(), false).show();
                }
            }
        });
        } else {
            ContentActions.pasteReference(linker);
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        boolean b = lh.isMainSelection() && lh.isParentWriteable() && lh.isPasteAllowed() ||
                lh.isTableSelection() && lh.isWriteable() && lh.isPasteAllowed();

        if (linker instanceof EditLinker && b) {
            final Module module = ((EditLinker) linker).getSelectedModule();
            if (module.getReferenceTypes().length() > 0) {
                String[] refs = module.getReferenceTypes().split(" ");
                allowedRefs = new ArrayList<String>();
                for (String ref : refs) {
                    String[] types = ref.split("\\[|\\]");
                    if (checkNodeType(CopyPasteEngine.getInstance().getCopiedPaths(), types[1])) {
                        allowedRefs.add(types[0]);
                    }
                }
                if (this.allowedRefs.size() == 0) {
                    b = false;
                }
            } else {
                b = false;
            }
        }

        setEnabled(b);
    }

    private boolean checkNodeType(List<GWTJahiaNode> sources, String nodetypes) {
        boolean allowed = true;

        if (nodetypes != null && nodetypes.length() > 0) {
            if (sources != null) {
                String[] allowedTypes = nodetypes.split(" |,");
                for (GWTJahiaNode source : sources) {
                    boolean nodeAllowed = false;
                    for (String type : allowedTypes) {
                        if (source.getNodeTypes().contains(type) || source.getInheritedNodeTypes().contains(type)) {
                            nodeAllowed = true;
                            break;
                        }
                    }
                    allowed &= nodeAllowed;
                }
            }
        } else {
            allowed = false;
        }
        return allowed;
    }

}
