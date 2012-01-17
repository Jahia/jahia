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

package org.jahia.taglibs.utility;

import name.fraser.neil.plaintext.DiffMatchPatch;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.LinkedList;
import java.io.IOException;

import org.apache.taglibs.standard.tag.common.core.Util;

/**
 * 
 * User: toto
 * Date: Dec 4, 2009
 * Time: 11:32:03 AM
 * 
 */
public class TextDiffTag extends TagSupport {

    private String oldText;
    private String newText;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;

    public void setOldText(String oldText) {
        this.oldText = oldText;
    }

    public void setNewText(String newText) {
        this.newText = newText;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    @Override
    public int doStartTag() throws JspException {
        DiffMatchPatch diff = new DiffMatchPatch();
        LinkedList<DiffMatchPatch.Diff> list = diff.diff_main(oldText, newText);
        diff.diff_cleanupSemantic(list);
        if (var != null) {
            pageContext.setAttribute(var,diff.diff_prettyHtml(list), scope);
        } else {
            try {
                pageContext.getOut().print(diff.diff_prettyHtml(list));
            } catch (IOException e) {
                throw new JspException(e);
            }
        }
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        oldText = null;
        newText = null;
        var = null;
        scope = PageContext.PAGE_SCOPE;
        return super.doEndTag();
    }
}
