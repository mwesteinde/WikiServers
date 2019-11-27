package cpen221.mp3.cache;

public class StringCacheable implements Cacheable {
    private String id;

    public StringCacheable (String str) {
        this.id = str;
    }

    @Override
    public String id() {
        return this.id();
    }
}
