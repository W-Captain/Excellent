$(function(){
	common.serActive(0,0);
	common.getClasses(["#classSelect"]);
	common.addCickToNav(classIntroMngr.showContent);
	classIntroMngr.getEditor();
	classIntroMngr.pubNews();
	
	$(".form_datetime").datetimepicker({
        format: "yyyy-mm-dd",
        weekStart:1,
        autoclose:true,
        minView:"month",
        todayBtn:true,
        todayHighlight:true
    });
	
	
	$("#logout").click(function(){
		classIntroMngr.clickLogout();
	});
});

var classIntroMngr = {};

(function(page){
	var editor;
	page.getEditor = function(){
		var options = {
		    basePath:'kindeditor-4.1.10/',
			allowFileManager:false,
			uploadJson:"/Excellent/file/uploadfile",
			imageUploadJson:"/Excellent/file/uploadImg"	,
			afterUpload:function(url){
				$('textarea[name="content"]').innerHtml='<img src='+url+'>';
			},
		};
		KindEditor.ready(function(K) {
			editor = K.create('textarea[name="content"]', options);
		});
	};
	
	page.showContent = function(i){
		var pubDynamicCon = $("#pubDynamicCon");
		var delEditDinamics = $("#delEditDinamics");
		switch(i){
		case 0:{
			pubDynamicCon.show();
			delEditDinamics.hide();
		}break;
		case 1:{
			pubDynamicCon.hide();
			delEditDinamics.show();
			page.getDynamicList.run(1);
		}break;
		}
	};
	
	//发布动态
	page.pubNews = function(){
		$("#pubNews").click(function(){
			var title = $("#title").val(); //动态标题
			var content = editor.html(); //动态内容
			var classId = $("#classSelect").val(); //班级
			var dynamicDate = $("#dynamicDate").val(); //动态时间
			if(title == "" || content == "" || classId == "" || dynamicDate == ""){
				alert("请完善信息！");
				return;
			}
			$.ajax({
				url:"/Excellent/news/writeNews",
				type:"post",
				dataType:"json",
				data:{
					type:1,
					title:title,
					content:content,
					class_id:classId,
					happen_time:dynamicDate
				},
				success:function(data){
					if(data.success === true){
						alert("发布成功！");
						window.open("/Excellent/pages/dynamicInfo.html#" + data.result.id);
					}else{
						alert("发布失败，请重新尝试！");
					}
				},
				error:function(){
					alert("发布请求失败！");
				}
			});
		});
	};
	
	
	//获取班级动态列表
	page.getDynamicList = {
		rowNum:15,
		pageNum:1,
		loadPage:false,
		run:function(index){
			$.getJSON(
				"/Excellent/news/showClassNewsList",
				{
					rowNum:this.rowNum,
					nowPage:index
				},
				function(data){
					if(data.success === true){
						var dymList = data.result.details;
						var html = "";
						var url = "/Excellent/pages/dynamicInfo.html";
						for(var i = 0,len = dymList.length; i < len; i++){
							html += '<tr>'
								 +  '<td class = "titleWidth" onclick = "pageToNew(\''+url+'\','+dymList[i].id+')">'+dymList[i].title+'</td>'
				  				 +	'<td>'+dymList[i].happen_time+'</td>'
				  				 +	'<td><button value = "'+dymList[i].id+'" type="button" class="btn btn-default btn-xs">编辑</button></td>'
				  				 +	'<td><button value = "'+dymList[i].id+'" type="button" class="btn btn-default btn-xs">删除</button></td>'
								 +  '</tr>';
						}
						$("#dynamicList").html(html);
						page.addClickDymList();
						//加载分页
						if(page.getDynamicList.loadPage == false){
							page.getDynamicList.pageNum = data.result.totalPage;
							loadDevidePage(page.getDynamicList.pageNum,10,1,data.result.totalRow,page.getDynamicList);
							page.getDynamicList.loadPage = true;//分页已完成
						}
					}else{
						alert("获取班级动态列表失败，请刷新重试！");
					}
				}
			);
		}
	};
	
	//给动态列表添加点击事件
	page.addClickDymList = function(){
		$("#dynamicList").delegate('td','click',function(){
			var obj = $($(this).find('button'));
			if(obj.length === 0){
				return;
			}
			var name= obj.html();
			var val = obj.val();
			if(name === "编辑"){
				pageToNew("/Excellent/pages/dynamicEditMngr.html",val);
			}else if(name === "删除"){
				if(confirm("确认删除吗？") === false)
					return;
				$.ajax({
					url:"/Excellent/news/deleteNews",
					data:{
						atyId:val
					},
					type:"post",
					success:function(data){
						if(data.success === true){
							alert("删除成功！");
							location.reload();
						}else{
							alert("删除失败！");
						}
					},
					error:function(){
						alert("删除请求失败！");
					}
				});
			}
		});
		
		
	};
	
	page.clickLogout = function(){		
		$.ajax({
			url:"/Excellent/logout",
			type:"post",
			success:function(data){
				if(data.success === true){
					alert("退出成功！");
					location.href="/Excellent/pages/home.html";
				}else{
					alert("退出失败！");
				}
			},
			error:function(msg){
				alert("网络超时！");
			}
		});
	}
	
})(classIntroMngr);

