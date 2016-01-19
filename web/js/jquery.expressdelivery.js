﻿(function($) {
    $.fn.Delivery = function(o) {
		var log = function(msg){window.console && console.info && console.info(msg);};
		var option = $.extend({
			url:'http://www.kuaidi100.com/query',
			autourl:'http://www.kuaidi100.com/autonumber/auto',
            type: null,
            postid: null,
			checkClass:'check',
			firstClass:'first',
			waitClass:'wait',
			template:'<li>{date} -- {desc} -- {class}</li>'
        },o || {}),$_this = $(this);
		
		get = function(){
			$.ajax({
				type		:"get",
				async		:true,
				url			:option.url,
				dataType	:"jsonp",
				jsonp		:"callback",
				data		:{type:option.type,postid:option.postid},
				success		:function(json){
					if(json.status =='200'){
						var context = json.data.reverse(),contextLength = context.length,check = json.ischeck == '1';
						for(var i = 0;i < contextLength;i++){
							var class_ = '';
							if(i == 0)
								class_ = option.firstClass;
							else if(i == contextLength -1)
								class_ = check?option.checkClass:option.waitClass;
							$_this.append(option.template.replace(/{date}/g,context[i]['time']).replace(/{desc}/g,context[i]['context']).replace('{class}',class_));
						}
					}else{
						$_this.append(json.message);
					}
				}
			});
		};
		get();
    };
})(jQuery);