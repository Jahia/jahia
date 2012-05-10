package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.servlet.http.HttpSession;
import java.util.LinkedList;

/**
 * Filter that logs into the session main resources visited by the user
 */
public class HistoryTrackerFilter extends AbstractFilter {
    private int historySize = 10;
    private String name = "historyTracker";

    public void setHistorySize(int historySize) {
        this.historySize = historySize;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        HttpSession session = renderContext.getRequest().getSession();
        LinkedList<String> historyTracker = (LinkedList<String>) session.getAttribute(name);
        if (historyTracker == null) {
            session.setAttribute(name, historyTracker = new LinkedList<String>());
        }
        String identifier = renderContext.getMainResource().getNode().getIdentifier();
        historyTracker.remove(identifier);
        historyTracker.addFirst(identifier);
        if (historyTracker.size() > historySize) {
            historyTracker.removeLast();
        }
        return super.execute(previousOut, renderContext, resource, chain);
    }
}
