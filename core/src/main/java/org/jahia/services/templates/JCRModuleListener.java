package org.jahia.services.templates;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.ExternalEventListener;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;


public class JCRModuleListener  extends DefaultEventListener implements ExternalEventListener {

    private TemplatePackageRegistry packageRegistry;

    private Listener listener;

    public void setPackageRegistry(TemplatePackageRegistry packageRegistry) {
        this.packageRegistry = packageRegistry;
    }

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED;
    }

    @Override
    public String getPath() {
        return "/modules";
    }

    @Override
    public String[] getNodeTypes() {
        return new String[] {"jnt:moduleVersion"};
    }

    public void onEvent(EventIterator events) {
        while (events.hasNext()) {
            try {
                Event e = events.nextEvent();
                String path = e.getPath();
                String[] splitpath = path.split("/");
                JahiaTemplatesPackage p = packageRegistry.lookupByFileNameAndVersion(splitpath[2], new ModuleVersion(splitpath[3]));
                if (listener != null && p != null) {
                    listener.onModuleImported(p);
                }
            } catch (Exception e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onModuleImported(JahiaTemplatesPackage pack);
    }
}
