# Transactional outbox

A request and the email it should send are stored in **one database transaction**. A later worker delivers the email. If the request rolls back, no email row remains, so the worker cannot send a message for a request that never happened.

The worker claims a row by updating `PENDING` to `SENDING` only when it is still `PENDING`. A second worker that loses that update does not send. This sample logs the delivery instead of calling a mail provider. A recipient ending in `@fail.test` is treated as a provider rejection and the row becomes `FAILED`.

On PostgreSQL, the same claim is usually `SELECT … FOR UPDATE SKIP LOCKED`. The update-if-pending claim here runs on H2 so the sample needs no database server.

## Run

Java 21.

```sh
./mvnw spring-boot:run
```

```sh
curl -sS -H 'Content-Type: application/json' \
  -d '{"recipient":"ada@example.com","body":"Your note is saved"}' \
  http://127.0.0.1:8080/api/notices
```

The response is `202`. The worker runs every five seconds and logs `Delivered notice <id>`.

## Tests

```sh
./mvnw test
```

The tests call the worker directly. The scheduler is off on the `test` profile so a test does not race the background thread.

## What this is

A portfolio proof of concept. It does not send real email.
