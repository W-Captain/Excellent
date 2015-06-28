$(function(){
	common.serActive(3,0);
	recuitEdit.getEditor();
	recuitEdit.saveRecuit();
	
	$(".form_datetime").datetimepicker({
        format: "yyyy-mm-dd",
        weekStart:1,
        autoclose:true,
        minView:"month",
        todayBtn:true,
        todayHighlight:true
    });
});

var recuitEdit = {};

(function(page){
	var editor;
	page.getEditor = function(){
		KindEditor.ready(function(K) {
			editor = K.create('textarea[name="content"]', {
				allowFileManager : true
			});
			page.getRecuitInfo();
		});
	};
	
	//根据动态Id 获取 招新内容
	page.getRecuitInfo = function(){
		atyIds = location.hash.split("#");
		$.getJSON(
			"/Excellent/news/showNewsDetail",
			{
				atyId:atyIds[1]
			},
			function(data){
				if(data.success){
					var recuit = data.result.details;
					$("#title").val(recuit.title); //动态标题
					$("#recruitDate").val(recuit.happen_time);
					editor.html(recuit.content); //动态内容
				}else{
					alert("获取招新信息失败！");
				}
			}
		);
	};
	
	//保存编辑
	page.saveRecuit = function(){
		$("#saveRecuit").click(function(){
			var title = $("#title").val(); //动态标题
			var content = editor.html(); //动态内容
			var recruitDate = $("#recruitDate").val();
			var recruitId = location.hash.split("#")[1];
			//console.log(title,content);
			if(title == "" || content == ""){
				alert("请完善信息！");
				return;
			}
			$.ajax({
				url:"/Excellent/news/mergeNews",
				type:"post",
				data:{
					"news.id":recruitId,
					"news.type":2,
					"news.title": title,
					"news.content": content,
					"news.happen_time": recruitDate
				},
				success:function(data){
					if(data.success === true){
						alert("保存成功！");
						window.open("/Excellent/pages/recruitInfo.html#" + recruitId);
					}else{
						alert("保存失败，请重新尝试！");
					}
				},
				error:function(){
					alert("保存请求失败！");
				}
			});
		});
	};
})(recuitEdit);