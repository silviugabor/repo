package repo.web.wicket.util;

import javax.servlet.ServletContext;

import org.apache.wicket.protocol.http.WebApplication;

public final class WicketUtils {
	public static String getRootContext(){

		String rootContext = "";

		WebApplication webApplication = WebApplication.get();
		if(webApplication!=null){
			ServletContext servletContext = webApplication.getServletContext();
			if(servletContext!=null){
				rootContext = servletContext.getServletContextName();
			}else{
				//do nothing
			}
		}else{
			//do nothing
		}

		return rootContext;

}
}
