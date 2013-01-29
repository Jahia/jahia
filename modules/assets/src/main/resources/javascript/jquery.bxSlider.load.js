$(document).ready(function(){

			$('#example1').bxSlider({
				mode: 'slide',
				speed: 250,
				wrapper_class: 'example1_container'
			});

			$('#example2').bxSlider({
				mode: 'slide',
				auto: 'true',
				controls: false,
				speed: 1500,
				pause: 3500,
				width: 869,
				wrapper_class: 'example2_container'
			});

			$('#example3').bxSlider({
				mode: 'fade',
				speed: 1000,
				pause: 5000,
				auto: true,
				controls: true,
				autoDirection: 'right',
				nextText: 'next image',
				prevText: 'previous image',
				width: 307,
				wrapper_class: 'example3_container'
			});

			$('#example4').bxSlider({
				mode: 'ticker',
				speed: 7000,
				width: 500,
				wrapper_class: 'example4_container'
			});

		});