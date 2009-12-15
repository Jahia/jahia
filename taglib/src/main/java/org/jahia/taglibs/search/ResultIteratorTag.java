/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.search;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.LoopTagSupport;

import org.jahia.services.search.Hit;

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
