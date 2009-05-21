/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.content;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 9 ao�t 2005
 * Time: 10:17:28
 * To change this template use File | Settings | File Templates.
 */
public interface TimeBasedPublishingState {

    public static final int EXPIRED_STATE = 0;
    public static final int NOT_VALID_STATE = 1;
    public static final int IS_VALID_STATE = 2;

    /**
     * Flag used to request expired content only.
     */
    public static final int EXPIRED_STATE_LOAD_FLAG = 1;

    /**
     * Flag used to request content not available yet.
     */
    public static final int NOT_VALID_STATE_LOAD_FLAG = 2;

    /**
     * Flag used to request content that are available yet.
     */
    public static final int IS_VALID_STATE_LOAD_FLAG = 4;

    /**
     * Flag used to request all content that are either expired or not available.
     */
    public static final int EXPIRED_OR_NOT_VALID_STATE_LOAD_FLAG = EXPIRED_STATE_LOAD_FLAG | NOT_VALID_STATE_LOAD_FLAG;

    /**
     * Flag used to request all content.
     */
    public static final int ALL_STATES_LOAD_FLAG = EXPIRED_STATE_LOAD_FLAG | NOT_VALID_STATE_LOAD_FLAG
            | IS_VALID_STATE_LOAD_FLAG;

}
