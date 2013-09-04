package com.releasex.realtime.atmosphere.listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BroadcasterListener implements HttpSessionListener 
{
	private static final Logger logger = LoggerFactory.getLogger(BroadcasterListener.class);
	public static final String BROADCASTER_ID = "broadcasterId";
	
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		try {
		HttpSession session = se.getSession();
		String broadcasterId = "test";
		logger.info("Creating broadcaster: {}", broadcasterId);
		//session.setAttribute(BROADCASTER_ID, broadcasterId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		try {
			HttpSession session = se.getSession();
			String broadcasterId = (String) session.getAttribute(BROADCASTER_ID);
			logger.info("Removing broadcaster: {}", broadcasterId);

			if (broadcasterId != null) {
				Broadcaster b = BroadcasterFactory.getDefault().lookup(broadcasterId);
				if (b != null)
					b.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
