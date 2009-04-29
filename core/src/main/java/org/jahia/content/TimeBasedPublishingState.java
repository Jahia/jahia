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
