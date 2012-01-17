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

package org.jahia.services.workflow.jbpm;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.pvm.internal.history.model.HistoryProcessInstanceImpl;
import org.jbpm.pvm.internal.history.model.HistoryVariableImpl;
import org.jbpm.pvm.internal.query.AbstractQuery;
import org.jbpm.pvm.internal.query.Page;
import org.jbpm.pvm.internal.util.CollectionUtil;

/**
 * Query history process by variable
 */
public class HistoryProcessInstanceByVariableQuery extends AbstractQuery {

  private static final long serialVersionUID = 1L;

  protected String processDefinitionId;
  protected String state;
  protected String processInstanceId;
  protected String processInstanceKey;
  protected boolean useLike = false;

  protected boolean ended;
  protected Date endedBefore;
  protected Date endedAfter;

    protected String variableName;
    protected String value;

  public String hql() {
  	StringBuilder hql = new StringBuilder();

  	hql.append("select ");
  	if (count) {
  	  hql.append("count(hpi) ");
  	} else {
  	  hql.append("hpi ");
  	}

    hql.append("from ");
    hql.append(HistoryProcessInstanceImpl.class.getName());
    hql.append(" as hpi ");

      if (variableName != null) {
          hql.append(", ");
          hql.append(HistoryVariableImpl.class.getName());
          hql.append(" as hv ");
          appendWhereClause(" hpi.processInstanceId = hv.processInstanceId", hql);
          appendWhereClause(" hv.variableName = '"+variableName+"' ", hql );
          if (useLike) {
              appendWhereClause(" hv.value like '"+value+"' ", hql );
          } else {
              appendWhereClause(" hv.value = '"+value+"' ", hql );
          }
      }

    if (processInstanceId!=null) {
      appendWhereClause(" hpi.processInstanceId = '"+processInstanceId+"' ", hql);
    }

    if (processDefinitionId!=null) {
      appendWhereClause(" hpi.processDefinitionId = '"+processDefinitionId+"' ", hql);
    }

    if (state!=null) {
      appendWhereClause(" hpi.state = '"+state+"' ", hql);
    }

    if (processInstanceKey!=null) {
      appendWhereClause(" hpi.key = '" + processInstanceKey + "'", hql);
    }

    if (ended) {
      appendWhereClause(" hpi.endTime is not null", hql);
    }
    if (endedBefore != null) {
      appendWhereClause(" hpi.endTime < :before", hql);
    }
    if (endedAfter != null) {
      appendWhereClause(" hpi.endTime >= :after", hql);
    }

    appendOrderByClause(hql);

    return hql.toString();
  }

  protected void applyParameters(Query query) {
    if (endedBefore != null) {
      query.setTimestamp("before", endedBefore);
    }
    if (endedAfter != null) {
      query.setTimestamp("after", endedAfter);
    }
  }

  public List<HistoryProcessInstance> list() {
    return CollectionUtil.checkList(untypedList(), HistoryProcessInstance.class);
  }

  public HistoryProcessInstance uniqueResult() {
    return (HistoryProcessInstance)untypedUniqueResult();
  }

  public HistoryProcessInstanceByVariableQuery processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }

  public HistoryProcessInstanceByVariableQuery orderAsc(String property) {
    addOrderByClause("hpi."+property+" asc");
    return this;
  }

  public HistoryProcessInstanceByVariableQuery orderDesc(String property) {
    addOrderByClause("hpi."+property+" desc");
    return this;
  }

  public HistoryProcessInstanceByVariableQuery page(int firstResult, int maxResults) {
    this.page = new Page(firstResult, maxResults);
    return this;
  }

  public HistoryProcessInstanceByVariableQuery processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public HistoryProcessInstanceByVariableQuery processInstanceKey(String processInstanceKey) {
    this.processInstanceKey = processInstanceKey;
    return this;
  }

    public HistoryProcessInstanceByVariableQuery variable(String variableName, String value) {
        this.variableName = variableName;
        this.value = value;
        return this;
    }

    public HistoryProcessInstanceByVariableQuery variableLike(String variableName, String value) {
        this.variableName = variableName;
        this.value = value;
        this.useLike = true;
        return this;
    }

  public HistoryProcessInstanceByVariableQuery state(String state) {
    this.state = state;
    return this;
  }

  public HistoryProcessInstanceByVariableQuery ended() {
    ended = true;
    endedBefore = endedAfter = null;
    return this;
  }

  public HistoryProcessInstanceByVariableQuery endedBefore(Date threshold) {
    if (endedAfter != null && endedAfter.after(threshold)) {
      throw new IllegalArgumentException("threshold is later than endedAfter date");
    }
    endedBefore = threshold;
    ended = false;
    return this;
  }

  public HistoryProcessInstanceByVariableQuery endedAfter(Date threshold) {
    if (endedBefore != null && endedBefore.before(threshold)) {
      throw new IllegalArgumentException("threshold is earlier than endedBefore date");
    }
    endedAfter = threshold;
    ended = false;
    return this;
  }
}