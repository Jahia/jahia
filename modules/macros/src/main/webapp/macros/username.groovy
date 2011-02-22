/**
 * Created by IntelliJ IDEA.
 * @author : rincevent
 * @since : JAHIA 6.1
 * Created : 21/12/10
 */
if (currentUser.username.trim().equals("guest")) {
    print "guest"
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