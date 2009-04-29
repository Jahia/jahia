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
package org.jahia.taglibs.search;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.LoopTagSupport;

import org.jahia.engines.search.Hit;

/**
 * Iterator over search results.
 * 
 * @author Sergiy Shyrkov
 */
@SuppressWarnings("serial")
public class ResultIteratorTag extends LoopTagSupport {

    private static final String DEF_VAR = "hit";

    private Iterator<Hit> resultIterator;

    /**
     * Initializes an instance of this class.
     */
    public ResultIteratorTag() {
        super();
        init();
    }

    @Override
    protected boolean hasNext() throws JspTagException {
        return resultIterator.hasNext();
    }

    private void init() {
        setVar(DEF_VAR);
    }

    @Override
    protected Object next() throws JspTagException {
        return resultIterator.next();
    }

    @Override
    protected void prepare() throws JspTagException {
        if (end != -1 && begin > end) {
            throw new JspTagException("'begin' > 'end'");
        }

        ResultsTag parent = (ResultsTag) findAncestorWithClass(this,
                ResultsTag.class);
        if (null == parent) {
            throw new JspTagException("Parent tag not found. This tag ("
                    + this.getClass().getName()
                    + ") must be nested inside the "
                    + ResultsTag.class.getName());
        }

        List<Hit> results = parent.getHits();
        if (results == null || results.size() <= begin) {
            results = Collections.emptyList();
        } else if (end != -1 && begin > 0) {
            results = results.subList(begin, Math.min(results.size(), end));

        }

        resultIterator = results.iterator();
    }

    @Override
    public void release() {
        super.release();
        init();
    }

    public void setBegin(int begin) throws JspTagException {
        this.beginSpecified = true;
        this.begin = begin;
        validateBegin();
    }

    public void setEnd(int end) throws JspTagException {
        this.endSpecified = true;
        this.end = end;
        validateEnd();
    }

    public void setStep(int step) throws JspTagException {
        this.stepSpecified = true;
        this.step = step;
        validateStep();
    }
}
