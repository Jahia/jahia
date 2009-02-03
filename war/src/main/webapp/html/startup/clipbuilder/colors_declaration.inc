<%!
static private final String colors[]     = {"blue", "orange", "green", "red", ""};
static private final  String colorCodes[] = {"#006699", "#FF7D00", "#2DA02D", "#d50d1f", "#006699"};
static private final  String colorTopPicts[] = {"top_menu_left_blue_on.gif", "top_menu_left_orange_on.gif",
                       "top_menu_left_green_on.gif", "top_menu_left_red_on.gif", "top_menu_left_blue_on.gif"};
static private final String colorLeftPicts[] = {"leftmenuon_blue.gif", "leftmenuon_orange.gif",
                            "leftmenuon_green.gif", "leftmenuon_red.gif", "leftmenuon_blue.gif"};

public String getInfo(String color, String array[])  {
    String str = "";
    for (int  i = 0; i < colors.length; i++){
    	//if (color.equals(ResourceBundleMarker.drawMarker(resBundleID,colors[i],""))){
        if (color.equals(colors[i])){
            str = array[i];
            break;
        }
    }
    return str;
}

public String getColorCode(String color) {
    String str = getInfo(color,colorCodes);
    return str;
}

public String getColorTopPict(String color) {
    String str = getInfo(color,colorTopPicts);
    return str;
}

public String getColorLeftPict(String color) {
    String str = getInfo(color,colorLeftPicts);
    return str;
}

%>