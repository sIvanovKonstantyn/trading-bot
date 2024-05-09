package org.home.cryptobot.exchange;

import java.util.List;

public interface ExchangeInfoStore {
    List<String> getAvailableUSDTPairs();
}
