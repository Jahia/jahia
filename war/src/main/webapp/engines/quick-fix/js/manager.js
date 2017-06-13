$(document).ready(function(){

    $("body").on("click", "#JahiaGxtManagerBottomTabs .x-panel-footer", function(){
        $("body").attr("data-INDIGO-INLINE-EDIT-ENGINE", function(index, attr){
            return (attr == "on") ? "" : "on";
        });
    })


    $("body").on("click", "#JahiaGxtManagerLeftTree .x-tab-panel-header", function(){
        $("body").attr("data-INDIGO-SEARCH", "on");
        $(this).find("li:nth-child(2) em").trigger("click");
    })

    $("body").on("click", "#JahiaGxtManagerLeftTree .x-panel > div.x-accordion-hd", function(){
        $("body").attr("data-INDIGO-SEARCH", "");
    })



});