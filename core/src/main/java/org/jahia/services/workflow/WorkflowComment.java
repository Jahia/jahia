package org.jahia.services.workflow;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 6, 2010
 * Time: 11:37:02 AM
 * 
 */
public class WorkflowComment implements Serializable {
    private String user;
    private Date time;
    private String comment;

    public WorkflowComment() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
