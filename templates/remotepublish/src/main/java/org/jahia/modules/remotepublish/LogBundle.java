package org.jahia.modules.remotepublish;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 22, 2010
 * Time: 2:11:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogBundle implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sourceUuid;
    private String sourcePath;

    public LogBundle() {
    }

    public String getSourceUuid() {
        return sourceUuid;
    }

    public void setSourceUuid(String sourceUuid) {
        this.sourceUuid = sourceUuid;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

}
