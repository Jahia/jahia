/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

    private Iterator<Hit<?>> resultIterator;
    private List<Hit<?>> hits;

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
        List<Hit<?>> results = getHits();
        if (results == null) {
            ResultsTag parent = (ResultsTag) findAncestorWithClass(this, ResultsTag.class);
            if (null == parent) {
                throw new JspTagException("Parent tag not found. This tag ("
                        + this.getClass().getName() + ") must be nested inside the "
                        + ResultsTag.class.getName());
            }

            results = parent.getHits();
        }
        if (results == null || results.size() <= begin) {
            results = Collections.emptyList();
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
        if (end > 0) {
            this.endSpecified = true;
            this.end = end;
            validateEnd();
        }
    }

    public void setStep(int step) throws JspTagException {
        this.stepSpecified = true;
        this.step = step;
        validateStep();
    }

    public List<Hit<?>> getHits() {
        return hits;
    }

    public void setHits(List<Hit<?>> hits) {
        this.hits = hits;
    }
}
