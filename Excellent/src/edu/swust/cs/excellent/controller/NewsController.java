package edu.swust.cs.excellent.controller;


import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;








import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.ehcache.CacheName;
import com.jfinal.plugin.ehcache.EvictInterceptor;
import com.jfinal.plugin.ehcache.IDataLoader;

import edu.swust.cs.excellent.authorized.Authority;
import edu.swust.cs.excellent.authorized.AuthorityInterceptor;
import edu.swust.cs.excellent.authorized.LoginInterceptor;
import edu.swust.cs.excellent.cache.MyCacheName;
import edu.swust.cs.excellent.cache.MyEvictInterceptor;
import edu.swust.cs.excellent.config.Constant;
import edu.swust.cs.excellent.model.News;
import edu.swust.cs.excellent.service.inter.IEditNews;

import com.jfinal.plugin.spring.Inject;

public class NewsController extends CommonController {

	@Inject.BY_TYPE
	private IEditNews editNewsImpl;
	public void showClassNewsList(){
		int pageNum = getParaToInt("nowPage",1);
		int numPerPage = getParaToInt("rowNum",10);
		String classNum=getPara("calssNum","");
		int type = getParaToInt("type",1);
		String para=type+classNum;
		if (type==1){
			Page<News> page = CacheKit.get("news_cache",type+"-"+pageNum+"-"+numPerPage ,
					new IDataLoader(){
				public Object load() {    
					Page<News> news = editNewsImpl.getList(pageNum,numPerPage);  
					if (news==null)
						return null;
					for (News p:news.getList()){
						String  content=p.getStr("content");
						if (content.trim().equals(""))
							continue;
						String summary=content.substring(0,Math.min(50, content.length()-1))+"...";
						p.put("summary",summary);
						p.remove("content");
					}
					return news;
				}}); 

			//Page<News> page=editNewsImpl.getList(pageNum,numPerPage);
			renderP(page,"details");
		}else {
			Page<News> page = CacheKit.get("news_cache",type+"-"+pageNum+"-"+numPerPage ,
					new IDataLoader(){
				public Object load() {    
					Page<News> news = editNewsImpl.getList(pageNum,numPerPage,para);
					if (news==null)
						return null;
					for (News p:news.getList()){
						String  content=p.getStr("content");
						if (content.trim().equals(""))
							continue;
						String summary=content.substring(0,Math.min(50, content.length()-1))+"...";
						p.put("summary",summary);
						p.remove("content");
					}
					return news;
				}}); 

			//Page<News> page=editNewsImpl.getList(pageNum,numPerPage);
			renderP(page,"details");
		}
	}

	public void showNewsDetail(){
		int id = getParaToInt("atyId");
		News news = CacheKit.get("news_cache", id) ;
		if (news == null){
			news = editNewsImpl.getDetail(id);
			CacheKit.put("news_cache", id, news);
		}
		renderJ("details", news);
	}

	@Authority({
		Constant.AUTHORITY_ADMIN,Constant.AUTHORITY_MONITOR,Constant.AUTHORITY_SECRETARY,
		Constant.AUTHORITY_LIFE_REP,Constant.AUTHORITY_ACADEMIC_REP,Constant.AUTHORITY_SPORTS_REP
	})
	@Before({LoginInterceptor.class,AuthorityInterceptor.class,MyEvictInterceptor.class})
	@MyCacheName({"index_cache","news_cache"})
	public void writeNews(){
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		String time = format.format(calendar.getTime());
		News  news = new News().set("title", getPara("title ",""))
				.set("type", getParaToInt("type",1))
				.set("content", getPara("content ",""))
				.set("author", getName())
				.set("happen_time", getPara("happen_time",time))
				.set("class_id", getPara("class_id",""));
		boolean r=editNewsImpl.add(news);
		if  (r){
			add("id",news.getInt("id"));
			renderJ();
		}else{
			renderError("添加失败");
		}
	}


	@Authority({
		Constant.AUTHORITY_ADMIN,Constant.AUTHORITY_MONITOR,Constant.AUTHORITY_SECRETARY,
		Constant.AUTHORITY_LIFE_REP,Constant.AUTHORITY_ACADEMIC_REP,Constant.AUTHORITY_SPORTS_REP
	})
	@Before({LoginInterceptor.class,AuthorityInterceptor.class,MyEvictInterceptor.class})
	@MyCacheName({"index_cache","news_cache"})
	public void mergeNews(){
		renderJ(null==editNewsImpl.merge(getModel(News.class,"news")));
	}

	@Authority({
		Constant.AUTHORITY_ADMIN,Constant.AUTHORITY_MONITOR,Constant.AUTHORITY_SECRETARY,
		Constant.AUTHORITY_LIFE_REP,Constant.AUTHORITY_ACADEMIC_REP,Constant.AUTHORITY_SPORTS_REP
	})
	@Before({LoginInterceptor.class,AuthorityInterceptor.class,MyEvictInterceptor.class})
	@MyCacheName({"index_cache","news_cache"})
	public void deleteNews(){
		renderJ(editNewsImpl.delete(getParaToInt("atyId")));
	}

}
