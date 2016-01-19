/*
 * 树形页面公共方法
 * Author: fsren
 * Date: 2014-11-14
 * */

var JJG = JJG || {};

// 搜索基础方法，依赖于 jquery
JJG.tree = (function ($) {
	var log = function (msg) {
		window.console && console.info && console.info(msg);
	};

	var init_height = function () {
		(function () {
			var _height = $(window).height();
			$(document.body).css('height', _height);
			$('.tree_view').css('height', _height - 37);
		})();
	};

	var bind_branch = function () {
		$('.branch').click(function (event) {
			event.stopPropagation();
			var _this = $(this);
			if (_this.hasClass('branch-notlast-collapse')) {
				_this.removeClass('branch-notlast-collapse').addClass('branch-notlast-expand');
				_this.parent().next().show();
			} else {
				_this.removeClass('branch-notlast-expand').addClass('branch-notlast-collapse');
				_this.parent().next().hide();
			}
			if (_this.hasClass('branch-last-collapse')) {
				_this.removeClass('branch-last-collapse').addClass('branch-last-expand');
				_this.parent().next().show();
			} else {
				_this.removeClass('branch-last-expand').addClass('branch-last-collapse');
				_this.parent().next().hide();
			}
		});
	};

	var bind_tree = function () {
		url_key = $('#DetailFrame').attr('src');
		$('#tree1 dt').click(function () {
			var _this = $(this),
			_url = _this.attr('url');
			$('#tree1 dt').removeClass('selected');
			_this.addClass('selected');
			
			if (typeof _url != 'undefined') {
				if(url_key != _url){
					$('#DetailFrame').attr('src', _url);
					url_key = _url;
				}
			} else if (_this.next().find('dl').length > 0) {
				_this.find('.branch').click();
			}else{
				$.message("warn", "当前菜单未设置地址");
			}

		});
	},
	url_key;

	var init = function () {
		init_height();
		bind_branch();
		bind_tree();
	};

	//返回方法供页面使用
	return {
		init : init
	};

})(jQuery);

$(function () {
	JJG.tree.init();
});
