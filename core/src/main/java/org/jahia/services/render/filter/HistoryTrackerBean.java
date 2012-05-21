package org.jahia.services.render.filter;

import org.springframework.beans.factory.BeanNameAware;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpSession;
import java.util.LinkedList;

public class HistoryTrackerBean implements BeanNameAware {
    private int historySize = 10;
    private String name = "historyTracker";

    public void setHistorySize(int historySize) {
        this.historySize = historySize;
    }

    public void setBeanName(String name) {
        this.name = name;
    }

    public void addHistoryNode(HttpSession session, String identifier) throws RepositoryException {
        LinkedList<String> historyTracker = (LinkedList<String>) session.getAttribute(name);
        if (historyTracker == null) {
            session.setAttribute(name, historyTracker = new LinkedList<String>());
        }
        historyTracker.remove(identifier);
        historyTracker.addFirst(identifier);
        if (historyTracker.size() > historySize) {
            historyTracker.removeLast();
        }
    }
}
