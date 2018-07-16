package com.imooc.miaosha.controller;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import com.imooc.miaosha.redis.KeyPrefix;
import com.imooc.miaosha.redis.RedisService;

/**
 * @author 605162215@qq.com
 *
 * @date 2018年1月26日 上午9:54:23<br/>
 */
@Controller
public class BaseController {
	
	//加一个配置项
	@Value("#{'${pageCache.enbale}'}")
	private boolean pageCacheEnable;
	
	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;
	
	@Autowired
	RedisService redisService;
	

	public String render(HttpServletRequest request, HttpServletResponse response, Model model,String tplName, KeyPrefix prefix, String key) {
    	if(!pageCacheEnable) {
    		return tplName;
    	}
    	//取缓存
    	String html = redisService.get(prefix, key, String.class);
    	if(!StringUtils.isEmpty(html)) {
    		out(response, html);
    		return null;
    	}
    	//手动渲染
    	WebContext ctx = new WebContext(request,response,
    			request.getServletContext(),request.getLocale(), model.asMap());
    	html = thymeleafViewResolver.getTemplateEngine().process(tplName, ctx);
    	if(!StringUtils.isEmpty(html)) {
    		redisService.set(prefix, key, html);
    	}
    	out(response, html);
    	return null;
    }
	
	public static void out(HttpServletResponse res, String html){
		res.setContentType("text/html");
		res.setCharacterEncoding("UTF-8");
		try{
			OutputStream out = res.getOutputStream();
			out.write(html.getBytes("UTF-8"));
			out.flush();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
