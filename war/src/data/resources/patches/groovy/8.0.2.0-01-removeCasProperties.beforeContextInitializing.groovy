// Remove CAS related properties
// see https://jira.jahia.org/browse/TECH-197

import java.util.regex.Pattern
import org.jahia.settings.JahiaPropertiesUtils

import static org.jahia.settings.JahiaPropertiesUtils.RemoveOperation

final String PROP_ENABLED = "auth.cas.enabled"
final String PROP_SERVER_URL_PREFIX = "auth.cas.serverUrlPrefix"
final String PROP_LOGIN_URL = "auth.cas.loginUrl"
final String PROP_LOGOUT_URL = "auth.cas.logoutUrl"

final String DESC_ENABLED_1 = "######################################################################"
final String DESC_ENABLED_2 = "### CAS Authentication config ########################################"
final String DESC_ENABLED_3 = "######################################################################"
final String DESC_ENABLED_4 = "# Enables the CAS authentication valve"

final String DESC_SERVER_URL_PREFIX = "# Specifies the URL prefix of the CAS server"
final String DESC_LOGIN_URL = "# Specifies the redirect URL to the CAS server for login"
final String DESC_LOGOUT_URL = "# Specifies the logout URL to invalidate the user session on the CAS server"

JahiaPropertiesUtils.removeEntry([
        new RemoveOperation(PROP_ENABLED, RemoveOperation.Type.EXACT_BLOCK, DESC_ENABLED_1, DESC_ENABLED_2, DESC_ENABLED_3, DESC_ENABLED_4),
        new RemoveOperation(PROP_ENABLED, RemoveOperation.Type.REGEXP_LINE, ".*" + Pattern.quote(PROP_ENABLED) + ".*"),
        new RemoveOperation(PROP_SERVER_URL_PREFIX, RemoveOperation.Type.EXACT_BLOCK, DESC_SERVER_URL_PREFIX),
        new RemoveOperation(PROP_SERVER_URL_PREFIX, RemoveOperation.Type.REGEXP_LINE, ".*" + Pattern.quote(PROP_SERVER_URL_PREFIX) + ".*"),
        new RemoveOperation(PROP_LOGIN_URL, RemoveOperation.Type.EXACT_BLOCK, DESC_LOGIN_URL),
        new RemoveOperation(PROP_LOGIN_URL, RemoveOperation.Type.REGEXP_LINE, ".*" + Pattern.quote(PROP_LOGIN_URL) + ".*"),
        new RemoveOperation(PROP_LOGOUT_URL, RemoveOperation.Type.EXACT_BLOCK, DESC_LOGOUT_URL),
        new RemoveOperation(PROP_LOGOUT_URL, RemoveOperation.Type.REGEXP_LINE, ".*" + Pattern.quote(PROP_LOGOUT_URL) + ".*")
] as RemoveOperation[])
