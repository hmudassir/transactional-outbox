# Architecture

The request that accepts work and the email that follows it commit together. The mail provider is called later, by a worker, and only for a row that this transaction left behind.

## Boundaries

```text
POST /api/notices
        |
        v
NoticeController
        |
        v
NoticeService                         one transaction
        |-- insert notices
        +-- insert email_outbox       status PENDING
        |
        commit, then HTTP 202
                |
                v
        OutboxScheduler               every 5 seconds, off in tests
                |
                v
        OutboxWorker
                |-- claim: PENDING -> SENDING, only if still PENDING
                +-- LoggingMailSender
                        success -> SENT
                        rejection -> FAILED
```

`LoggingMailSender` is the port. This sample logs the delivery. A provider adapter would implement the same `send` method. The controller does not call that port.

## Request path

1. The service inserts the notice and the outbox row in one transaction.
2. The response is `202` after that commit. It does not wait for delivery.
3. The worker reads pending rows and claims one by updating `PENDING` to `SENDING` only while it is still `PENDING`. A second worker that loses the update does not send.
4. After the claim, the sender runs. The row becomes `SENT` or `FAILED`.

On PostgreSQL the same claim is usually `SELECT … FOR UPDATE SKIP LOCKED`. The update-if-pending claim here runs on H2 so the sample needs no database server.

## Failure

| Condition | Result |
| --- | --- |
| The request throws before commit | The notice and the outbox row both disappear. The worker cannot send mail for a request that never happened. |
| The recipient ends in `@fail.test` | The provider rejects the message. The row becomes `FAILED` and `attempts` is 1. |
| The worker runs again | A `SENT` or `FAILED` row is not claimed again. |

The claim and the send of one message use their own transaction (`REQUIRES_NEW`), so one failure does not roll back the others.

## What this sample leaves out

One process, no real inbox, and no retry schedule beyond the failed status. A second instance would use the same claim so two workers do not both send one row.
