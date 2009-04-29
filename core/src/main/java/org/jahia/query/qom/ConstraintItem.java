/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.query.qom;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 nov. 2007
 * Time: 10:07:40
 * To change this template use File | Settings | File Templates.
 */
public class ConstraintItem {

    private ConstraintImpl constraint;
    private List<ConstraintItem> childConstraintItems;

    private ConstraintItem parentConstraint;

    public ConstraintItem(ConstraintImpl constraint) {
        this.constraint = constraint;
        this.childConstraintItems = new ArrayList<ConstraintItem>();
    }

    public ConstraintItem(ConstraintItem parentConstraint, ConstraintImpl constraint) {
        this.parentConstraint = parentConstraint;
        this.constraint = constraint;
        this.childConstraintItems = new ArrayList<ConstraintItem>();
    }

    public ConstraintImpl getConstraint() {
        return constraint;
    }

    public void setConstraint(ConstraintImpl constraint) {
        this.constraint = constraint;
    }

    public List<ConstraintItem> getChildConstraintItems() {
        return childConstraintItems;
    }

    public void setChildConstraintItems(List<ConstraintItem> childConstraintItems) {
        this.childConstraintItems = childConstraintItems;
    }

    public void addChildConstraintItem(ConstraintItem constraintItem) {
        this.childConstraintItems.add(constraintItem);
    }

    public ConstraintItem getParentConstraint() {
        return parentConstraint;
    }

    public void setParentConstraint(ConstraintItem parentConstraint) {
        this.parentConstraint = parentConstraint;
    }

}
