package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.QPropertyDefinition;
import org.apache.jackrabbit.spi.commons.name.NameConstants;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 31, 2009
 * Time: 11:46:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaNodeTypeInstanceHandler extends NodeTypeInstanceHandler {
    private Calendar created = null;
    private Calendar lastModified = null;
    private String createdBy = null;
    private String lastModifiedBy = null;

    /**
     * userid to use for the "*By" autocreated properties
     */
    private final String userId;

    /**
     * Creates a new node type instance handler.
     * @param userId the user id. if <code>null</code>, {@value #DEFAULT_USERID} is used.
     */
    public JahiaNodeTypeInstanceHandler(String userId) {
        super(userId);
        this.userId = userId == null
                ? DEFAULT_USERID
                : userId;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public void setLastModified(Calendar lastModified) {
        this.lastModified = lastModified;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    @Override
    public InternalValue[] computeSystemGeneratedPropertyValues(NodeState parent, QPropertyDefinition def) {
        // add our own auto generated values here
        InternalValue[] genValues = null;

        Name name = def.getName();
        Name declaringNT = def.getDeclaringNodeType();

        if (NameConstants.JCR_CREATED.equals(name)) {
            // jcr:created property of a version or a mix:created
            if (NameConstants.MIX_CREATED.equals(declaringNT)
                    || NameConstants.NT_VERSION.equals(declaringNT)) {
                genValues = new InternalValue[]{InternalValue.create(created != null ? created : GregorianCalendar.getInstance())};
            }
        } else if (NameConstants.JCR_CREATEDBY.equals(name)) {
            // jcr:createdBy property of a mix:created
            if (NameConstants.MIX_CREATED.equals(declaringNT)) {
                genValues = new InternalValue[]{InternalValue.create(createdBy != null ? createdBy : userId)};
            }
        } else if (NameConstants.JCR_LASTMODIFIED.equals(name)) {
            // jcr:lastModified property of a mix:lastModified
            if (NameConstants.MIX_LASTMODIFIED.equals(declaringNT)) {
                genValues = new InternalValue[]{InternalValue.create(lastModified != null ? lastModified : GregorianCalendar.getInstance())};
            }
        } else if (NameConstants.JCR_LASTMODIFIEDBY.equals(name)) {
            // jcr:lastModifiedBy property of a mix:lastModified
            if (NameConstants.MIX_LASTMODIFIED.equals(declaringNT)) {
                genValues = new InternalValue[]{InternalValue.create(lastModifiedBy != null ? lastModifiedBy : userId)};
            }
        } else {
            genValues = super.computeSystemGeneratedPropertyValues(parent, def);
        }
        return genValues;
    }
}
