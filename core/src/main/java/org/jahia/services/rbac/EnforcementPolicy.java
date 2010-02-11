package org.jahia.services.rbac;

import org.jahia.services.usermanager.GuestGroup;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.jcr.JCRUser;

/**
 * Defines the role/permission checking policy.
 * 
 * @author Sergiy Shyrkov
 */
public class EnforcementPolicy {

    public static class DenyAllEnforcementPolicy extends EnforcementPolicy {

        @Override
        public boolean isDenyAll() {
            return true;
        }
        
    }

    public static class EnforcementPolicyResult {
        private boolean applied = true;
        private boolean granted;

        public EnforcementPolicyResult(boolean granted) {
            super();
            this.granted = granted;
        }

        public boolean getResult() {
            return granted;
        }

        public boolean isApplied() {
            return applied;
        }

    }
    
    public static class GrantAllEnforcementPolicy extends EnforcementPolicy {

        @Override
        public boolean isGrantAll() {
            return true;
        }
        
    }

    private static final EnforcementPolicyResult NON_MATCHED_POLICY_RESULT = new EnforcementPolicyResult(false) {

        @Override
        public boolean getResult() {
            throw new UnsupportedOperationException("The policy is not applied on the principal."
                    + " The value for 'granted' is undefined.");
        }

        @Override
        public boolean isApplied() {
            return false;
        }
    };

    private boolean denyAll = false;

    private boolean denyAllToGuest = true;

    private boolean grantAll = true;

    private boolean grantAllToRoot = true;

    /**
     * Initializes an instance of this class.
     */
    public EnforcementPolicy() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param denyAll
     * @param grantAll
     * @param grantAllToRoot
     * @param denyAllToGuest
     */
    public EnforcementPolicy(boolean denyAll, boolean grantAll, boolean grantAllToRoot, boolean denyAllToGuest) {
        this();
        this.denyAll = denyAll;
        this.grantAll = grantAll;
        this.grantAllToRoot = grantAllToRoot;
        this.denyAllToGuest = denyAllToGuest;
    }

    public EnforcementPolicyResult enforce(JahiaPrincipal principal) {
        EnforcementPolicyResult result = NON_MATCHED_POLICY_RESULT;

        if (isDenyAll()) {
            // we deny all to any user
            result = new EnforcementPolicyResult(false);
        } else if (isGrantAll()) {
            // we grant all to any user
            result = new EnforcementPolicyResult(true);
        } else if (isGrantAllToRoot() && isRoot(principal)) {
            // we grant all to root user
            result = new EnforcementPolicyResult(true);
        } else if (isDenyAllToGuest() && isGuest(principal)) {
            // we deny all to guest
            result = new EnforcementPolicyResult(false);
        }

        return result;
    }

    /**
     * Returns {@code true} if any role/permission is silently denied to any
     * principal.
     * 
     * @return {@code true} if any role/permission is silently denied to any
     *         principal
     */
    public boolean isDenyAll() {
        return denyAll;
    }

    /**
     * Returns {@code true} if the guest user/group is silently denied all
     * roles/permissions, i.e. the role/permission check always evaluates to $
     * {@code false}.
     * 
     * @return {@code true} if the guest user/group is silently denied all
     *         roles/permissions, i.e. the role/permission check always
     *         evaluates to ${@code false}
     */
    public boolean isDenyAllToGuest() {
        return denyAllToGuest;
    }

    /**
     * Returns {@code true} if any role/permission is silently granted to any
     * principal.
     * 
     * @return {@code true} if any role/permission is silently granted to any
     *         principal
     */
    public boolean isGrantAll() {
        return grantAll;
    }

    /**
     * Returns {@code true} if the system root user is silently granted all
     * roles/permissions, i.e. the role/permission check always evaluates to $
     * {@code true}.
     * 
     * @return {@code true} if the system root user is silently granted all
     *         roles/permissions, i.e. the role/permission check always
     *         evaluates to ${@code true}
     */
    public boolean isGrantAllToRoot() {
        return grantAllToRoot;
    }

    /**
     * Test if the current principal is the guest user.
     * 
     * @return {@code true} if this principal is the guest user
     */
    protected boolean isGuest(JahiaPrincipal principal) {
        return (principal instanceof GuestGroup) || (principal instanceof JahiaUser)
                && JahiaUserManagerService.isGuest((JahiaUser) principal);
    }

    /**
     * Test if the current principal is the root system user.
     * 
     * @return {@code true} if this principal is the root system user
     */
    protected boolean isRoot(JahiaPrincipal principal) {
        return (principal instanceof JCRUser) && ((JCRUser) principal).isRoot();
    }

    /**
     * Sets the value for the <code>denyAll</code> property.
     * 
     * @param denyAll the denyAll to set
     * @see ${@link #isDenyAll()}
     */
    public void setDenyAll(boolean denyAll) {
        this.denyAll = denyAll;
    }

    /**
     * Sets the value for the <code>denyAllToGuest</code> property
     * 
     * @param denyAllToGuest the denyAllToGuest to set
     * @see ${@link #isDenyAllToGuest()}
     */
    public void setDenyAllToGuest(boolean denyAllToGuest) {
        this.denyAllToGuest = denyAllToGuest;
    }

    /**
     * Sets the value for the <code>grantAll</code> property.
     * 
     * @param grantAll the grantAll to set
     * @see ${@link #isGrantAll()}
     */
    public void setGrantAll(boolean grantAll) {
        this.grantAll = grantAll;
    }

    /**
     * Sets the value for the <code>grantAllToRoot</code> property.
     * 
     * @param grantAllToRoot the grantAllToRoot to set
     * @see ${@link #isGrantAllToRoot()}
     */
    public void setGrantAllToRoot(boolean grantAllToRoot) {
        this.grantAllToRoot = grantAllToRoot;
    }

}