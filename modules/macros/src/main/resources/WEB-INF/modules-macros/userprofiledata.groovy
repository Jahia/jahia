/**
 * Created with IntelliJ IDEA.
 * User: Damien GAILLARD
 * Date: 11/8/13
 * Time: 3:27 PM
 */
if(binding.variables.containsKey("param1")){
    if(binding.variables.containsKey("param2")){
        pathUserNode = currentUser.getLocalPath() + "/" + param1;
        try{
            print renderContext.getSite().getSession().getNode(pathUserNode).getProperty(param2).getString();
        }catch(Exception e){
            print "Unknown parameter : \"" + e.getMessage() + "\" !";
        }
    }
    else{
        if(currentUser.getUserProperty(param1) == null){
            print "Unknown parameter : \"" + param1 + "\" !";
        }else{
            print currentUser.getUserProperty(param1).getValue();
        }
    }
}else{
    print "<p>This macro require one or two parameter like : <br />" +
            "## userprofiledata(parameter) ##  or ## userprofiledata(parameter1, parameter2) ##</p>";
}