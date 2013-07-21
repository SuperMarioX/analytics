package org.mortbay.log;

import java.text.MessageFormat;

import com.cnebula.common.log.imp.LogImp;
import com.cnebula.common.log.imp.LogManager;

public class ESLog implements Logger {

	 private  LogImp log;
	 
	 public static String format(String pattern, Object ... args) {
		 StringBuilder rt = new StringBuilder();
		 boolean patternBegin = false;
		 int patternIdx = 0;
		 for (int i = 0; i < pattern.length(); i++){
			 char c = pattern.charAt(i);
			 if (patternBegin){
				 if (c == '}'){
					 rt.append(args[patternIdx++]);
					 patternBegin = false;
				 }else{
					 rt.append("{");
					 if (c != '{'){
						 rt.append(c);
					 }
				 }
			 }else{
				 if (c != '{'){
					 rt.append(c);
				 }
			 }
			 patternBegin = c == '{';
		 }
		 return rt.toString();
	 }
	 
	 public ESLog() {
		 log = LogManager.instance.getLog(getClass());
	}
	
	@Override
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	@Override
	public void setDebugEnabled(boolean enabled) {
		return;
	}

	@Override
	public void info(String msg, Object arg0, Object arg1) {
		String fmsg = format(msg, arg0, arg1);
		log.info(fmsg);
	}

	@Override
	public void debug(String msg, Throwable th) {
		log.debug(msg, th);
	}

	@Override
	public void debug(String msg, Object arg0, Object arg1) {
		String fmsg = format(msg, arg0, arg1);
		log.debug(fmsg);
	}

	@Override
	public void warn(String msg, Object arg0, Object arg1) {
		String fmsg = format(msg, arg0, arg1);
		log.warn(fmsg);
	}

	@Override
	public void warn(String msg, Throwable th) {
		log.warn(msg, th);
	}

	@Override
	public Logger getLogger(String name) {
		return this;
	}

}
