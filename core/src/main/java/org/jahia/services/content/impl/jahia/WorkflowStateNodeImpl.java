package org.jahia.services.content.impl.jahia;

import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.fields.ContentField;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.content.ContentObject;

import javax.jcr.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 9, 2009
 * Time: 1:34:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowStateNodeImpl extends NodeImpl {
    private JahiaContentNodeImpl parent;

    protected ContentObject object;


    public WorkflowStateNodeImpl(SessionImpl session, JahiaContentNodeImpl parent) throws RepositoryException {
        super(session);
        setDefinition(parent.getPrimaryNodeType().getChildNodeDefinitionsAsMap().get("j:workflowState"));
        setNodetype(NodeTypeRegistry.getInstance().getNodeType("jnt:workflowState"));
        this.parent = parent;
        this.object = parent.getContentObject();
    }

    public Node getParent() throws ItemNotFoundException, AccessDeniedException, RepositoryException {
        return parent;
    }

    @Override
    protected void initNodes() throws RepositoryException {
        super.initNodes();
    }

    @Override
    protected void initProperties() throws RepositoryException {
        if (properties == null) {
            super.initProperties();
            // workflowState
            try {

                String v = "";

                List<Locale> locales = getSite().getLanguageSettingsAsLocales(true);

                WorkflowService workflowService = ServicesRegistry.getInstance().getWorkflowService();
                int state;

                for (Locale locale : locales) {
                    String loc = (object.isShared()) ? "shared" : locale.toString();
                    Map<String, Integer> states = object.getLanguagesStates();
                    if (!states.containsKey(loc)) {
                        continue;
                    }
                    state = states.get(loc);
                    if (object instanceof ContentContainer) {
                        List<? extends ContentObject> l = object.getChilds(getProcessingContext().getUser(), getEntryLoadRequest());
                        for (Iterator<? extends ContentObject> contentObjectIterator = l.iterator(); contentObjectIterator.hasNext();) {
                            ContentObject child = contentObjectIterator.next();
                            if (child instanceof ContentField) {
                                loc = (child.isShared()) ? "shared" : locale.toString();
                                states = child.getLanguagesStates();
                                if (!states.containsKey(loc)) {
                                    continue;
                                }
                                state = Math.max(state,states.get(loc));
                            }
                        }
                    }
                    if (state == 1) {
                        v = "active";
                    } else {
                        String extState = workflowService.getExtendedWorkflowState(object, locale.toString());
                        char c = extState.charAt(1);
                        String quickEdit = "";
                        if (extState.charAt(2) == '1') {
                            quickEdit = "-quickEdit";
                        }
                        switch (c) {
                            case '1':
                                v = "active"; break;
                            case '2':
                                v = "staging"; break;
                            case '3':
                                v = "validationStep1"+quickEdit; break;
                            case '4':
                                v = "validationStep2"+quickEdit; break;
                            case '5':
                                v = "validationStep3"+quickEdit; break;
                        }
                    }

                    initProperty(new PropertyImpl(getSession(), this, locale.toString(),
                            nodetype.getPropertyDefinition("*"),
                            new ValueImpl(v, PropertyType.STRING)));

                }
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }

}
