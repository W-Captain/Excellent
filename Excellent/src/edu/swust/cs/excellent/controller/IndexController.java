package edu.swust.cs.excellent.controller;

import com.jfinal.aop.Before;
import com.jfinal.plugin.spring.IocInterceptor;
import com.jfinal.plugin.spring.Inject;

import edu.swust.cs.excellent.authorized.LoginInterceptor;
import edu.swust.cs.excellent.service.inter.ILogin;

@Before(IocInterceptor.class)
public class IndexController extends CommonController{

    @Inject.BY_TYPE
	private ILogin loginImpl;
    
	public void index(){
	   System.out.println("ok");
   }
	
	public void captcha()
    {
        render(new MyCaptchaRender(60,22,4,true));
    }
	
	public void login(){
		String captcha = getPara("captcha");
		if (!MyCaptchaRender.validate(this, captcha)){
			renderError("验证码错误");
			return;
		}
		String uid  = getPara("userName");
		String pswd = getPara("pswd");
		renderJ(loginImpl.login(uid, pswd,getSession()));
	}
	

	
	@Before(LoginInterceptor.class)
	public void logout(){
		renderJ(loginImpl.logout(getSession()));
	}
}