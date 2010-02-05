package org.jahia.services.content.nodetypes;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 8:24:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class OnConflictAction {
    public static final int UNRESOLVED = 1;
    public static final int USE_SOURCE = 2;
    public static final int USE_TARGET = 3;
    public static final int USE_OLDEST = 4;
    public static final int USE_LATEST = 5;
    public static final int NUMERIC_USE_MIN = 6;
    public static final int NUMERIC_USE_MAX = 7;
    public static final int NUMERIC_SUM = 8;
    public static final int TEXT_MERGE = 9;
}
