package org.jahia.services.history;

import org.apache.log4j.Logger;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Oct 5, 2010
 * Time: 11:32:15 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name = "jahia_contenthistory",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"date", "uuid", "propertyName"})}
)
public class HistoryEntry {
    private transient static Logger logger = Logger.getLogger(HistoryEntry.class);
    private Long id = 0l;
    private Date date;
    private String path;
    private String uuid;
    private String action;
    private String propertyName;
    private String userKey;
    private String message;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Basic
    @Column(length = 50)
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Basic
    @Column(length = 50)
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Lob
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Basic
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Basic
    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    @Basic
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
