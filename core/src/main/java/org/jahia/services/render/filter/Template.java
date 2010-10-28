package org.jahia.services.render.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: Oct 28, 2010
* Time: 3:36:35 PM
* To change this template use File | Settings | File Templates.
*/
public class Template implements Serializable {
    public String view;
    public String node;
    public Template next;

    Template(String view, String node, Template next) {
        this.view = view;
        this.node = node;
        this.next = next;
    }

    @Override
    public String toString() {
        return view + " for node " + node;
    }

    public String getView() {
        return view;
    }

    public String getNode() {
        return node;
    }

    public Template getNext() {
        return next;
    }

    public List<Template> getNextTemplates() {
        List<Template> t;
        if (next == null) {
             t = new ArrayList<Template>();
        } else {
            t = next.getNextTemplates();
        }
        t.add(this);
        return t;
    }
}
