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
          appendWhereClause(" hv.value = '"+value+"' ", hql );
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