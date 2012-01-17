/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;

/**
 * Key-value bean for representing values in selection lists or boxes.
 * User: rfelden
 * Date: 22 oct. 2008 - 12:48:46
 */
public class GWTJahiaValueDisplayBean extends BaseModelData implements Serializable {

    public GWTJahiaValueDisplayBean() {
        super() ;
    }

    public GWTJahiaValueDisplayBean(String value, String display) {
        super() ;
        setAllowNestedValues(false);
        set("value", value);
        set("display", display);
        set("image", "");
    }

    public String getValue() {
        return get("value");
    }

    public void setValue(String value) {
        set("value", value) ;
    }

    public String getDisplay() {
        return get("display") ;
    }

    public void setDisplay(String display) {
        set("display", display) ;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj)
                || (getValue() != null && obj != null && getValue().equals(
                        ((GWTJahiaValueDisplayBean) obj).getValue()));
    }
    
    
}
