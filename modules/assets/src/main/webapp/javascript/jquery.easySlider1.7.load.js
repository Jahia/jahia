		$(document).ready(function(){	
			$("#slider").easySlider({
				auto: true,
				continuous: true,
				nextId: "slider1next",
				prevId: "slider1prev",
				speed: 200,
				pause: 8000
			});
			$("#slider2").easySlider({ 
				numeric: true,
				auto: true,
				continuous: true,
				speed: 200,
				pause: 4000
			});
		});
		


/*The following options are configurable:

prevId:                 'prevBtn',
prevText:               'Previous',
nextId:                 'nextBtn',
nextText:               'Next',
controlsShow:           true,
controlsBefore:         '',
controlsAfter:          '',
controlsFade:           true,
firstId:                'firstBtn',
firstText:              'First',
firstShow:              false,
lastId:                 'lastBtn',
lastText:               'Last',
lastShow:               false,
vertical:               false,
speed:                  800,
auto:                   false,
pause:                  2000,
continuous:             false,
numeric:                false,
numericId:              'controls'*/
