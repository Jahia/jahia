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

 package org.jahia.workflow.nstep;

import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 12 avr. 2006
 * Time: 09:39:44
 * To change this template use File | Settings | File Templates.
 */
public class MailInfo {
    private String text;
    private String processName;
    private String userName;
    private String displayName;
    private String comment;
    private String authorName;
    private Locale language;
    private int pageID;
    private String pageLink;
    private String currentStepLabel;
    private String nextStepLabel;
    private boolean rollbackedToAuthor;
    private boolean finished;
    private boolean deleted;
    private boolean advanced;
    private boolean rollbacked;
    private String pageTitle;

    public MailInfo(String body, String processName, String userName, String displayName, String comment,
                    String authorName, Locale language, int pageID, String pageLink, String currentStepLabel,
                    String nextStepLabel, boolean rollbackedToAuthor, boolean finished, boolean deleted,
                    boolean advanced, boolean rollbacked, String pageTitle) {
        this.text = body;
        this.processName = processName;
        this.userName = userName;
        this.displayName = displayName;
        this.comment = comment;
        this.authorName = authorName;
        this.language = language;
        this.pageID = pageID;
        this.pageLink = pageLink;
        this.currentStepLabel = currentStepLabel;
        this.nextStepLabel = nextStepLabel;
        this.rollbackedToAuthor = rollbackedToAuthor;
        this.finished = finished;
        this.deleted = deleted;
        this.advanced = advanced;
        this.rollbacked = rollbacked;
        this.pageTitle = pageTitle;
    }

    public String getText() {
        return text;
    }

    public String getProcessName() {
        return processName;
    }

    public String getUserName() {
        return userName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getComment() {
        return comment;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Locale getLanguage() {
        return language;
    }

    public int getPageID() {
        return pageID;
    }

    public String getPageLink() {
        return pageLink;
    }

    public String getCurrentStepLabel() {
        return currentStepLabel;
    }

    public String getNextStepLabel() {
        return nextStepLabel;
    }

    public boolean isRollbackedToAuthor() {
        return rollbackedToAuthor;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public boolean isAdvanced() {
        return advanced;
    }

    public boolean isRollbacked() {
        return rollbacked;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MailInfo mailInfo = (MailInfo) o;

        if (advanced != mailInfo.advanced) return false;
        if (deleted != mailInfo.deleted) return false;
        if (finished != mailInfo.finished) return false;
        if (pageID != mailInfo.pageID) return false;
        if (rollbacked != mailInfo.rollbacked) return false;
        if (rollbackedToAuthor != mailInfo.rollbackedToAuthor) return false;
        if (authorName != null ? !authorName.equals(mailInfo.authorName) : mailInfo.authorName != null) return false;
        if (comment != null ? !comment.equals(mailInfo.comment) : mailInfo.comment != null) return false;
        if (currentStepLabel != null ? !currentStepLabel.equals(mailInfo.currentStepLabel) : mailInfo.currentStepLabel != null)
            return false;
        if (displayName != null ? !displayName.equals(mailInfo.displayName) : mailInfo.displayName != null) return false;
        if (language != null ? !language.equals(mailInfo.language) : mailInfo.language != null) return false;
        if (nextStepLabel != null ? !nextStepLabel.equals(mailInfo.nextStepLabel) : mailInfo.nextStepLabel != null)
            return false;
        if (pageLink != null ? !pageLink.equals(mailInfo.pageLink) : mailInfo.pageLink != null) return false;
        if (processName != null ? !processName.equals(mailInfo.processName) : mailInfo.processName != null) return false;
        if (text != null ? !text.equals(mailInfo.text) : mailInfo.text != null) return false;
        if (userName != null ? !userName.equals(mailInfo.userName) : mailInfo.userName != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (text != null ? text.hashCode() : 0);
        result = 29 * result + (processName != null ? processName.hashCode() : 0);
        result = 29 * result + (userName != null ? userName.hashCode() : 0);
        result = 29 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 29 * result + (comment != null ? comment.hashCode() : 0);
        result = 29 * result + (authorName != null ? authorName.hashCode() : 0);
        result = 29 * result + (language != null ? language.hashCode() : 0);
        result = 29 * result + pageID;
        result = 29 * result + (pageLink != null ? pageLink.hashCode() : 0);
        result = 29 * result + (currentStepLabel != null ? currentStepLabel.hashCode() : 0);
        result = 29 * result + (nextStepLabel != null ? nextStepLabel.hashCode() : 0);
        result = 29 * result + (rollbackedToAuthor ? 1 : 0);
        result = 29 * result + (finished ? 1 : 0);
        result = 29 * result + (deleted ? 1 : 0);
        result = 29 * result + (advanced ? 1 : 0);
        result = 29 * result + (rollbacked ? 1 : 0);
        return result;
    }
}
