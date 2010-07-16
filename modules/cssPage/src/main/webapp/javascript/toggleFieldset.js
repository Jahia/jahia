$(function() {
    $("#buttonBody").click(function() {
        var options = {};
        $("#showBody").toggle('blind', options, 500);
        $.fancybox.resize();
        $.fancybox.center();
        return false;
    });
    $("#buttonLinks").click(function() {
        var options = {};
        $("#showLinks").toggle('blind', options, 500);
        $.fancybox.resize();
        $.fancybox.center();
        return false;
    });
    $("#buttonTitles").click(function() {
        var options = {};
        $("#showTitles").toggle('blind', options, 500);
        $.fancybox.resize();
        $.fancybox.center();
        return false;
    });
    $("#buttonPageColors").click(function() {
        var options = {};
        $("#showPageColors").toggle('blind', options, 500);
        $.fancybox.resize();
        $.fancybox.center();
        return false;
    });
    $("#buttonBackgroundImages").click(function() {
        var options = {};
        $("#showBackGroundImages").toggle('blind', options, 500);
        $.fancybox.resize();
        $.fancybox.center();
        return false;
    });
});