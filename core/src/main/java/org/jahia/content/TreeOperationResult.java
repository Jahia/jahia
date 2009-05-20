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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * <p>Title: This class contains the results of a recursive tree
 * operation.</p>
 * <p>Description: This class is used directly or derived to store the
 * results of operations that occur on a tree, such as a content activation
 * or a content version restore. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber
 * @version 1.0
 * @todo implement a way to migrate errors into warnings when actually treating
 * an error in code.
 */

public class TreeOperationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(TreeOperationResult.class);

    static final public int FAILED_OPERATION_STATUS = 0;
    static final public int COMPLETED_OPERATION_STATUS = 1;
    static final public int PARTIAL_OPERATION_STATUS = 2;

    // The purpose of this matrix is to determine the resulting status. The
    // hardcoded values correspond to the operation status constants above.
    private static final int[][] mergeMatrix = {
        { FAILED_OPERATION_STATUS, FAILED_OPERATION_STATUS, FAILED_OPERATION_STATUS },
        { FAILED_OPERATION_STATUS, COMPLETED_OPERATION_STATUS, PARTIAL_OPERATION_STATUS },
        { FAILED_OPERATION_STATUS, PARTIAL_OPERATION_STATUS, PARTIAL_OPERATION_STATUS }
        };

    // This structure is used to store messages that are non fatal.
    final List<Object> warnings = new ArrayList<Object>();

    // This structure is used to store messages that are fatal
    final List<Object> errors = new ArrayList<Object>();

    // always look on the bright side :) Actually we need this default
    // in order to make the matrix merging successful more often.
    int status = COMPLETED_OPERATION_STATUS;

    public TreeOperationResult() {
    }

    public TreeOperationResult(int initialStatus) {
        status = initialStatus;
    }

    public void appendWarning(Object warning) {
        warnings.add(warning);
    }

    public void appendError(Object error) {
        errors.add(error);
    }

    public List<Object> getWarnings() {
        return warnings;
    }

    public List<Object> getErrors() {
        return errors;
    }

    public ListIterator<Object> getWarningIterator() {
        return warnings.listIterator();
    }

    public ListIterator<Object> getErrorIterator() {
        return errors.listIterator();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int newStatus) {
        status = newStatus;
    }

    /**
     * Updates the status considering status merge matrix.
     * 
     * @param newStatus
     */
    public void mergeStatus(int newStatus) {
        setStatus(mergeMatrix[newStatus][getStatus()]);
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        switch (status) {
            case FAILED_OPERATION_STATUS :
                result.append("Status=FAILED ");
                break;
            case COMPLETED_OPERATION_STATUS :
                result.append("Status=COMPLETED ");
                break;
            case PARTIAL_OPERATION_STATUS :
                result.append("Status=PARTIAL ");
                break;
        }
        result.append(" errors : [");
        int count = 0;
        ListIterator<Object> errorIter = errors.listIterator();
        while (errorIter.hasNext()) {
            Object curError = errorIter.next();
            if (count < errors.size()) {
                result.append(curError.toString() + " , ");
            }
            count++;
        }
        result.append("]");

        result.append(" warnings : [");
        count = 0;
        ListIterator<Object> warningIter = warnings.listIterator();
        while (warningIter.hasNext()) {
            Object curWarning = warningIter.next();
            if (count < warnings.size()) {
                result.append(curWarning.toString() + " , ");
            }
            count++;
        }
        result.append("]");

        return result.toString();
    }

    public void merge(TreeOperationResult input) {
        if (input == null) {
            logger.debug("Error : merging recursive operation results with null object ! Resulting status is failed !");
            status = FAILED_OPERATION_STATUS;
            return;
        }
        mergeStatus(input.getStatus());
        warnings.addAll(input.getWarnings());
        errors.addAll(input.getErrors());
    }

    /**
     * This method migrates all the errors to warnings.
     */
    public void moveErrorsToWarnings() {
        warnings.addAll(errors);
        errors.clear();
    }

    /**
     * Returns <code>true</code> if the result contains error and at least one
     * of them is a blocker. Otherwise returns <code>false</code>.
     * 
     * @return <code>true</code> if the result contains error and at least one
     *         of them is a blocker. Otherwise returns <code>false</code>
     */
    public boolean hasBlockerError() {
        boolean hasBlocker = false;
        for (Object error : getErrors()) {
            if (((NodeOperationResult)error).isBlocker()) {
                hasBlocker = true;
                break;
            }
        }

        return hasBlocker;
    }
}