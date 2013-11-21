/**
 * Created with IntelliJ IDEA.
 * User: Damien GAILLARD
 * Date: 11/8/13
 * Time: 3:14 PM
 */
if(binding.variables.containsKey("param1") != false){
    if(param1 == "contentPath" || param1 == "contentId" || param1 == "full"){
        if(param1 == "contentPath"){
            print "<p>Content path : " + currentNode.getPath() + "</p>";
        }
        if(param1 == "contentId"){
            print "<p>Content id : " + currentNode.getIdentifier() + "</p>";
        }
        if(param1 == "full"){
            print "<p>Content path : " + currentNode.getPath() + "</p>";
            print "<p>Content id : " + currentNode.getIdentifier() + "</p>";
        }
    }else{
        print "<p>Unknown parameter ! Parameter should be \"contentPath\", \"contentId\" or \"full\" without quote !</p>";
    }
}else{
    print "<p>This macro require a parameter like, ## devmode(parameter) ##</p>";
}