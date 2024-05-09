package org.home.cryptobot.exchange;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class ExchangeInfoTaker implements ExchangeInfoStore {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.binance.com/api/v3/exchangeInfo"))
        .timeout(Duration.ofMinutes(2))
        .GET()
        .build();
    private final JsonFactory factory = new JsonFactory();

    public List<String> getAvailableUSDTPairs() {

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() != 200) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();

        try (JsonParser parser = factory.createParser(response.body())) {
            while (parser.nextToken() != null) {
                var fieldName = parser.currentName();
                if ("symbol".equals(fieldName) && parser.currentToken().equals(JsonToken.VALUE_STRING)) {
                    var symbol = parser.getValueAsString();
                    if (symbol.endsWith("USDT")) {
                        result.add(symbol);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
