package org.jahia.services.render;

import java.io.Serializable;

/**
 * This class is used to sort content templates by priority for template resolution
 * @see RenderService
 */
public class OrderedContentTemplate implements Comparable<OrderedContentTemplate>, Serializable {


    private static final long serialVersionUID = 5335847511441188234L;
    private int priority;
    private Template template;


    /**
     * Class constructor
     * @param template
     * @param priority  : resolution takes the higher the priority
     */
    public OrderedContentTemplate(Template template, int priority) {
        this.priority = priority;
        this.template = template;
    }

    public Template getTemplate() {
        return template;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(OrderedContentTemplate o) {
        return this.priority - o.getPriority();
    }
}

