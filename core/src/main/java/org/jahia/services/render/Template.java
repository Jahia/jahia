package org.jahia.services.render;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: Oct 28, 2010
* Time: 3:36:35 PM
* 
*/
public class Template implements Serializable {

    private static final long serialVersionUID = -1700784569502723022L;
    
    public String view;
    public String node;
    public Template next;

    public Template(String view, String node, Template next) {
        this.view = view;
        this.node = node;
        this.next = next;
    }

    public Template(String serialized) {
        String[] s = StringUtils.substringBefore(serialized, "|").split("/");
        this.view = s[0].equals("null") ? null : s[0];
        this.node = s[1];
        String n = StringUtils.substringAfter(serialized, "|");
        if (!StringUtils.isEmpty(n)) {
            this.next = new Template(n);
        }
    }

    @Override
    public String toString() {
        return view + " for node " + node;
    }

    public String getView() {
        if (view == null) {
            return "default";
        }
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

    public String serialize() {
        String r = view+"/"+node;
        if (next != null) {
            r += "|" + next.serialize();
        }
        return r;
    }
}
