package com.releasex.realtime.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListener;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicy;
import org.atmosphere.cpr.BroadcasterLifeCyclePolicyListener;
import org.atmosphere.cpr.HeaderConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.SearchParameters;
import org.springframework.social.twitter.api.impl.TwitterTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.releasex.realtime.model.TwitterMessage;
import com.releasex.realtime.services.FeedService;

@Controller
public class HomeController 
{
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	private final Map<String, Future<?>> futures = new ConcurrentHashMap<String, Future<?>>();
	private long sinceId = 0;

	@Autowired
	private FeedService feedService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! the client locale is " + locale.toString());
		model.addAttribute("serverTime", feedService.formatDate(locale));
		return "home";
	}

	@RequestMapping(value = "/twitter/concurrency", method = RequestMethod.GET)
	@ResponseBody
	public void execute(AtmosphereResource event) {
		try {
			event.suspend();
			final AtmosphereRequest request = event.getRequest();
			final String username = request.getSession().getId();
			request.getSession().setAttribute("broadcasterId", username);
			logger.info("username : {}", username);
			final Broadcaster broadcaster = BroadcasterFactory.getDefault().lookup(username, true);
			broadcaster.setScope(Broadcaster.SCOPE.APPLICATION);
			broadcaster.setBroadcasterLifeCyclePolicy(BroadcasterLifeCyclePolicy.EMPTY_DESTROY);
			broadcaster.addBroadcasterLifeCyclePolicyListener(new BroadcasterLifeCyclePolicyListener() {
				public void onEmpty() {
					logger.info("{} calls onEmpty", broadcaster.getID());
				}

				public void onIdle() {
					logger.info("{} calls onIdle", broadcaster.getID());
				}

				public void onDestroy() {
					logger.info("{} calls onDestroy",
							broadcaster.getID());
				}
			});
			event.addEventListener(new AtmosphereResourceEventListener() {
				@Override
				public void onSuspend(final AtmosphereResourceEvent event) {
					logger.info("{} calls onSuspend", username);
				}

				@Override
				public void onResume(final AtmosphereResourceEvent event) {
					logger.info("{} calls onResume", username);
				}

				@Override
				public void onDisconnect(final AtmosphereResourceEvent event) {
					String transport = event.getResource().getRequest()
							.getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);
					Future<?> future = futures.get(username);
					if (event != null && event.broadcaster() != null)
						event.broadcaster().resumeAll();
					if (future != null) {
						future.cancel(true);
					}

					logger.info("{} calls onDisconnect, {}", username,
							transport);
				}

				@Override
				public void onBroadcast(final AtmosphereResourceEvent event) {
					String transport = event.getResource().getRequest()
							.getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);
					logger.info("{} calls onBroadcast, {}", username, transport);
				}

				@Override
				public void onThrowable(final AtmosphereResourceEvent event) {
					logger.info("{} calls onThrowable", username);
				}
			});
			broadcaster.addAtmosphereResource(event);
			Future<?> future = broadcaster.scheduleFixedBroadcast(
					executeApplication(username), 10, TimeUnit.SECONDS);
			futures.put(username, future);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	private Callable<String> executeApplication(final String username) {
		final ObjectMapper objectMapper = new ObjectMapper();
		return new Callable<String>() {
			@Override
			public String call() throws Exception {
				try {
					final TwitterTemplate twitterTemplate = new TwitterTemplate(
							"p1pOLWX6XuZrhmOJ3znRg",
							"afeJTNhwqEpwRa7R9gVL8HFNo7nX57732lrehzks",
							"11425532-BS6OWsRY60AgubvN27H0XrXAZAHd6eyv7F87o00m4",
							"RlBDi8swEIkhe2m5opOc8LSNoyNEI5GnYPgBgA0QDH4");
					final SearchParameters parameters = new SearchParameters(
							"news").count(5).sinceId(sinceId).maxId(0);
					final SearchResults results = twitterTemplate
							.searchOperations().search(parameters);
					sinceId = results.getSearchMetadata().getMax_id();
					List<TwitterMessage> twitterMessages = new ArrayList<TwitterMessage>();
					for (Tweet tweet : results.getTweets()) {
						twitterMessages.add(new TwitterMessage(tweet.getId(),
							tweet.getCreatedAt(), tweet.getText(), tweet.getFromUser(), tweet.getProfileImageUrl()));
					}

					return objectMapper.writeValueAsString(twitterMessages);
				} catch (Exception e) {
					logger.error("", e);
				}
				return null;
			}
		};
	}
}