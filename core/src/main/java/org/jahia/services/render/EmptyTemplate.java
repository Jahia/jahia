package org.jahia.services.render;

/**
* Created by IntelliJ IDEA.
* User: loom
* Date: 12.04.11
* Time: 15:59
* To change this template use File | Settings | File Templates.
*/
class EmptyTemplate extends Template {
    public EmptyTemplate(String serialized) {
        super(serialized);
    }

    public EmptyTemplate(String view, String node, Template next) {
        super(view, node, next);
    }
}
