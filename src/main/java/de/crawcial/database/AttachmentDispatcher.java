package de.crawcial.database;

import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Sebastian Lauber on 03.03.2015.
 */
public class AttachmentDispatcher extends Thread {
    private static AttachmentDispatcher ourInstance = new AttachmentDispatcher();
    LinkedBlockingQueue<DatabaseAttachment> q = new LinkedBlockingQueue<>();
    HashSet<DatabaseAttachment> h = new HashSet<>();
    boolean running = true;
    int maxThreads = 8;

    private AttachmentDispatcher() {
    }

    public static AttachmentDispatcher getInstance() {
        return ourInstance;
    }

    public void setMaxThreads(int threadCnt) {
        maxThreads = threadCnt;
    }

    void init() {
        running = true;
        start();
    }

    @Override
    public void run() {
        int i = 0;
        long startTime = System.currentTimeMillis();
        while (running || !q.isEmpty()) {
            if (h.size() <= maxThreads) {
                try {
                    ++i;
                    DatabaseAttachment da = q.take();
                    h.add(da);
                    new Thread(da).start();
                    if (i % 100 == 0) {
                        System.out.println("Persisted per second: " + (i / ((System.currentTimeMillis() - startTime) / 1000)));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void addDownloader(DatabaseAttachment da) {
        try {
            q.put(da);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void downloadDone(DatabaseAttachment da) {
        h.remove(da);
    }

    public void shutdown() {
        running = false;
    }
}
