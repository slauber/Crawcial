package de.crawcial.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sebastian Lauber on 04.03.15.
 */
public class DatabaseAttachDispatcher extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseAttachDispatcher.class);
    private static DatabaseAttachDispatcher ourInstance = new DatabaseAttachDispatcher();
    private LinkedBlockingQueue<DatabaseAttachment> q;
    private HashSet<DatabaseAttachment> h;
    private int maxThread = 8;
    private boolean running = true;

    private DatabaseAttachDispatcher() {
        q = new LinkedBlockingQueue<>();
        h = new HashSet<>();
    }

    public static DatabaseAttachDispatcher getInstance() {
        return ourInstance;
    }

    @Override
    public void run() {
        int i = 0;
        long startTime = System.currentTimeMillis();

        while (running || !q.isEmpty()) {
            if (h.size() <= maxThread) {
                try {
                    synchronized (q) {
                        DatabaseAttachment da = q.poll(10, TimeUnit.MILLISECONDS);
                        if (da != null) {
                            ++i;
                            logger.debug("Persisting.. q: {}, h: {}", q.size(), h.size());
                            h.add(da);
                            Thread t = new Thread(da);
                            t.setName("Attachment-" + da.getId());
                            t.start();
                            if (i % 100 == 0) {
                                System.out.println("Persisted per second: " + (i / ((System.currentTimeMillis() - startTime) / 1000)));
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void reset() {
        ourInstance = new DatabaseAttachDispatcher();
    }

    synchronized void addDownloader(DatabaseAttachment da) throws InterruptedException {
        q.put(da);
    }

    synchronized void downloadDone(DatabaseAttachment da) {
        h.remove(da);
    }

    synchronized void shutdown() throws InterruptedException {
        running = false;
        logger.debug("AttachmentDispatcher shutdown");
    }

}
