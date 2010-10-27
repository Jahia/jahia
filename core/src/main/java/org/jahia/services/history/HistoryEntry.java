package org.jahia.services.history;

import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.Locale;

/**
 * Represent a content change event entry.
 * User: loom
 * Date: Oct 5, 2010
 * Time: 11:32:15 AM
 */
@Entity
@Table(name = "jahia_contenthistory",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"entry_date", "uuid", "property_name"})}
)
public class HistoryEntry {

    private Long id = 0l;
    private Date date;
    private String path;
    private String uuid;
    private String action;
    private String propertyName;
    private String userKey;
    private String message;
    private transient Locale locale;

    @Id
    @GeneratedValue(generator = "jahia")
    @GenericGenerator(name ="jahia", strategy = "org.jahia.hibernate.dao.JahiaIdentifierGenerator")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "entry_date")
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
    @Column(length = 50, name = "property_name")
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Lob
    @Column(name = "entry_path")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Basic
    @Column(name = "entry_action")
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Basic
    @Column(name = "user_key")
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

    @Transient
    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("HistoryEntry");
        sb.append("{action='").append(action).append('\'');
        sb.append(", id=").append(id);
        sb.append(", date=").append(date);
        sb.append(", path='").append(path).append('\'');
        sb.append(", uuid='").append(uuid).append('\'');
        sb.append(", propertyName='").append(propertyName).append('\'');
        sb.append(", userKey='").append(userKey).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append(", locale=").append(locale);
        sb.append('}');
        return sb.toString();
    }
}
