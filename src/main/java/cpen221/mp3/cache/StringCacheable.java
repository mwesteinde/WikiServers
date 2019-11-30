package cpen221.mp3.cache;

public class StringCacheable implements Cacheable {
    //RI: String is not null
    //AF: String represents a cached object

    private String id;

    /**
     * Associates an id value to a cache object
     * @param str a unique identifier for an object
     */
    public StringCacheable(String str) {
        this.id = str;
    }

    /**
     * @return an identifier that can uniquely identify an instance of Cacheable
     */
    @Override
    public String id() {
        return this.id();
    }
}
