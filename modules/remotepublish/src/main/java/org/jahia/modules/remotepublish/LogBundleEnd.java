package org.jahia.modules.remotepublish;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 23, 2010
 * Time: 4:39:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogBundleEnd implements Serializable {
    private Calendar date;

    public LogBundleEnd() {
    }

    public LogBundleEnd(Calendar date) {
        this.date = date;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }
}
