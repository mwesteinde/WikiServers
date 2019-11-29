package cpen221.mp3.wikimediator;

import cpen221.mp3.cache.Cacheable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

public class Query implements Comparable {
        /*
       Abstraction Function:
            A class to store queries as well as the latest time they were searched for on Wikipedia,
            the number of times they were searched,.

        Representation Invariant:
            num > 1
            timestamp != null and is not after Instant.now()
            query is not an empty string
        */

    /* term searched for */
    private String query;
    /* most recent time queried */
    private Instant timestamp;
    /* number of times query was searched for */
    private Integer numQueries = 0;

    /**
     * Creates a new Query object with the string queried.
     *
     * @param s String searched for by user.
     */
    public Query(String s) {
        query = s;
        timestamp = Instant.now();
        numQueries++;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (o instanceof Query) {
            Query newO = (Query) o;
            if (newO.getNumQueries() > numQueries) {
                return -1;
            } else {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Modifies query when a user searches for it again by updating the timestamp and number of searches.
     *
     * @param s Query searched for.
     */
    public void update(String s) {
        if (this.query.equals(s)) {
            this.timestamp = Instant.now();
            this.numQueries++;
        }
    }

    /**
     * Only considers query field for equality.
     *
     * @param o Object to compare this to.
     * @return Whether this Query is equal to the input object.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Query)) {
            return false;
        }
        Query object = (Query) o;
        if (((Query) o).query.equals(this.query)) {
            return true;
        }
        return false;
    }

    //TODO: hashcode

    public String getQuery() {
        return this.query;
    }

    /**
     * Checks if a time is within 30 seconds of this query being searched.
     *
     * @param t The time to compare this to, t is not null.
     * @return true if t is within 30 seconds of this Query being searched for.
     */
    public boolean within30S(Instant t) {
        return timestamp.isAfter(t.minus(30, ChronoUnit.SECONDS));
    }

    /**
     * @return The number of times this String has been searched for.
     */
    public int getNumQueries() {
        return this.numQueries;
    }

    /**
     * A method used to test peakload30s
     *
     * @param time amount of time to subtract in seconds. Must be positive.
     */
    protected void mutateTime(int time) {
        this.timestamp.minusSeconds(time);
    }
}
