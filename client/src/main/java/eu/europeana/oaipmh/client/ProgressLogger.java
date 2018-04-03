package eu.europeana.oaipmh.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.Duration;
import org.joda.time.Period;

/**
 * Utility class to log progress of long harvests
 * @author Patrick Ehlert
 * Created on 30-03-2018
 */
public class ProgressLogger {

    private static final Logger LOG = LogManager.getLogger(ProgressLogger.class);

    private long startTime;
    private long totalItems;
    private int logAfterSeconds;
    private long lastLogTime;

    /**
     * Create a new progressLogger. This also sets the operation start Time
     * @param totalItems total number of items that are expected to be retrieved
     * @param logAfterSeconds to prevent too much logging, only log every x seconds
     */
    public ProgressLogger(long totalItems, int logAfterSeconds) {
        this.startTime = System.currentTimeMillis();
        this.lastLogTime = startTime;
        this.totalItems = totalItems;
        this.logAfterSeconds = logAfterSeconds;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    /**
     * Log the number of items that are left to retrieve and an estimate of the remaining time, but only every x seconds
     * as specified by logAfterSeconds
     * @param itemsDone the number of items that have been retrieved
     */
    public void logProgress(long itemsDone) {
        Duration d = new Duration(lastLogTime, System.currentTimeMillis());
        if (logAfterSeconds > 0 && d.getMillis() / 1000 > logAfterSeconds) {
            if (totalItems > 0) {
                Period period = new Period(Math.round((totalItems - itemsDone) * (Double.valueOf(itemsDone) / (System.currentTimeMillis() - startTime))));
                String time;
                if (period.getDays() >= 1) {
                    time = String.format("%d days, %d hours and %d minutes", period.getDays(), period.getHours(), period.getMinutes());
                } else if (period.getHours() >= 1) {
                    time = String.format("%d hours and %d minutes", period.getHours(), period.getMinutes());
                } else {
                    time = String.format("%d minutes and %d seconds", period.getMinutes(), period.getSeconds());
                }
                LOG.info("Retrieved {} items of {}. Expected time remaining is {}", itemsDone, totalItems, time);
            } else {
                LOG.info("Retrieved {} items");
            }
            lastLogTime = System.currentTimeMillis();
        }
    }
}