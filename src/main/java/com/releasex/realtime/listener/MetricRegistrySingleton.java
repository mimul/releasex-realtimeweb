package com.releasex.realtime.listener;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

public final class MetricRegistrySingleton {
	public static final MetricRegistry metrics = new MetricRegistry();
	
    static {
        Logger logger = LoggerFactory.getLogger("com.releasex.realtime.listener");
        final Slf4jReporter reporter = Slf4jReporter.forRegistry(metrics).outputTo(logger).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
        reporter.start(5, TimeUnit.MINUTES);
    }
	
}
