package de.crawcial.twitter;

import com.twitter.hbc.twitter4j.handler.StatusStreamHandler;
import com.twitter.hbc.twitter4j.message.DisconnectMessage;
import com.twitter.hbc.twitter4j.message.StallWarningMessage;
import de.crawcial.database.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;

/**
 * Created by Sebastian Lauber on 24.02.15.
 */
class CraTwitterStatusListener implements StatusStreamHandler {

    final static private Logger logger = LoggerFactory.getLogger(CraTwitterStatusListener.class);
    private final boolean downloadMedia;

    public CraTwitterStatusListener(boolean downloadMedia) {
        this.downloadMedia = downloadMedia;
    }

    @Override
    public void onStatus(Status status) {
        // Persist tweet using DatabaseService singleton
        logger.debug("onStatus - StatusID: {}", status.getId());
        DatabaseService.getInstance().persist(status, downloadMedia);
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        logger.warn("onDeletionNotice - StatusID: {}", statusDeletionNotice.getStatusId());
    }

    @Override
    public void onTrackLimitationNotice(int i) {
        logger.warn("onTrackLimitationNotice - {}", new Integer(i));
    }

    @Override
    public void onScrubGeo(long l, long l1) {
        logger.warn("onScrubGeo - Lat {} Long {}", new Long(l), new Long(l1));
    }

    @Override
    public void onStallWarning(StallWarning stallWarning) {
        logger.warn("onStallWarning - {}", stallWarning.getMessage());
    }

    @Override
    public void onException(Exception e) {
        logger.error("onException - {}", e.toString());
    }

    @Override
    public void onDisconnectMessage(DisconnectMessage disconnectMessage) {
        logger.error("onDisconnectMessage - Code: {} - {}", disconnectMessage.getDisconnectCode(), disconnectMessage.getDisconnectReason());
    }

    @Override
    public void onStallWarningMessage(StallWarningMessage stallWarningMessage) {
        logger.warn("onStallWarningMessage - {}", stallWarningMessage.getMessage());
    }

    @Override
    public void onUnknownMessageType(String s) {
        logger.warn("onUnknownMessageType - {}", s);
    }
}
