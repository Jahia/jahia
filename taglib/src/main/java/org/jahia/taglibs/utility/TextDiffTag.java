package org.jahia.taglibs.utility;

import name.fraser.neil.plaintext.DiffMatchPatch;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.LinkedList;
import java.io.IOException;

import org.apache.taglibs.standard.tag.common.core.Util;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 4, 2009
 * Time: 11:32:03 AM
 * To change this template use File | Settings | File Templates.
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
        LinkedList<DiffMatchPatch.Diff> list = diff.diff_main(newText, oldText);
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
