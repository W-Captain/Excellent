$(function(){
	common.serActive(3);
	common.addCickToNav(recruitEdit.showContent);
	recruitEdit.getEditor();
	recruitEdit.pubRecuit();
});

var recruitEdit = {};

(function(page){
	var editor;
	page.getEditor = function(){
		KindEditor.ready(function(K) {
			editor = K.create('textarea[name="content"]', {
				allowFileManager : true
			});
		});
	};
	
	page.showContent = function(i){
		var pubRecuit = $("#pubRecuit");
		var delEditRecuit = $("#delEditRecuit");
		switch(i){
		case 0:{
			pubRecuit.show();
			delEditRecuit.hide();
		}break;
		case 1:{
			pubRecuit.hide();
			delEditRecuit.show();
			page.getDynamicList();
		}break;
		}
	};
	
	//发布招新信息
	page.pubRecuit = function(){
		$("#pubRecuitBtn").click(function(){
			var title = $("#title").val();
			var content = editor.html();
			if(title == "" || content == "" || clas == ""){
				alert("请完善信息！");
				return;
			}
			console.log(title,content);
		});
	};
	
	//获取班级动态列表
	page.getDynamicList = function(){
		$.getJSON(
			"/Excellent/pages/json/dynamicList.json",
			/*{
				rowNum:15,
				nowPage:1
			},*/
			function(data){
				if(data.success === true){
					var recuits = data.result;
					var html = "";
					var url = "/Excellent/pages/DynamicsInfo.html";
					for(var i = 0,len = recuits.length; i < len; i++){
						html += '<tr>'
							 +  '<td class = "titleWidth" onclick = "pageToNew(\''+url+'\','+recuits[i].recuritId+')">'+recuits[i].recuritTitle+'</td>'
			  				 +	'<td>'+recuits[i].pubTime+'</td>'
			  				 +	'<td><button value = "'+recuits[i].recuritId+'" type="button" class="btn btn-default btn-xs">编辑</button></td>'
			  				 +	'<td><button value = "'+recuits[i].recuritId+'" type="button" class="btn btn-default btn-xs">删除</button></td>'
							 +  '</tr>';
					}
					$("#recuitList").html(html);
					page.addClickRecuits();
				}else{
					alert("获取班级动态列表失败，请刷新重试！");
				}
			}
		);
	};
	
	//给动态列表添加点击事件
	page.addClickRecuits = function(){
		$("#recuitList").delegate('td','click',function(){
			var obj = $($(this).find('button'));
			if(obj.length === 0){
				return;
			}
			var name= obj.html();
			var val = obj.val();
			if(name === "编辑"){
				pageToNew("/Excellent/pages/editDynamics.html",val);
			}else if(name === "删除"){
				alert(val);
			}
		});
	};
})(recruitEdit);
