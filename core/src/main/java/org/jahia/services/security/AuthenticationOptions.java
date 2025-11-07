package org.jahia.services.security;

public class AuthenticationOptions {
    private final boolean sessionUsed;
    private final boolean rememberMeSupported;
    private final boolean triggerLoginEventEnabled;
    private final boolean updateCurrentLocaleEnabled;
    private final boolean updateUILocaleEnabled;
    private final boolean sessionAttributesPreserved;
    private final boolean sessionValidityCheckEnabled;

    AuthenticationOptions(Builder builder) {
        this.sessionUsed = builder.sessionUsed;
        this.rememberMeSupported = builder.rememberMeSupported;
        this.triggerLoginEventEnabled = builder.triggerLoginEventEnabled;
        this.updateCurrentLocaleEnabled = builder.updateCurrentLocaleEnabled;
        this.updateUILocaleEnabled = builder.updateUILocaleEnabled;
        this.sessionAttributesPreserved = builder.sessionAttributesPreserved;
        this.sessionValidityCheckEnabled = builder.sessionValidityCheckEnabled;
    }

    public boolean isStateful() {
        return sessionUsed;
    }

    public boolean isStateless() {
        return !sessionUsed;
    }

    public boolean shouldRememberMe() {
        return rememberMeSupported;
    }

    public boolean isTriggerLoginEventEnabled() {
        return triggerLoginEventEnabled;
    }

    public boolean isUpdateCurrentLocaleEnabled() {
        return updateCurrentLocaleEnabled;
    }

    public boolean isUpdateUILocaleEnabled() {
        return updateUILocaleEnabled;
    }

    public boolean areSessionAttributesPreserved() {
        return sessionAttributesPreserved;
    }

    public boolean isSessionValidityCheckEnabled() {
        return sessionValidityCheckEnabled;
    }

    public static final class Builder {
        private boolean sessionUsed = true;
        private boolean rememberMeSupported = true;
        private boolean updateCurrentLocaleEnabled = true;
        private boolean updateUILocaleEnabled = true;
        private boolean sessionAttributesPreserved = true;
        private boolean triggerLoginEventEnabled = true;
        private boolean sessionValidityCheckEnabled = true;

        private Builder() {
            // prevent external instantiation
        }

        /**
         * Enables session-based authentication (default).
         * When enabled, the authentication process:
         * <ul>
         *   <li>Creates or reuses an HTTP session</li>
         *   <li>Preserves specified session attributes across session invalidation</li>
         *   <li>Sets the authentication result in request attributes ({@code VALVE_RESULT})</li>
         *   <li>Supports "remember me" functionality via cookies</li>
         *   <li>Updates UI and current locale in the session based on user preferences</li>
         * </ul>
         *
         * @return this builder
         */
        public Builder stateful() {
            return sessionUsed(true);
        }

        /**
         * Disables session-based authentication (stateless mode).
         * When disabled, the authentication process:
         * <ul>
         *   <li>Does not create or modify HTTP sessions</li>
         *   <li>Automatically disables all session-related options (remember me, locale updates, session attributes, validity check)</li>
         *   <li>Only validates credentials and returns the authenticated user node</li>
         * </ul>
         *
         * @return this builder
         */
        public Builder stateless() {
            return sessionUsed(false);
        }

        /**
         * Sets whether the authentication process is session-based.
         *
         * @param sessionUsed {@code true} for session-based authentication, {@code false} for stateless
         * @return this builder
         * @see #stateful()
         * @see #stateless()
         */
        public Builder sessionUsed(boolean sessionUsed) {
            this.sessionUsed = sessionUsed;
            if (!sessionUsed) {
                // turn off the session-related options
                this.rememberMeSupported = false;
                this.updateCurrentLocaleEnabled = false;
                this.updateUILocaleEnabled = false;
                this.sessionAttributesPreserved = false;
                this.sessionValidityCheckEnabled = false;
            }
            return this;
        }

        /**
         * Enables "remember me" functionality (default: enabled).
         * When enabled:
         * <ul>
         *   <li>A persistent authentication cookie is created with a random key</li>
         *   <li>The key is stored in the user's properties for later validation</li>
         * </ul>
         * Only applicable when session-based authentication is enabled.
         *
         * @param flag {@code true} to enable, {@code false} to disable
         * @return this builder
         */
        public Builder shouldRememberMe(boolean flag) {
            this.rememberMeSupported = flag;
            return this;
        }

        /**
         * Enables triggering a login event after a successful authentication (default: enabled).<br>
         * The event is an implementation of {@link org.jahia.params.valves.BaseLoginEvent}.
         * The topic used to publish the event is {@code org/jahia/usersgroups/login/LOGIN}.
         *
         * @param flag {@code true} to enable, {@code false} to disable
         * @return this builder
         */
        public Builder triggerLoginEventEnabled(boolean flag) {
            this.triggerLoginEventEnabled = flag;
            return this;
        }

        /**
         * Enables updating the current locale in the session based on the user's preferred language (default: enabled).
         * Only applies if {@code considerPreferredLanguageAfterLogin} is enabled in settings.
         * Only applicable when session-based authentication is enabled.
         *
         * @param flag {@code true} to enable, {@code false} to disable
         * @return this builder
         */
        public Builder updateCurrentLocaleEnabled(boolean flag) {
            this.updateCurrentLocaleEnabled = flag;
            return this;
        }

        /**
         * Enables updating the UI locale in the session to the user's preferred locale after successful login (default: enabled).
         * Only applicable when session-based authentication is enabled.
         *
         * @param flag {@code true} to enable, {@code false} to disable
         * @return this builder
         */
        public Builder updateUILocaleEnabled(boolean flag) {
            this.updateUILocaleEnabled = flag;
            return this;
        }

        /**
         * Enables preserving specific session attributes when the session is invalidated and recreated during login (default: enabled).
         * Attributes to preserve are configured via {@code preserveSessionAttributesOnLogin} setting.
         * Only applicable when session-based authentication is enabled.
         *
         * @param flag {@code true} to enable, {@code false} to disable
         * @return this builder
         */
        public Builder sessionAttributesPreserved(boolean flag) {
            this.sessionAttributesPreserved = flag;
            return this;
        }

        /**
         * Sets whether to check the validity of the session during the authentication process,
         * by looking at the {@code j:invalidateSessionTime} property of the user node (default: enabled).
         *
         * @param flag {@code true} to enable session validity checking, {@code false} to disable it
         * @return this builder
         * @see org.jahia.services.content.decorator.JCRUserNode#getInvalidatedSessionTime()
         */
        public Builder sessionValidityCheckEnabled(boolean flag) {
            this.sessionValidityCheckEnabled = flag;
            return this;
        }

        public AuthenticationOptions build() {
            if (!sessionUsed) {
                // check the session-related options are not used when being stateless
                if (rememberMeSupported) {
                    throw new IllegalStateException("Cannot use both stateless and rememberMeEnabled");
                }
                if (updateCurrentLocaleEnabled) {
                    throw new IllegalStateException("Cannot use both stateless and updateCurrentLocaleEnabled");
                }
                if (updateUILocaleEnabled) {
                    throw new IllegalStateException("Cannot use both stateless and updateUILocaleEnabled");
                }
                if (sessionAttributesPreserved) {
                    throw new IllegalStateException("Cannot use both stateless and sessionAttributesPreserved");
                }
                if (sessionValidityCheckEnabled) {
                    throw new IllegalStateException("Cannot use both stateless and sessionValidityCheckEnabled");
                }
            }
            return new AuthenticationOptions(this);
        }

        /**
         * Creates a new builder with default values.
         * Default configuration:
         * <ul>
         *   <li>Session-based authentication: enabled</li>
         *   <li>Remember me: enabled</li>
         *   <li>Login events: enabled</li>
         *   <li>Current locale update: enabled</li>
         *   <li>UI locale update: enabled</li>
         *   <li>Session attributes preservation: enabled</li>
         *   <li>Session validity check: enabled</li>
         * </ul>
         *
         * @return a new builder instance
         */
        public static Builder withDefaults() {
            return new Builder();
        }
    }
}
