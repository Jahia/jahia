import org.jahia.data.viewhelper.principal.PrincipalViewHelper

if (currentUser.username.trim().equals("guest")) {
    print PrincipalViewHelper.getUserDisplayName(currentUser.username.trim());
} else {
    String property1 = currentUser.getProperty("j:firstName")
    if (property1 != null)
        print(property1.capitalize() + " ");
    String property2 = currentUser.getProperty("j:lastName")
    if (property2 != null)
        print(property2.capitalize())
    if (property1 == null && property2 == null)
        print(currentUser.getUsername().capitalize())
}