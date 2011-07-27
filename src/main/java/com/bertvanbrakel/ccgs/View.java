package com.bertvanbrakel.ccgs;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface View {
	public String getPath();
	
	public void render(HttpServletRequest req, HttpServletResponse res);
}
