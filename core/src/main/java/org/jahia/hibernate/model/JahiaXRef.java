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
package org.jahia.hibernate.model;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 18 juil. 2007
 * Time: 17:03:53
 * To change this template use File | Settings | File Templates.
 */
/**
 * @hibernate.class table="jahia_reference" lazy="false"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaXRef implements Serializable {
    private JahiaXRefPK comp_id;

    public JahiaXRef() {
    }

    public JahiaXRef(JahiaXRefPK compid) {
        this.comp_id = compid;
    }

    /**
     * @hibernate.id generator-class="assigned"
     */
    public JahiaXRefPK getComp_id() {
        return comp_id;
    }

    public void setComp_id(JahiaXRefPK comp_id) {
        this.comp_id = comp_id;
    }
}
