/**
 * Created with IntelliJ IDEA.
 * User: Damien GAILLARD
 * Date: 11/8/13
 * Time: 9:28 AM
 */
if(currentNode.hasProperty("j:keywords")){
    keywords = currentNode.getProperty("j:keywords").getValues();
    keywordsSize =  keywords.size();
    for(int i = 0 ; i < keywordsSize ; i++){
        print keywords.value[i].getString();
        if(keywordsSize > 0 && i < keywordsSize-1){
            print ", ";
        }
    }
}else{
    print "Defined Keyword(s) before use !";
}


