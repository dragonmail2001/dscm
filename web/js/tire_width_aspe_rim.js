

$(function () {
	var tireWidthArray = ['145','155','165','175','185','195','196','205','210','215','225','235','245','255','265','275','285','295','30','305','30X','30x9.5','31','315','31X','31X10','31x10.5','32,325','32X','32x11.5','33','335','33X','33x12.5','35,37X','55','650','700'];

	var tireAspectratioArray = ['790','105','85','80','75','70','65','60','55Z','55','50Z','50','45','40','35','30','25','16','12.5','11.5','10.5','9.5'];

	var tireRimArray = ['V','540','45','28','26','24','23','22','21','20','19','18','17','16C','16.5','16','15C','15','14C','14','13LT','13C','13','12C','12','11'];
	
	var bindWARFun = function(){
		var hidden_tireWidth = $("input[name=hidden_tireWidth]").val();
		var hidden_tireAspectratio = $("input[name=hidden_tireAspectratio]").val();
		var hidden_tireRim = $("input[name=hidden_tireRim]").val();
		var tireWidthHtml = '';
		tireWidthArray.forEach(function(w){
			var selected = '';
			if(""!=hidden_tireWidth&&hidden_tireWidth==w){
				selected = 'selected="selected"';
			}
			tireWidthHtml+='<option value="'+w+'" '+selected+'>'+w+'</option>';
		});
		$("select[name=tireWidth]").html(tireWidthHtml);
		
		var tireAspectratioHtml = '';
		tireAspectratioArray.forEach(function(a){
			var selected = '';
			if(""!=hidden_tireAspectratio&&hidden_tireAspectratio==a){
				selected = 'selected="selected"';
			}
			tireAspectratioHtml+='<option value="'+a+'" '+selected+'>'+a+'</option>';
		});
		$("select[name=tireAspectratio]").html(tireAspectratioHtml);
		
		var tireRimHtml = '';
		tireRimArray.forEach(function(r){
			var selected = '';
			if(""!=hidden_tireRim&&hidden_tireRim==r){
				selected = 'selected="selected"';
			}
			tireRimHtml+='<option value="'+r+'" '+selected+'>'+r+'</option>';
		});
		$("select[name=tireRim]").html(tireRimHtml);
	};
	bindWARFun();
});