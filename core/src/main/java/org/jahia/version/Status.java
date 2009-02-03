/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
