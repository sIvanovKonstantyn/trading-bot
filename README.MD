The Idea:
A bot with a Telegram interface - OI screener.
(Bot) Collects open interest for various currencies once per minute:

We can send only one currency pair at a time. We retrieve them using ExchangeInfoTaker once a day and store them in memory (approximately 4KB).
Based on current data, we have about 500 pairs, which implies 500 outgoing requests per minute. It might take a while and could be flagged as spam by the API. We can consider white/blacklists of currencies for which we do not want to send requests - testing is needed.
If there is a 20% surge in interest within 5 minutes (configurable parameters), it sends a notification on Telegram - this signals that attention is needed and perhaps currencies should be bought (potential bull market for a specific currency). More about working with a similar screener can be found here: https://www.youtube.com/watch?v=etxktQKHee4&t=191s
(Manually) If it's decided that the signal is good, we open a stop-loss order and adjust it until the price starts to fall (https://youtu.be/plNZROAls_c?t=208 here's an example). We include commission prices for buying and selling in the stop-loss.

(Bot) Collects the exchange rate of a pair once per minute and stores it in a database. This is needed for historical backtests of the correlation between open interest and price growth.

(Bot) Has a web API where one can view a graph of the pair's price history with an overlay of the open interest graph.


links:
https://developers.binance.com/docs/derivatives/coin-margined-futures/market-data/Open-Interest-Statistics

https://www.binance.com/en/futures/funding-history/perpetual/trading-data?contract=FRONTUSDT (график открытого интереса бинанс)


