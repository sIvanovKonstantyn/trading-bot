package org.home.cryptobot.openinterest;

import java.sql.Timestamp;

public class OpenInterest {
    final Timestamp timestamp;
    final String pair;
    final Double openInterest;

    public OpenInterest(Timestamp timestamp, String pair, double lastOpenInterest) {
        this.timestamp = timestamp;
        this.pair = pair;
        this.openInterest = lastOpenInterest;
    }

    public OpenInterest() {
        this.timestamp = null;
        this.pair = null;
        this.openInterest = null;
    }

    public boolean isNotEmpty() {
        return pair != null && openInterest != null;
    }
}
