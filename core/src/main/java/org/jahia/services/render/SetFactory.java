package org.jahia.services.render;

import org.apache.commons.collections.Factory;

import java.io.Serializable;
import java.util.LinkedHashSet;

public class SetFactory implements Factory, Serializable {
    public Object create() {
        return new LinkedHashSet<String>();
    }
}