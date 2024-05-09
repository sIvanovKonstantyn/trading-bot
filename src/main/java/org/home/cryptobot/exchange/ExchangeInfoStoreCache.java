package org.home.cryptobot.exchange;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExchangeInfoStoreCache implements ExchangeInfoStore {
    private final ExchangeInfoStore exchangeInfoStore = new ExchangeInfoTaker();
    private final Set<String> cache = new HashSet<>();

    @Override
    public List<String> getAvailableUSDTPairs() {
        if (cache.isEmpty()) {
            cache.addAll(exchangeInfoStore.getAvailableUSDTPairs());
        }
        return new ArrayList<>(cache);
    }
}
