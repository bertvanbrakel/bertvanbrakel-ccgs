package com.bertvanbrakel.ccgs;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public abstract class SimpleView implements View {

	private static final Logger LOG = Logger.getLogger(SimpleView.class);
	private final String path;
	
	public SimpleView(String path){
		this.path = path;
	}
	
	@Override
	public final String getPath() {
		return path;
	}

	@Override
	public final  void render(HttpServletRequest req, HttpServletResponse res) {
		PrintWriter w;
		try {
			w = res.getWriter();
		} catch (IOException e) {
			//TODO:detect client disconnect error
			LOG.warn("Error getting client writer",e);
			return;
		}

		int code = render(w, req.getParameterMap());
		res.setStatus(code);
		w.flush();
	}

	protected String getParam(Map<String, String[]> params, String paramName) {
		String[] val = params.get(paramName);
		if (val != null && val.length > 0) {
			return val[0];
		}
		return null;
	}
	
    protected String removeUnsafeXssChars(String s){
    	if( s != null){
	    	String allowed="&=-_ abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890./:";
	    	StringBuilder sb = new StringBuilder();
	    	for( int i = 0;i< s.length();i++){
	    		char c = s.charAt(i);
	    		if( allowed.indexOf(c) != -1){
	    			sb.append(c);
	    		}
	    	}
	    	
	    	return sb.toString();
    	} 
    	return null;
    }
	
	protected abstract int render(PrintWriter w, Map<String,String[]> params);
}
