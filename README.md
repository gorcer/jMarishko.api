jMarishko.api
=============

API-сервер для самообучаемого чат-бота Маришко.

Слеплен на 90% из старого студенческого кода для Skype-версии https://github.com/gorcer/JMarishko.Skype

База фраз (/db/ai.sql.gz)  импортирована из ещё более старой ICQ-версии http://marishko.gorcer.com/

Реализация на Spark Java.

Реализованы следующие ЕП:

/0.1/getAnswer/{userName}/{message}/

/0.1/getHello/{userName}/

/0.1/getSometing/{userName}/


