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

package org.jahia.services.render;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
* 
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
