package org.home.cryptobot.openinterest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.home.cryptobot.exchange.ExchangeInfoStore;
import org.home.cryptobot.exchange.ExchangeInfoStoreCache;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class OpenInterestChecker {

    private static final int HISTORY_CAPACITY = 5;
    public volatile boolean stopped;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final JsonFactory factory = new JsonFactory();
    private final Map<String, Queue<Double>> limitedHistory = new HashMap<>();
    private final ExchangeInfoStore exchangeInfoStore = new ExchangeInfoStoreCache();
    private final OpenInterestDao openInterestDao = new OpenInterestDao();

    public void check() {
        List<OpenInterest> data = exchangeInfoStore.getAvailableUSDTPairs()
            .parallelStream()
            .map(this::check)
            .filter(OpenInterest::isNotEmpty)
            .map(this::saveInDb)
            .map(this::saveImMem)
            .toList();

        System.out.println("number of checked pairs: " + data.size());
    }

    private OpenInterest saveImMem(OpenInterest data) {
        if (!limitedHistory.containsKey(data.pair)) {
            Queue<Double> history = new LinkedList<>();
            history.add(data.openInterest);
            limitedHistory.put(data.pair, history);
        } else {
            if (limitedHistory.get(data.pair)
                .size() < HISTORY_CAPACITY) {
                limitedHistory.get(data.pair)
                    .add(data.openInterest);
            } else {
                Queue<Double> history = limitedHistory.get(data.pair);
                Double oldestElement = history.poll();
                double changedValue = data.openInterest - oldestElement;
                double percentage = changedValue * 100 / oldestElement;
                if (percentage > 10) {
                    System.out.println("Pair: " + data.pair);
                    System.out.println("OI changed: " + changedValue);
                    System.out.println("OI changed %: " + percentage);
                }
            }
        }

        return data;
    }

    private OpenInterest saveInDb(OpenInterest openInterest) {
        openInterestDao.save(openInterest);

        return openInterest;
    }

    private OpenInterest check(String pair) {

        var startTime = System.currentTimeMillis() - 300 * 1000;//current time -5 minutes

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://www.binance.com/futures/data/openInterestHist?symbol="+pair+"&period=5m&limit=10&startTime="+startTime))
            .timeout(Duration.ofMinutes(2))
            .GET()
            .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() != 200) {
            return new OpenInterest();
        }

        double lastOpenInterest = 0.0;
        long lastTime = 0L;

        try (JsonParser parser = factory.createParser(response.body())) {
            double currentOpenInterest = 0.0;
            while (parser.nextToken() != null) {
                var fieldName = parser.currentName();
                if ("sumOpenInterest".equals(fieldName) && parser.currentToken().equals(JsonToken.VALUE_STRING)) {
                    currentOpenInterest = parser.getValueAsDouble();
                } else if ("timestamp".equals(fieldName) && parser.currentToken().equals(JsonToken.VALUE_NUMBER_INT)) {
                    var currentTime = parser.getValueAsLong();
                    if (currentTime > lastTime) {
                        lastTime = currentTime;
                        lastOpenInterest = currentOpenInterest;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (lastOpenInterest != 0.0) {
            return new OpenInterest(new Timestamp(lastTime), pair, lastOpenInterest);
        }

        return new OpenInterest();
    }

    public void shutDown() {
        openInterestDao.shutDown();
        stopped = true;
    }

    public void init() {
        openInterestDao.init();
    }

    //calculate the diff. If diff is more than M limit - notify in console
}
