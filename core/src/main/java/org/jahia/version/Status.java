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
package org.jahia.version;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 17 sept. 2007
 * Time: 11:27:15
 * To change this template use File | Settings | File Templates.
 */
public class Status {
    long start = System.currentTimeMillis();
    String scriptName = null;
    String subStatus = null;
    double percentCompleted = 0.d;
    double percentTimeUnit = 0.d;
    int count = 0;
    int result = -1;

    public String getScriptName() {
        return scriptName;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(String subStatus) {
        this.subStatus = subStatus;
        percentCompleted = 0.d;
        percentTimeUnit = 0.d;
        count = 0;
        start = System.currentTimeMillis();
    }

    public int getPercentCompleted() {
        return (int) percentCompleted;
    }

    public void setPercentCompleted(double percentCompleted) {
        this.percentCompleted = percentCompleted;
        percentTimeUnit = ((percentTimeUnit * count) + ((System.currentTimeMillis() - start) / percentCompleted)) / (count+1);
        count++;
    }

    public int getExecutionTime() {
        return (int) ((System.currentTimeMillis()-start) / 1000);
    }

    public int getRemainingTime() {
        return (int) (percentTimeUnit * (100.-percentCompleted)) / 1000;
    }

    public int getResult() {
        return result;
    }
}
