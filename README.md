Aggregate API with Akka
=========================

Build sample project using Play Scala and aggregate currency exchange rate from 2 API with Akka.

List API:
1. https://currencylayer.com/
2. http://fixer.io/

For CurrencyLayer API, you need to have access key in order to use the API. Just register and you will get the access key.

Provide 2 endpoints:
1. "/" : For aggregate 2 API
2. "/realtime" : Using websocket to get latest rate of 2 API
