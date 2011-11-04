import org.jahia.services.pwdpolicy.*;

def log = log;

log.info("Start updating password policy rules (removing periodical rules)...");

boolean updated = false;
JahiaPasswordPolicy policy = JahiaPasswordPolicyService.getInstance().getDefaultPolicy();
for (Iterator ruleIterator = policy.getRules().iterator(); ruleIterator.hasNext();) {
    JahiaPasswordPolicyRule rule = ruleIterator.next();
    if (rule.isPeriodical()) {
        log.info("Removing periodical rule: " + rule.getName());
        ruleIterator.remove();
        updated = true;
    }
}

if (updated) {
    JahiaPasswordPolicyService.getInstance().updatePolicy(policy);
}

log.info("... done updating password policy.");