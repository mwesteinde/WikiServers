package cpen221.mp3.cache;

import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Cache represents a generic cache for up to a certain number of Objects
 * that stores them for a certain amount of time.
 */

public class Cache<T extends Cacheable> {

    /* the default cache size is 32 objects */
    public static final int DSIZE = 32;

    /* the default timeout value is 3600s */
    public static final int DTIMEOUT = 3600;

    /* The maximum number of most recently accessed pages cached */
    private static final int RECENT_PAGES = 256;

    private final int timeout;
    private final int capacity;

    /* Tracks the maximum number of objects that have been cached since creation of this cache */
    private int maxCached = 0;

    private int currentlyCached;

    // AF: A cache is represented by a map with a stored object as keys and their time of placement in the map as values.
    // RI: 0 <= The number of objects in the cache <= capacity.
    // capacity >= maxCached >= currentlyCached >= 0.
    // timeout >= 0
    // The cache must not contain any objects that have been there longer than the timeout value.

    // Thread safety argument: All methods that modify the cache have locks

    private Map<T, Long> cache = new HashMap<>();

    /**
     * Create a cache with a fixed capacity and a timeout value.
     * Objects in the cache that have not been refreshed within the timeout period
     * are removed from the cache.
     *
     * @param capacity the number of objects the cache can hold, >= 0.
     * @param timeout  the duration, in seconds, an object should be in the cache before it times out.
     */
    public Cache(int capacity, int timeout) {
        this.capacity = capacity;
        this.timeout = timeout;
    }

    /**
     * Create a cache with default capacity and timeout values.
     */
    public Cache() {
        this(DSIZE, DTIMEOUT);
    }

    /**
     * Add a value to the cache. If value is already contained by cache, update it.
     * If the cache is full then remove the least recently accessed object to
     * make room for the new object.
     *
     * @param t the value to add to the cache, type t implements cacheable.
     * @return true if an object was removed and false if not.
     */
    public synchronized boolean put(T t) {
        long time;
        boolean full = false;

        time = java.lang.System.currentTimeMillis();

        Object last = expiry(time);
        if (cache.size() == 0) {
            cache.put(t, time);
        } else {
            if (cache.containsKey(t)) {
                cache.replace(t, cache.get(t), time);
            } else {
                synchronized (this) {
                    if (cache.size() == this.capacity) {
                        full = true;
                        cache.remove(last);
                    }
                }
                cache.put(t, time);
            }
        }
        updateSize();
        return full;
    }



    /**
     * Removes stale objects and returns the oldest.
     *
     * @return The oldest object in this cache.
     */
    private synchronized T expiry(long time) {
        T last = null;
        long longest = 0;

        Iterator<Map.Entry<T, Long>> itr = cache.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<T, Long> i = itr.next();
            long duration = time - (long) i.getValue();
            if (duration > this.timeout * 1000) {
                itr.remove();
                updateSize();
            }
            if (duration >= longest) {
                last = i.getKey();
                longest = duration;
            }
        }

        return last;
    }

    /**
     * Gets an object in this cache, if it exists.
     *
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the cache
     * @throws NotPresentException if the object is not in the cache
     */
    public T get(String id) throws NotPresentException {
        T returned = null;
        long time = java.lang.System.currentTimeMillis();
        Object o = expiry(time);

        for (Map.Entry i : cache.entrySet()) {
            T now = (T) i.getKey();
            if (now.id().equals(id)) {
                returned = now;
            }
        }
        if (returned == null) {
            throw new NotPresentException("object represented by id is not contained in cache");
        }
        return returned;
    }

    /**
     * Updates the last refresh time for the object with the provided id.
     * This method is used to mark an object as "not stale" so that its timeout
     * is delayed.
     *
     * @param id the identifier of the object to "touch"
     * @return true if successful and false otherwise. True if the object is in the cache.
     */
    public boolean touch(String id) {
        T now;
        long time = java.lang.System.currentTimeMillis();
        Object o = expiry(time);

        try {
            now = get(id);
        } catch (NotPresentException e) {
            return false;
        }
        synchronized (this) {
            cache.replace(now, time);
        }
        return true;
    }

    /**
     * Update an object in the cache.
     * This method updates an object and acts like a "touch" to renew the
     * object in the cache.
     *
     * @param t the object to update
     * @return true if successful and false otherwise. True if the object is in the cache.
     */
    public boolean update(T t) {
        boolean returned = false;
        long time = java.lang.System.currentTimeMillis();
        Object o = expiry(time);

        String id = t.id();
        Iterator<Map.Entry<T, Long>> itr = cache.entrySet().iterator();
        while (itr.hasNext()) {
            T now = (T) itr.next().getKey();
            if (now.id().equals(id)) {
                returned = true;
                synchronized (this) {
                    cache.remove(now);
                    cache.put(t, time);
                    break;
                }
            }
        }

        return returned;
    }

    /**
     * Synchronizes the size variables with present the cache size.
     */
    private void updateSize() {
        this.currentlyCached = this.cache.size();
        if (this.currentlyCached > this.maxCached) {
            this.maxCached = this.currentlyCached;
        }
    }

    /**
     * Gets the maximum number of objects that have been cached since creation of this cache.
     *
     * @return maxCached, The maximum number of items that have been cached at any time.
     */
    public int getMaxCached() {
        return maxCached;
    }

}
