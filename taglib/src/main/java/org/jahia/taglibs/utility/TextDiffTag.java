/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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

    private static final long serialVersionUID = 5137407997753630923L;
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
