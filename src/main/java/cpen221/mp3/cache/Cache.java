package cpen221.mp3.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    //TODO:
    // AF:
    // RI: maxCached >= currentlyCached >= 0

    private Map<T, Long> cache = new HashMap<>();

    /* TODO: Implement this datatype */

    /**
     * Create a cache with a fixed capacity and a timeout value.
     * Objects in the cache that have not been refreshed within the timeout period
     * are removed from the cache.
     *
     * @param capacity the number of objects the cache can hold. Must be less than or equal to 256.
     * @param timeout  the duration, in seconds, an object should be in the cache before it times out. Must be less than
     *                 or equal to
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
     * @param t the value to add to the cache
     * @return true if an object was removed and false if not.
     * @throws
     */
    public boolean put(T t) throws Exception {
        long time;
        boolean full = false;

        time = java.lang.System.currentTimeMillis() / 1000;

        //TODO: what is this doing?
        Object last = expiry();
        if (cache.size() == 0) {
            cache.put(t, time);
        } else {
            if (last == null) {
                throw new Exception("last==null");
            }
            if (cache.containsKey(t)) {
                cache.replace(t, cache.get(t), time);
            } else {
                if (cache.size() == this.capacity) {
                    full = true;
                    cache.remove(last);
                }
                cache.put(t, time);
            }
        }

        updateSize();
        return full;
    }

    //Checks for objects that have been in cache for longer than timeout
    private Object expiry() {
        Object last = null;
        long time = java.lang.System.currentTimeMillis() / 1000;
        long longest = 0;

        for (Map.Entry i : cache.entrySet()) {
            long duration = time - (int) i.getValue();
            if (duration > this.timeout) {
                cache.remove(i);
                updateSize();
            }
            if (duration >= longest) {
                last = i;
            }
        }

        return last;
    }

    /**
     * @param id the identifier of the object to be retrieved
     * @return the object that matches the identifier from the cache
     */
    public T get(String id) throws NotPresentException {
        /* Do not return null. Throw a suitable checked exception when an object
            is not in the cache. */
        T returned = null;

        Object o = expiry();

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
     * Update the last refresh time for the object with the provided id.
     * This method is used to mark an object as "not stale" so that its timeout
     * is delayed.
     *
     * @param id the identifier of the object to "touch"
     * @return true if successful and false otherwise
     */
    public boolean touch(String id) {
        T now;
        Object o = expiry();
        long time;

        time = java.lang.System.currentTimeMillis() / 1000;
        try {
            now = get(id);
        } catch (NotPresentException e) {
            return false;
        }

        cache.replace(now, time);
        return true;
    }

    /**
     * Update an object in the cache.
     * This method updates an object and acts like a "touch" to renew the
     * object in the cache.
     *
     * @param t the object to update
     * @return true if successful and false otherwise
     */
    public boolean update(T t) {
        boolean returned = false;
        Object o = expiry();
        long time = java.lang.System.currentTimeMillis() / 1000;
        String id = t.id();
        for (Map.Entry i : cache.entrySet()) {
            T now = (T) i.getKey();
            if (now.id().equals(id)) {
                returned = true;
                cache.remove(now);
                cache.put(t, time);
                break;
            }
        }

        return returned;
    }

    /**
     * Updates the variables tracking number of cached items.
     */
    private void updateSize() {
        this.currentlyCached = this.cache.size();
        if (this.currentlyCached > this.maxCached) {
            this.maxCached = this.currentlyCached;
        }
    }

    /**
     * @return The maximum number of items that have been cached at any time.
     */
    public int getMaxCached() {
        return maxCached;
    }
}
