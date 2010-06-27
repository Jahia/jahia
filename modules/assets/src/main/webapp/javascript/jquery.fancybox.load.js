$(document).ready(function() {
    $("a.zoom").fancybox();

    $("a.zoom1").fancybox({
        'overlayOpacity'    :    0.7,
        'overlayColor'        :    '#FFF'
    });

    $("a.zoom2").fancybox({
        'zoomSpeedIn'        :    500,
        'zoomSpeedOut'        :    500
    });
});