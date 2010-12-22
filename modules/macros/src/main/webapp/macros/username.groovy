/**
 * Created by IntelliJ IDEA.
 * @author : rincevent
 * @since : JAHIA 6.1
 * Created : 21/12/10
 */
if (currentUser.username.trim().equals("guest")) {
    print "guest"
} else {
    print(currentUser.getProperty("j:firstName") + " " + currentUser.getProperty("j:lastName"))
}