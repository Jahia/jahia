/**
*
*
* bxSlider: Content slider / fade / ticker using the jQuery javascript library.
*
* Author: Steven Wanderski
* Email: wandoledzep@gmail.com
* URL: http://bxslider.com
*
*
**/

jQuery.fn.bxSlider = function(options){

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Declare variables and functions
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	var defaults = {
		mode: 'slide',
		speed: 500,
		auto: false,
		auto_direction: 'left',
		pause: 2500,
		controls: true,
		prev_text: 'prev',
		next_text: 'next',
		width: $(this).children().width(),
		prev_img: '',
		next_img: '',
		ticker_direction: 'left',
		wrapper_class: 'container'
	};

	options = $.extend(defaults, options);

	if(options.mode == 'ticker'){
		options.auto = true;
	}

	var $this = $(this);

	var $parent_width = options.width;
	var current = 0;
	var is_working = false;
	var child_count = $this.children().size();
	var i = 0;
	var j = 0;
	var k = 0;

	function animate_next(){

		is_working = true;

		$this.animate({'left':'-' + $parent_width * 2 + 'px'}, options.speed, function(){

			$this.css({'left':'-' + $parent_width + 'px'}).children(':first').appendTo($this);

			is_working = false;

		});

	}

	function animate_prev(){

		is_working = true;

		$this.animate({'left': 0}, options.speed, function(){

			$this.css({'left':'-' + $parent_width + 'px'}).children(':last').insertBefore($this.children(':first'));

			is_working = false;

		});

	}

	function fade(direction){

		if(direction == 'next'){

			var last_before_switch = child_count - 1;
			var start_over = 0;
			var incr = k + 1;

		}else if(direction == 'prev'){

			var last_before_switch = 0;
			var start_over = child_count -1;
			var incr = k - 1;

		}

		is_working = true;

		if(k == last_before_switch){

			$this.children().eq(k).fadeTo(options.speed, 0);
			$this.children().eq(start_over).fadeTo(options.speed, 1, function(){

			is_working = false;

			k = start_over;

			});

		}else{

			$this.children().eq(k).fadeTo(options.speed, 0);
			$this.children().eq(incr).fadeTo(options.speed, 1, function(){

			is_working = false;

			k = incr;

			});

		}

	}

	function add_controls(){

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Check if user selected images to use for next / prev
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////

		if(options.prev_img != '' || options.next_img != ''){

			$this.parent().append('<a class="slider_prev" href=""><img src="' + options.prev_img + '" alt=""/></a><a class="slider_next" href=""><img src="' + options.next_img + '" alt="" /></a>');

		}else{

			$this.parent().append('<a class="slider_prev" href="">' + options.prev_text + '</a><a class="slider_next" href="">' + options.next_text + '</a>');

		}

		$this.parent().find('.slider_prev').css({'float':'left', 'outline':'0'});
		$this.parent().find('.slider_next').css({'float':'right', 'outline':'0'});


		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Accomodate padding-top for controls when elements are absolutely positioned (only in fade mode)
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////

		if(options.mode == 'fade'){

			$this.parent().find('.slider_prev').css({'paddingTop' : $this.children().height()})
			$this.parent().find('.slider_next').css({'paddingTop' : $this.children().height()})

		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// Actions when user clicks next / prev buttons
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////

		$this.parent().find('.slider_next').click(function(){

			if(!is_working){

				if(options.mode == 'slide'){

					animate_next();

					if(options.auto){

						clearInterval($.t);

						$.t = setInterval(function(){animate_next();}, options.pause);

					}

				}else if(options.mode == 'fade'){

					fade('next');

					if(options.auto){

						clearInterval($.t);

						$.t = setInterval(function(){fade('next');}, options.pause);

					}


				}

			}

			return false;

		});

		$this.parent().find('.slider_prev').click(function(){

			if(!is_working){

				if(options.mode == 'slide'){

					animate_prev();

					if(options.auto){

						clearInterval($.t);

						$.t = setInterval(function(){animate_prev();}, options.pause);

					}

				}else if(options.mode == 'fade'){

					fade('prev');

					if(options.auto){

						clearInterval($.t);

						$.t = setInterval(function(){fade('prev');}, options.pause);

					}

				}

			}

			return false;

		});

	}


	function ticker() {

		if(options.ticker_direction == 'left'){

			$this.animate({'left':'-' + $parent_width * 2 + 'px'}, options.speed, 'linear', function(){

				$this.css({'left':'-' + $parent_width + 'px'}).children(':first').appendTo($this);

				ticker();

			});

		}else if(options.ticker_direction == 'right'){

			$this.animate({'left': 0}, options.speed, 'linear', function(){

				$this.css({'left':'-' + $parent_width + 'px'}).children(':last').insertBefore($this.children(':first'));

				ticker();

			});

		}

	}



	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create content wrapper and set CSS
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////

	$this.wrap('<div class="' + options.wrapper_class + '"></div>');

	//console.log($this.parent().css('paddingTop'));

	if(options.mode == 'slide' || options.mode == 'ticker'){

		$this.parent().css({
			'overflow' : 'hidden',
			'position' : 'relative',
			'width' : options.width + 'px'
		});

		$this.css({
			'width' : '999999px',
			'position' : 'relative',
			'left' : '-' + $parent_width + 'px'
		});

		$this.children().css({
			'float' : 'left',
			'width' : $parent_width
		});

		$this.children(':last').insertBefore($this.children(':first'));

	}else if(options.mode == 'fade'){

		$this.parent().css({
			'overflow' : 'hidden',
			'position' : 'relative',
			'width' : options.width + 'px'
			//'height' : $this.children().height()
		});

		if(!options.controls){
			$this.parent().css({'height' : $this.children().height()});
		}

		$this.children().css({
			'position' : 'absolute',
			'width' : $parent_width,
			'listStyle' : 'none',
			'opacity' : 0
		});

		$this.children(':first').css({
			'opacity' : 1
		});

	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Check if user selected "auto"
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////

	if(!options.auto){

		add_controls();

	}else{

		if(options.mode == 'ticker'){

			ticker();

		}else{

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Set a timed interval
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////

			if(options.mode == 'slide'){

				if(options.auto_direction == 'left'){

					$.t = setInterval(function(){animate_next();}, options.pause);

				}else if(options.auto_direction == 'right'){

					$.t = setInterval(function(){animate_prev();}, options.pause);

				}

			}else if(options.mode == 'fade'){

				if(options.auto_direction == 'left'){

					$.t = setInterval(function(){fade('next');}, options.pause);

				}else if(options.auto_direction == 'right'){

					$.t = setInterval(function(){fade('prev');}, options.pause);

				}

			}

			if(options.controls){

				add_controls();

			}

		}

	}

}
















