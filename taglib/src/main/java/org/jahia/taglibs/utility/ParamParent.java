package org.jahia.taglibs.utility;

import org.apache.taglibs.standard.tag.common.core.ParamSupport;

public interface ParamParent {

    /**
     * Adds an object parameter value to this tag.
     *
     * @see ParamSupport
     */
    void addParam(Object value);
}
