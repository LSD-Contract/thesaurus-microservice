package com.lsd.thesaurus.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ILogger {

	@Value("${logLevel}")
	private int logLevel;

	private Logger logger;
	private ThreadLocal<String> threadLocal = null;

	public ILogger() {
		this.logger = LoggerFactory.getLogger(this.getClass());
	}

	public void setThreadLocal(ThreadLocal<String> threadLocal) {
		this.threadLocal = threadLocal;
	}

	public void debug(int level, Class<?> cls, String msg) {
		//System.out.println("logLevel:"+logLevel);
		//System.out.println("logging debug message msg:"+msg);
		if (logLevel >= level) {
			System.out.println("logging debug message msg:"+msg);
			logger.debug("[" + cls.getName() + "]" + "[" + ((threadLocal == null) ? "" : threadLocal.get() + "]") + " " + msg);
		}
	}

	public void error(int level, Class<?> cls, String msg) {
		if (logLevel >= level) {
			logger.error(cls.getName() + " " + msg);
		}
	}

	public void info(int level, Class<?> cls, String msg) {
		if (logLevel >= level) {
			logger.info(cls.getName() + " " + msg);
		}
	}

	public void trace(int level, Class<?> cls, String msg) {
		if (logLevel >= level) {
			logger.trace(cls.getName() + " " + msg);
		}
	}

}
