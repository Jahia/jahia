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
package org.jahia.services.workflow;

/**
 * Encapsulates information about the state of an External Workflow.
 *
 * @author Xavier Lawrence
 */
public class ExternalWorkflowInstanceCurrentInfos {
    private int nextStep;
    private int currentStep;
    private String nextRole;
    private String currentRole;
    private boolean isLastStep;

    public ExternalWorkflowInstanceCurrentInfos() {
    }

    public ExternalWorkflowInstanceCurrentInfos(final int nextStep,
                                                final int currentStep,
                                                final String nextRole,
                                                final String currentRole,
                                                final boolean isLastStep) {
        this.nextStep = nextStep;
        this.currentStep = currentStep;
        this.nextRole = nextRole;
        this.currentRole = currentRole;
        this.isLastStep = isLastStep;
    }

    public int getNextStep() {
        return nextStep;
    }

    public void setNextStep(final int nextStep) {
        this.nextStep = nextStep;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(final int currentStep) {
        this.currentStep = currentStep;
    }

    public String getNextRole() {
        return nextRole;
    }

    public void setNextRole(final String nextRole) {
        this.nextRole = nextRole;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(final String currentRole) {
        this.currentRole = currentRole;
    }

    public boolean isLastStep() {
        return isLastStep;
    }

    public void setLastStep(final boolean lastStep) {
        isLastStep = lastStep;
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("ExternalWorkflowInstanceCurrentInfos: ");
        buff.append("currentRole: ").append(currentRole);
        buff.append(", currentStep: ").append(currentStep);
        buff.append(", nextRole: ").append(nextRole);
        buff.append(", nextStep: ").append(nextStep);
        buff.append(", isLastStep: ").append(isLastStep);
        return buff.toString();
    }
}
