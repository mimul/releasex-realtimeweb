package com.releasex.realtime.listener;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.codahale.metrics.Counter;

public class SessionMetrics implements HttpSessionListener 
{
	private final Counter activeSessions;
	
    public SessionMetrics() {
        super();
        activeSessions = MetricRegistrySingleton.metrics.counter("web.sessions.active.count");
    }
    
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		activeSessions.inc();
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		activeSessions.dec();
	}
}