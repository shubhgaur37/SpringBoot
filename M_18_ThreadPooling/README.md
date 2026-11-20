# M_18_ThreadPooling

**Understanding Java thread pools, Spring `@Async`, `CompletableFuture`, scheduled tasks, and asynchronous HTTP
responses in Spring MVC**

## 1. What is this project about?

This is a small Spring Boot learning project that generates a user report by calling three independent services:

| Operation             | Service          | Simulated execution time | Result type                 |
|-----------------------|------------------|-------------------------:|-----------------------------|
| Fetch user details    | `UserService`    |                2 seconds | `CompletableFuture<String>` |
| Fetch order details   | `OrderService`   |                3 seconds | `CompletableFuture<String>` |
| Fetch payment details | `PaymentService` |                4 seconds | `CompletableFuture<String>` |

The endpoint is:

```http
GET /users/reports
```

A successful response is a JSON array containing the user, order, and payment information, in that order.

The project also has a small scheduling example:

```http
POST /emails/{email}
```

This endpoint schedules one email log message for ten seconds later. It demonstrates delayed execution with
`TaskScheduler`; it does not send a real email.

The project explores an important distinction:

> **Running service tasks on background threads is not the same as handling the HTTP request asynchronously.**

The first versions already ran the three service methods with `@Async`. However, the Tomcat request thread **waited**
for their results. The current version instead returns an incomplete `CompletableFuture<List<String>>` to Spring MVC so
that Spring MVC can free the request thread while the work continues.

The goal is **better use of request threads**, not magically faster service operations.

---

## 2. Technologies

- **Java 21** — `CompletableFuture`, threads, and concurrency APIs.
- **Spring Boot 4.1.2** — application configuration and startup.
- **Spring Web MVC + Tomcat** — HTTP request and response handling.
- **Spring `@EnableAsync` and `@Async`** — execution of service methods using an executor.
- **`ThreadPoolTaskExecutor`** — configured pool for the service tasks.
- **`TaskScheduler` and `ThreadPoolTaskScheduler`** — one-time delayed email tasks.
- **`ThreadPoolExecutor`** — underlying Java executor and rejection policy.
- **Lombok `@Slf4j`** — thread and timing logs.
- **Maven Wrapper** — build and run commands.
- **JUnit 5 / Spring Boot test support** — application context test.

## 3. Project components

| Component                          | Responsibility                                                                                       |
|------------------------------------|------------------------------------------------------------------------------------------------------|
| `M18ThreadPoolingApplication`      | Starts the Spring Boot application and enables asynchronous method interception with `@EnableAsync`. |
| `ThreadConfig`                     | Defines the async task executor and the separate scheduler used by delayed email tasks.              |
| `CustomRejectedExecutionHandler`   | Handles tasks rejected when the executor is full; currently sleeps and resubmits.                    |
| `UserService`                      | Simulates fetching user information; about 2 seconds.                                                |
| `OrderService`                     | Simulates fetching order information; about 3 seconds.                                               |
| `PaymentService`                   | Simulates fetching payment information; about 4 seconds.                                             |
| `UserReportService`                | Starts the three operations and combines their eventual results.                                     |
| `UserReportController`             | Handles `GET /users/reports` and returns the report future to Spring MVC.                            |
| `EmailService`                     | Schedules one email log message to run ten seconds later.                                            |
| `EmailController`                  | Handles `POST /emails/{email}` and returns immediately after scheduling.                             |
| `M18ThreadPoolingApplicationTests` | Checks that the Spring application context loads.                                                    |

### What does `@Async` actually change?

Without asynchronous method interception, a normal method call executes on the **calling thread**.

With `@EnableAsync`, a Spring-managed service method annotated with `@Async` can instead be **submitted to the
configured executor**. A worker thread runs the method body.

For a request entering through Tomcat:

```text
Tomcat request thread
  |
  | calls userService.getUserInfo()
  v
Spring @Async proxy
  |
  | submits task
  v
ThreadPoolTaskExecutor
  |
  v
TestParallel_1 executes UserService method body
```

The call to the service returns a `CompletableFuture<String>` representing a result that may not be ready yet. The
returned future is **not** the same thing as the result itself.

**Important:** Spring's proxy must actually intercept the method call. In particular, an `@Async` method called directly
from another method on the **same object** does not normally go through that proxy. This project calls the async methods
on separate injected services.

### How the delayed email scheduler works

The email example uses a different bean from the `@Async` services:

```text
HTTP request thread
  |
  | POST /emails/{email}
  v
EmailService calculates a time 10 seconds later
  |
  | scheduler.schedule(action, scheduleTime)
  v
TaskScheduler stores the delayed action
  |
  | returns immediately
  v
HTTP 204 response

About 10 seconds later:
Task_Scheduler_ worker runs the email log action
```

`ThreadConfig` gives this scheduler a pool size of `3` and the thread-name prefix `Task_Scheduler_`. Up to three
scheduled actions can execute at the same time when they become due. If more actions become due while all three
scheduler workers are busy, the remaining actions wait for a scheduler worker.

The task exists only in application memory. If the application stops before the ten-second delay finishes, there is no
persistent store in this project that can run the task later.

### `TaskScheduler.schedule()` vs `@Scheduled`

The current email code uses `TaskScheduler.schedule(...)` because the execution time is calculated when the HTTP request
arrives. It creates one task for one specific time.

`@Scheduled` is different. Spring calls an annotated method automatically according to a schedule written in the
annotation:

| Option         | Behavior                                                                                                                          |
|----------------|-----------------------------------------------------------------------------------------------------------------------------------|
| `fixedDelay`   | Waits for one execution to finish, then waits for the configured delay before starting again.                                     |
| `fixedRate`    | Tries to start executions at a regular interval measured from their start times. A long-running execution can delay the next run. |
| `cron`         | Runs when the cron expression matches a date and time.                                                                            |
| `initialDelay` | Delays the first execution.                                                                                                       |

This project does **not** currently use `@Scheduled`, and `@EnableScheduling` is not present. If annotation-based
scheduling is added later, scheduling must be enabled and the annotated method will be triggered by Spring without an
HTTP request. The existing `TaskScheduler` bean can then provide the scheduler threads.

---

## 4. Three ideas to understand before reading the code

### 4.1 A `CompletableFuture` is a placeholder for a result

Imagine ordering food at a restaurant:

- The **order receipt** is like a `CompletableFuture` — it represents food that will be ready later.
- The **food** is the actual result — in this project, a `String` returned by a service.
- Holding the receipt does **not** mean the food is ready.

Likewise:

```java
CompletableFuture<String> userFuture = userService.getUserInfo();
```

This gives us a future immediately after successful submission (under normal executor conditions). It does **not** mean
the user information has finished loading.

A future may be:

1. **Incomplete** — the operation has not finished yet.
2. **Completed successfully** — a result is available.
3. **Completed exceptionally** — it finished with an error.

### 4.2 Starting tasks concurrently is different from waiting for them

The report service initiates three independent operations:

```java
CompletableFuture<String> userFuture = userService.getUserInfo();
CompletableFuture<String> orderFuture = orderService.getOrderInfo();
CompletableFuture<String> paymentFuture = paymentService.getPaymentInfo();
```

These three **submission statements are written one after another**. But after submission, the tasks can **run at the
same time** on different executor workers if capacity is available.

A blocking operation such as `get()` or `join()` only blocks **the thread that calls it** while the future is
incomplete. It does not automatically stop other worker threads from progressing.

### 4.3 There are two different sets of threads

**Tomcat request threads**, such as `http-nio-8080-exec-1`, handle incoming HTTP requests and Spring MVC controller
execution.

**Async executor threads**, such as `TestParallel_1`, perform the three service operations.

They have different jobs:

```text
HTTP request --> Tomcat thread --> Controller --> Report service
                                                   |
                                                   | submits work
                                                   v
                                          ThreadPoolTaskExecutor
                                          /         |         \
                                      User        Order      Payment
```

The critical question is: **Does the Tomcat thread wait for these operations, or can it return to the request-thread
pool while they finish?**

That is exactly what changed across the commits.

---

## 5. How the project evolved — recent commits

The commit history below is based on the actual changes in this repository.

| Commit    | Stage                           | What changed                                                                                                                                                                                    |
|-----------|---------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `8b1c5e6` | Initial implementation          | Introduced the project, three `@Async` services, custom executor, and a controller returning `List<String>`. Results were collected using blocking `get()` calls.                               |
| `a3d6029` | Initial README                  | Added documentation; no runtime change.                                                                                                                                                         |
| `fd49c5f` | Executor saturation experiments | Changed maximum pool size from 5 to 3; set queue capacity to 1; introduced the custom rejection handler; replaced sequential waiting with `allOf(...).join()` followed by `get()`.              |
| `7e03c26` | Asynchronous HTTP response      | Changed controller and report service return types to `CompletableFuture<List<String>>`; replaced blocking waiting with `allOf(...).thenApply(...)`; added `whenComplete(...)` failure logging. |
| `2d55827` | Documentation update            | Updated comments and documentation without changing application logic.                                                                                                                          |

**One important clarification:** The initial version was **not** made of ordinary synchronous service methods. The
services already used `@Async`. What was synchronous was the **HTTP response path**, because the report service waited
for the results before returning the list.

---

## 6. Previous implementation: asynchronous work, blocking HTTP request

### 6.1 Initial version — sequential `get()` calls (`8b1c5e6`)

The early report-generation flow was conceptually equivalent to:

```java
CompletableFuture<String> userFuture = userService.getUserInfo();
CompletableFuture<String> orderFuture = orderService.getOrderInfo();
CompletableFuture<String> paymentFuture = paymentService.getPaymentInfo();

// Earlier code also called allOf(...) without waiting on or using its result.

userReport.

add(userFuture.get());
        userReport.

add(orderFuture.get());
        userReport.

add(paymentFuture.get());

        return userReport;
```

*This excerpt is simplified to show the control flow; it is not a verbatim copy of the historical source.*

What happens?

1. A Tomcat worker receives `GET /users/reports`.
2. It enters the controller, then `UserReportService.fetchUserReport()`.
3. It submits all three `@Async` tasks to the task executor.
4. The service calls `userFuture.get()`. If that result is not ready, **the Tomcat worker stops and waits**.
5. After the user result is available, it calls `orderFuture.get()`. This might wait too.
6. It then calls `paymentFuture.get()`, which might also wait.
7. Only after collection finishes does the service return a `List<String>` to the controller.
8. The controller returns the completed list for HTTP response processing.

**The three executor tasks can still overlap.** Calling `get()` does not make the user, order, and payment tasks run
sequentially. It makes **result collection by the Tomcat thread** blocking.

In the initial version, the code also invoked `allOf(...)` without using the returned aggregate future. That invocation
was non-blocking and did not change the subsequent sequential `get()` waits.

### 6.2 Later blocking version — `allOf(...).join()` (`fd49c5f`)

A later version introduced an explicit aggregate wait:

```java
CompletableFuture.allOf(userFuture, orderFuture, paymentFuture).

join();

userReport.

add(userFuture.get());
        userReport.

add(orderFuture.get());
        userReport.

add(paymentFuture.get());
```

Read that first line in two parts:

```java
CompletableFuture.allOf(userFuture, orderFuture, paymentFuture)
```

Creates a future representing **all three tasks finishing**. This part does **not** block.

```java
.join()
```

Immediately waits for that aggregate future **if it is incomplete**. This part **does block the calling Tomcat thread**.

Once this `join()` returns successfully, all three results are already complete. The following `get()` calls therefore
return without further waiting.

**Why this version was still blocking:** The Tomcat worker could not leave `fetchUserReport()` until `allOf(...).join()`
finished.

### 6.3 Why this matters under load

Suppose several clients request reports at the same time. In either blocking version, **each request can occupy one
Tomcat worker for the duration of the report wait**.

Even though the actual service work is happening on `TestParallel_*` workers, the Tomcat worker is still tied up waiting
for results. A tied-up request worker cannot be used to process another request during that wait.

That is the limitation addressed by the newer implementation.

---

## 7. Current implementation: asynchronous HTTP response (`7e03c26`)

The current service uses this composition pattern:

```java
public CompletableFuture<List<String>> fetchUserReport() throws InterruptedException {

    long start = System.currentTimeMillis();

    CompletableFuture<String> userFuture = userService.getUserInfo();
    CompletableFuture<String> orderFuture = orderService.getOrderInfo();
    CompletableFuture<String> paymentFuture = paymentService.getPaymentInfo();

    return CompletableFuture.allOf(userFuture, orderFuture, paymentFuture)
            .thenApply(ignoredParam -> {

                List<String> userReport = List.of(
                        userFuture.join(),
                        orderFuture.join(),
                        paymentFuture.join()
                );

                log.info("Total Time taken to generate Report: {}",
                        System.currentTimeMillis() - start);

                return userReport;
            })
            .whenComplete((result, exception) -> {
                if (exception != null) {
                    log.error("User Report generation failed", exception);
                }
            });
}
```

*This is a focused excerpt of the documented current design, with unrelated log statements omitted to make the sequence
easier to read.*

The controller returns the future rather than extracting its result:

```java

@GetMapping("/users/reports")
public CompletableFuture<List<String>> getUserReport() throws InterruptedException {
    return userReportService.fetchUserReport();
}
```

The signatures above illustrate the current asynchronous HTTP boundary. The existing checked `InterruptedException`
declaration is not required by the visible `CompletableFuture` composition itself, but it was retained in the project's
implementation.

### 7.1 Step-by-step request flow

**Step 1 — The HTTP request arrives.**

Tomcat assigns a request worker, for example `http-nio-8080-exec-1`. That worker invokes the controller and calls
`fetchUserReport()`.

**Step 2 — The three tasks are submitted.**

The Tomcat thread calls the three `@Async` methods. Spring submits their bodies to `ThreadPoolTaskExecutor`. Each call
returns a `CompletableFuture<String>` after successful submission.

**Step 3 — `allOf()` creates a combined future.**

This combined future represents the completion of all three individual futures. It does **not** wait here. The Tomcat
thread can immediately move to the next line.

**Step 4 — `thenApply()` registers what should happen afterward.**

It tells Java: **“When all three operations succeed, run this function to create my report.”** Registration does not
wait for the tasks to finish.

**Step 5 — `whenComplete()` registers failure observation.**

It adds a callback that can log an exception if the report stage fails. It does not recover from that failure.

**Step 6 — The future returns to Spring MVC.**

The report service returns a `CompletableFuture<List<String>>`, and the controller returns it directly. The result list
may not yet exist. Spring MVC starts asynchronous request processing and the original Tomcat worker can be released once
that setup is done.

**Step 7 — The worker tasks finish.**

The three service operations finish according to executor availability and queueing. Java completes their respective
futures.

**Step 8 — The report continuation executes.**

Once all three succeed, the combined `allOf()` future completes successfully. That triggers the registered `thenApply()`
function. Inside the function, `join()` reads the already-completed values and creates the list.

**Step 9 — The returned report future completes.**

The `thenApply()` stage completes after its function returns the list. In this code, `whenComplete()` adds one more
stage; the final future returned by the chain completes after that callback finishes too.

**Step 10 — Spring MVC finishes the HTTP response.**

Spring MVC arranges an asynchronous servlet dispatch. An available Tomcat worker resumes response processing and
serializes the list to JSON. The original Tomcat worker does **not** need to be the same worker that handles this final
stage.

### 7.2 A visual flow

```mermaid
sequenceDiagram
    participant Client
    participant Tomcat as Tomcat request worker
    participant Report as UserReportService
    participant Pool as TestParallel workers
    participant MVC as Spring MVC

    Client->>Tomcat: GET /users/reports
    Tomcat->>Report: fetchUserReport()
    Report->>Pool: submit User, Order, Payment
    Note over Report: allOf() creates aggregate future
    Note over Report: thenApply() registers continuation
    Report-->>Tomcat: return incomplete report future
    Tomcat-->>MVC: controller returns future
    Note over Tomcat,MVC: MVC enables async handling; initial worker released
    Pool->>Pool: execute and finish service tasks
    Note over Pool: all three futures complete successfully
    Pool->>Report: trigger thenApply() continuation
    Note over Report: join() reads completed values
    Report-->>MVC: report future completes with List<String>
    MVC->>Tomcat: asynchronous servlet dispatch
    Tomcat-->>Client: HTTP JSON response
```

**Important:** In this diagram, the final `Tomcat` participant represents an **available** request-processing worker,
not necessarily the original one. The continuation often runs on the worker that completes the last outstanding service
future.

---

## 8. `allOf()`, `thenApply()`, and `join()` — the key concept

This is the most important part of the current implementation:

```java
return CompletableFuture.allOf(userFuture, orderFuture, paymentFuture)
        .

thenApply(ignoredParam ->{
        return List.

of(
        userFuture.join(),
                    orderFuture.

join(),
                    paymentFuture.

join()
            );
                    });
```

### 8.1 `allOf()` answers: **“Are all the tasks finished?”**

```java
CompletableFuture<Void> combinedFuture =
        CompletableFuture.allOf(userFuture, orderFuture, paymentFuture);
```

Java creates a **new** future that depends on the three supplied futures. Think of it as a completion checkpoint:

```text
userFuture    ------ completed ----\
                                 \
orderFuture   ------------ done ---+--> combinedFuture completes
                                 /
paymentFuture ------------------/
```

If all three tasks succeed, `combinedFuture` succeeds. If one or more fail, the combined future completes exceptionally
after the supplied futures have completed.

**Creating `combinedFuture` does not wait.** It merely sets up this relationship.

Its type is `CompletableFuture<Void>` because it represents completion, **not a list of the three results**.

### 8.2 `thenApply()` answers: **“What should happen after they all succeed?”**

```java
combinedFuture.thenApply(ignoredParam ->{
        // Build the report here.
        });
```

This registers a **dependent action**. Java's `CompletableFuture` machinery already knows that this action belongs to
`combinedFuture`.

If `combinedFuture` is still incomplete, the lambda is **not run yet**. No thread is sitting inside the lambda waiting
for the tasks. The action simply becomes eligible to run when the prerequisite future completes successfully.

When `combinedFuture` completes successfully, Java executes the registered function.

If the combined future was already complete before `thenApply()` was registered, the function may run immediately on the
thread registering it.

If the combined future completes exceptionally, the `thenApply()` function is **skipped** and its dependent future
completes exceptionally instead.

**`thenApply()` also returns a new future:** the result of the transformation, here a `CompletableFuture<List<String>>`.

### 8.3 `join()` answers: **“What value did this task produce?”**

```java
userFuture.join()
```

Normally, `CompletableFuture.join()` **can block**: if the future is incomplete, it waits for completion before
returning the value.

But look at **where** these three `join()` calls occur:

```java
combinedFuture.thenApply(ignoredParam ->{
        return List.

of(
        userFuture.join(),
            orderFuture.

join(),
            paymentFuture.

join()
    );
            });
```

The lambda executes only **after `combinedFuture` completes successfully**. And successful `combinedFuture` completion
guarantees that **all three supplied futures have already completed successfully**.

Therefore, at the moment the lambda starts:

```text
userFuture      COMPLETED -> user value is available
orderFuture     COMPLETED -> order value is available
paymentFuture   COMPLETED -> payment value is available
```

Each `join()` just reads an already-available value. **It does not have to wait again.**

This is not because `join()` magically became non-blocking. It is because **the code only calls it after the required
results are known to be ready**.

### 8.4 What would happen without `allOf()`?

Consider this different code:

```java
return userFuture.thenApply(user ->{
String order = orderFuture.join();
String payment = paymentFuture.join();

    return List.

of(user, order, payment);
});
```

Here `thenApply()` depends **only on `userFuture`**. If the user task finishes after 2 seconds, the lambda might start
while the order and payment tasks are still running.

In that case:

- `orderFuture.join()` **could block** until the order result is ready.
- `paymentFuture.join()` **could block** until the payment result is ready.

That is why the original `allOf()` dependency matters. It makes sure the continuation starts **after every required
operation succeeds**, avoiding a wait inside the report-building continuation.

### 8.5 Where does the continuation run?

The code uses **`thenApply()`**, not `thenApplyAsync()`.

The `thenApply()` function generally runs:

- On the thread that completes its preceding future, **or**
- On the thread registering it, if the preceding future is already complete.

For example, if `TestParallel_1` finishes the last outstanding task, the report-building lambda may also execute on
`TestParallel_1`.

`thenApply()` does **not** promise a new thread, and it does **not** necessarily use the original Tomcat thread.

### 8.6 Three separate future stages

It helps to give each stage a name:

```java
CompletableFuture<Void> combinedFuture =
        CompletableFuture.allOf(userFuture, orderFuture, paymentFuture);

CompletableFuture<List<String>> reportFuture =
        combinedFuture.thenApply(ignoredParam -> List.of(
                userFuture.join(), orderFuture.join(), paymentFuture.join()
        ));

CompletableFuture<List<String>> finalFuture =
        reportFuture.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("User Report generation failed", exception);
            }
        });

return finalFuture;
```

There are **three milestones**:

1. **`combinedFuture` completes:** all underlying service futures are done.
2. **`reportFuture` completes:** `thenApply()` has finished building and returning the list.
3. **`finalFuture` completes:** `whenComplete()` has finished observing the outcome.

The HTTP controller receives **`finalFuture`**, not the original `combinedFuture`.

---

## 9. Understanding the six-second report generation time

The configured executor has **two core workers**, not three always-available workers. It also has a queue slot.

For one request against an otherwise idle executor, the tasks are normally scheduled like this:

```text
Time                  0s       2s       3s                  6s
                      |--------|--------|--------------------|

TestParallel_1         [ User 2s ][      Payment 4s        ]

TestParallel_2         [    Order 3s    ]

Executor queue         [Payment waits] -> picked up at ~2s

Report future          incomplete ........................ done
```

### Why is the result about 6 seconds rather than 4 or 9?

1. At approximately **0 seconds**, the user task runs on worker 1.
2. At approximately **0 seconds**, the order task runs on worker 2.
3. The payment task is submitted next, but **both core workers are busy**. Because the queue has capacity for one task,
   payment is **queued** rather than immediately creating a third worker.
4. At approximately **2 seconds**, the user task finishes. Worker 1 picks up the queued payment task.
5. At approximately **3 seconds**, the order task finishes.
6. At approximately **6 seconds**, the payment task finishes (2 seconds waiting + 4 seconds executing).
7. Only then can `allOf()` complete successfully and trigger `thenApply()`.

So the nominal total is:

```text
2 seconds (User runs while Payment waits)
+ 4 seconds (Payment runs)
= approximately 6 seconds
```

The three delays do **not** add to 9 seconds because work overlaps. And the time is not simply 4 seconds because the
payment task has to **wait for a worker**.

**Why was `thenApply()` logged on `TestParallel_1`?** In the observed scheduling pattern, worker 1 finishes the user
task, then processes the queued payment task. Payment finishes last, so that worker can trigger the aggregate completion
and execute the continuation.

The exact worker suffixes and timestamps can change across runs. Under concurrent HTTP requests, tasks compete for the
same pool and queue, so timings can be much longer.

### What if three workers were immediately available?

With user, order, and payment all starting together on separate workers, the approximate critical path would be **4
seconds** (the duration of the slowest operation), plus small overhead.

That is an illustration of executor capacity, **not** what the queue-first scheduling of this idle two-core-worker
configuration normally does for its first three tasks.

---

## 10. How the executor schedules and rejects work

The current `ThreadConfig` values documented for the project are:

| Setting            |                            Value | Plain-English meaning                                               |
|--------------------|---------------------------------:|---------------------------------------------------------------------|
| Core pool size     |                              `2` | Up to two core workers start handling tasks as needed.              |
| Maximum pool size  |                              `3` | A third worker can be created under greater load.                   |
| Queue capacity     |                              `1` | One additional task can wait for a worker.                          |
| Keep-alive         |                      `5` seconds | An idle worker above the core size can be removed after this delay. |
| Thread-name prefix |                  `TestParallel_` | Makes async workers easy to recognize in logs.                      |
| Rejection handler  | `CustomRejectedExecutionHandler` | Handles an attempted submission when the pool and queue are full.   |

For a standard `ThreadPoolExecutor` with this configuration, the normal sequence is:

| Submitted task (when previous tasks are still busy) | What happens?                                                                                       |
|-----------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| Task 1                                              | Starts on core worker 1.                                                                            |
| Task 2                                              | Starts on core worker 2.                                                                            |
| Task 3                                              | Enters the one-slot queue.                                                                          |
| Task 4                                              | Queue is full; executor can create worker 3 (maximum size).                                         |
| Task 5                                              | If all three workers are occupied and the queue is full, the task is rejected and the handler runs. |

**Important:** A maximum pool size of `3` does **not** mean that the first three tasks automatically get three workers.
The executor normally **fills the queue before growing beyond the core pool size**.

### 10.1 What does the custom rejection handler do?

According to the documented implementation, it:

1. Logs that the task was rejected.
2. Sleeps for **one second**.
3. Attempts to resubmit the rejected task using `threadPoolExecutor.submit(runnable)`.

This raises an important question: **Which thread sleeps?**

The rejection handler is invoked during task submission. When the controller's Tomcat worker calls an `@Async` service,
it is the **Tomcat worker** that performs that submission. If submission is rejected, **that same Tomcat worker executes
the rejection handler and sleeps**.

So the current implementation is **non-blocking while waiting for accepted async results**, but it is **not guaranteed
to be non-blocking while submitting tasks**.

### 10.2 Why recursive resubmission is risky

Suppose the pool remains full after the one-second sleep:

```text
Original submission is rejected
    |
    v
Rejection handler sleeps for 1 second
    |
    v
Handler calls executor.submit(runnable)
    |
    v
Still full -> rejected again
    |
    v
Same rejection handler runs again, nested inside previous call
    |
    v
...potentially repeats...
```

That can repeatedly block the submitting thread, build up nested handler calls, and ultimately risk *
*`StackOverflowError`**. The documented handler has no retry limit or explicit shutdown handling. It also wraps an
interruption without restoring the interrupt flag.

This makes it useful for learning how saturation behaves, but it is **not a production-ready retry strategy**. The
current README documents that risk without changing the implementation.

### 10.3 What happens with multiple simultaneous HTTP requests?

Five concurrent clients may submit **15 service tasks** (3 per request) to the same executor.

All requests compete for:

- A maximum of **3 executor workers**.
- **1 queue slot**.
- The same rejection handler if submissions arrive while the pool and queue are full.

As a result, individual request times depend on scheduling, queue positions, and retries. They will not all necessarily
finish after six seconds. A request may also fail if a submission or callback encounters an exception.

---

## 11. Blocking vs. asynchronous MVC — side-by-side

| Question                                                    | Previous versions: controller returned `List<String>`                       | Current version: controller returns `CompletableFuture<List<String>>`       |
|-------------------------------------------------------------|-----------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| Do service tasks use `@Async`?                              | Yes.                                                                        | Yes.                                                                        |
| Who runs the service bodies?                                | `TestParallel_*` executor workers.                                          | `TestParallel_*` executor workers.                                          |
| Does the Tomcat worker wait for the results?                | **Yes**, in `get()` or `allOf().join()`.                                    | **No**, during normal future composition and waiting for accepted tasks.    |
| When does the controller return?                            | After the list has been assembled.                                          | After obtaining the composed future, even if the list is not ready.         |
| What does Spring MVC receive?                               | A completed `List<String>`.                                                 | A `CompletableFuture<List<String>>`.                                        |
| Can the original Tomcat worker be released while tasks run? | No, it is blocked collecting results.                                       | Yes, after Spring MVC starts asynchronous request processing.               |
| Does each report's underlying work get faster?              | Depends on worker availability.                                             | **Not inherently.** Same service delays and executor limits.                |
| Is the HTTP response generated without a Tomcat worker?     | No.                                                                         | No. A later async dispatch still needs servlet processing.                  |
| Can submission block under executor saturation?             | Yes, with the rejection handler.                                            | **Yes, still.**                                                             |
| How do task failures affect the result?                     | Earlier service code caught failures and could return a partial/empty list. | Failures propagate through the composed future; `whenComplete()` logs them. |

**Main takeaway:** The benefit is that the initial Tomcat worker does not stay occupied simply waiting for the accepted
tasks to finish. That can help an application handle more simultaneous waiting requests, provided the executor and
servlet container have sufficient capacity.

It does **not** mean unlimited concurrency or a faster individual service call.

---

## 12. `CompletableFuture` method reference

| Method                             | What it means                                                             | Does calling it wait?                                           | Result or behavior                                                                         |
|------------------------------------|---------------------------------------------------------------------------|-----------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| `CompletableFuture.allOf(a, b, c)` | Create one future representing the completion of all inputs.              | **No.**                                                         | Returns `CompletableFuture<Void>`.                                                         |
| `allOf(a, b, c).join()`            | Wait until the combined future completes, then observe the outcome.       | **Yes, if incomplete.**                                         | Returns `Void` (`null`) on success; throws unchecked exception on failure.                 |
| `future.get()`                     | Retrieve the value, waiting if needed.                                    | **Yes, if incomplete.**                                         | Returns `T`; throws checked `InterruptedException` and `ExecutionException` as applicable. |
| `future.join()`                    | Retrieve the value, waiting if needed.                                    | **Yes, if incomplete.**                                         | Returns `T`; typically throws unchecked `CompletionException` for failure.                 |
| `thread.join()`                    | Wait for a **thread to terminate**.                                       | **Yes, if the thread is still alive.**                          | Returns `void`; unrelated to `CompletableFuture.join()`.                                   |
| `thenApply(fn)`                    | Run a transformation after successful completion of the preceding future. | **Does not wait for an incomplete future during registration.** | Returns a dependent `CompletableFuture<U>`. Lambda may run inline if already complete.     |
| `thenApplyAsync(fn)`               | Schedule a transformation asynchronously after successful completion.     | **No waiting during registration.**                             | Uses the common pool by default, or a specified executor.                                  |
| `whenComplete(action)`             | Observe success or failure, usually for side effects such as logging.     | **Does not wait for an incomplete future during registration.** | Returns another stage; normally preserves the previous outcome.                            |

### `get()` vs `join()`: a simple rule

Both retrieve values, and **both can block** if the future is incomplete.

The main difference is exception handling:

```java
String value = userFuture.get();  // Checked exceptions must be handled/declared.
String value2 = userFuture.join(); // No checked-exception requirement.
```

`join()` does **not** eliminate errors. It reports failures through unchecked exceptions, commonly
`CompletionException`; cancellation has its own `CancellationException` behavior.

### `thenApply()` vs `thenApplyAsync()`: a simple rule

`thenApply()` attaches a continuation without requiring a separate scheduling step. The callback might execute on the
completing thread, or on the registering thread if the previous stage is already complete.

`thenApplyAsync()` schedules the callback onto an executor. If you do not supply one, it normally uses the
`ForkJoinPool.commonPool()`.

For this project's tiny report assembly operation — constructing a three-element list — the non-`Async` `thenApply()` is
sufficient. Using `thenApplyAsync()` would not make the already-running service tasks faster.

---

## 13. What happens when something fails?

### 13.1 A service task fails asynchronously

Suppose `paymentFuture` completes exceptionally:

```text
User task    -> completed successfully
Order task   -> completed successfully
Payment task -> completed exceptionally
                    |
                    v
                allOf() completes exceptionally
                    |
                    v
                thenApply() is SKIPPED
                    |
                    v
                whenComplete() logs the failure
                    |
                    v
                returned future remains exceptional
                    |
                    v
                Spring MVC handles the failed async result
```

Crucially, the code does **not** enter the `thenApply()` lambda in this case, so the individual `join()` calls inside it
are **not executed**.

`whenComplete()` is for **observation**, not recovery. Logging an exception there does not magically turn it into a
valid `List<String>`.

If the `whenComplete()` callback itself throws, that can also influence the outcome of the future it returns.

### 13.2 A task submission fails synchronously

There is a different failure point **before** the future chain is created.

For example, if the rejection handler throws while executing `paymentService.getPaymentInfo()`, `fetchUserReport()` may
exit immediately with that exception. In this case, the later `allOf(...).thenApply(...).whenComplete(...)` chain was
never registered.

That is a **synchronous submission failure**, not an asynchronous failure reported through the eventual report future.
Normal synchronous MVC exception handling applies.

Already-submitted user/order tasks might still be running even if the later payment submission fails.

### 13.3 An asynchronous HTTP request can time out

Asynchronous MVC request handling may have a timeout configured by Spring or the servlet container. This project does
not document an application-specific timeout value.

If a report takes too long, the HTTP request may time out even though executor tasks continue running. Returning a
future does not itself guarantee task cancellation when an HTTP timeout happens.

### 13.4 What if Tomcat workers are busy at the end?

Completing the report future does **not** bypass the servlet container. Spring MVC schedules an asynchronous dispatch so
that an available Tomcat worker can process the result and send the response.

If no Tomcat worker is currently available, that dispatch must wait for capacity (subject to the application's timeouts
and container limits). The already-computed report is not lost merely because response processing has not started yet.

---

## 14. Reading the thread logs

The logs help answer two questions:

1. **Which thread submitted the tasks?** Look for `http-nio-8080-exec-*` in the controller or report service.
2. **Which thread executed the tasks and continuation?** Look for `TestParallel_*` in the individual services and report
   completion log.

The email logs use `Task_Scheduler_*`. The first message records when the task is scheduled. The later message records
the scheduler thread and the actual execution time.

An example of the **sequence of events** for a single otherwise-idle request is:

```text
http-nio-8080-exec-1  -> controller and report service invoked
TestParallel_1        -> User task starts
TestParallel_2        -> Order task starts
                        Payment task waits in queue
TestParallel_1        -> User completes after about 2 seconds
TestParallel_1        -> Payment task starts
TestParallel_2        -> Order completes after about 3 seconds
TestParallel_1        -> Payment completes at about 6 seconds
TestParallel_1        -> thenApply() assembles the report
                        Spring MVC handles asynchronous response dispatch
```

These are **illustrative events**, not copied log lines or guaranteed thread IDs. Thread scheduling may change.

### What the elapsed-time log includes

If `start` is recorded at the beginning of `fetchUserReport()` and elapsed time is logged from inside `thenApply()`,
that measurement includes:

- Time spent submitting tasks.
- Time blocked by any submission-time rejection retry.
- Waiting in the executor queue.
- Actual service execution.
- Assembling the final list, up to the timestamp at the log call.

It does **not** measure final HTTP dispatch, JSON serialization, network transmission, or client download time. The
client-side `curl %{time_total}` value measures something different: the full HTTP transaction from the client's
perspective.

---

## 15. Run and test

### 15.1 Compile

From the project root:

```bash
./mvnw clean compile
```

### 15.2 Start Spring Boot

```bash
./mvnw spring-boot:run
```

### 15.3 Send one request

```bash
curl -i http://localhost:8080/users/reports
```

On success, expect a JSON array with **three strings**, representing user, order, and payment information.

The exact string contents are defined by the service implementations.

### 15.4 Send five concurrent requests

```bash
seq 5 | xargs -P 5 -I {} curl -sS -o /dev/null \
  -w "HTTP %{http_code} | Time %{time_total}s\n" \
  http://localhost:8080/users/reports
```

**How to read the command:**

- `seq 5` creates five request iterations.
- `xargs -P 5` allows up to five `curl` processes to run concurrently.
- `-o /dev/null` suppresses response bodies.
- `%{http_code}` prints each HTTP status code.
- `%{time_total}` prints the request's total client-observed duration.

Do **not** assume every response will take the same time or return HTTP 200 under saturation. This test intentionally
stresses a small executor and its potentially unsafe rejection/retry behavior.

When investigating the output, compare it with the server's `TestParallel_*`, rejection, retry, and report-completion
logs.

### 15.5 Schedule one email log

```bash
curl -i -X POST http://localhost:8080/emails/user@example.com
```

The endpoint returns HTTP `204 No Content` after registering the task. About ten seconds later, check the application
logs for the message executed by a `Task_Scheduler_*` thread.

---

## 16. Common misconceptions

**“The three service methods have `@Async`, so the HTTP request must be non-blocking.”**

No. The earlier versions used `@Async` but still blocked the Tomcat worker in `get()` or `allOf().join()` before
returning the response.

**“`allOf()` waits until everything finishes.”**

It creates a future that **represents** that event; calling `allOf()` itself does not block. Calling `join()` on the
future can block.

**“`thenApply()` starts running immediately in the background.”**

Not necessarily. It **registers a dependent action**. That action runs when the preceding future completes successfully,
or may run immediately if the preceding future was already successful at registration time.

**“`join()` is always non-blocking.”**

No. It blocks on an incomplete future. In this implementation, it **doesn't need to wait** because the successful
`allOf()` completion guarantees the values are ready before the lambda reads them.

**“Returning a future means the HTTP response is already sent.”**

No. It means the controller handed Spring MVC a representation of a future result. Spring MVC sends the HTTP response
after that future completes and response processing occurs.

**“A maximum pool size of three means the first three tasks all run immediately.”**

No. With a core size of two and queue capacity of one, the third task normally waits in the queue first. The executor
grows beyond the core size when the queue is full and more work arrives.

**“This change makes the report finish faster.”**

Not inherently. The two-, three-, and four-second operations still run under the same executor constraints. The main
change is that the original Tomcat worker need not remain blocked while waiting for them.

---

## 17. Final takeaways

1. **`@Async` moves service execution to an executor.** It does not automatically make the HTTP boundary asynchronous.
2. **`allOf()` coordinates completion without blocking** the caller.
3. **`thenApply()` registers the next step:** assemble the list after every required future succeeds.
4. **`join()` normally can wait, but here just retrieves ready values** because the lambda runs only after successful
   `allOf()` completion.
5. **Returning `CompletableFuture<List<String>>` lets Spring MVC free the original request worker** after setting up
   asynchronous request handling.
6. **The response still requires servlet processing later** when the future completes.
7. **Executor limits still matter.** Two core workers, one queue slot, and the rejection handler explain the nominal
   six-second single-report schedule and possible delays under load.
8. **The custom rejection handler can block the submitting Tomcat thread**, even in the current asynchronous response
   design.
9. **`TaskScheduler.schedule()` registers one task for a chosen time.** It returns immediately, and a `Task_Scheduler_*`
   worker executes the action when it becomes due.
10. **`@Scheduled` is for automatic repeated or cron-based execution.** It is described for comparison but is not
    enabled or used by the current code.

> **Remember the flow:** submit the three tasks → create the `allOf()` dependency → register the `thenApply()` action →
> return the future → finish the tasks → read their completed values → complete the report future → let Spring MVC send
> the response.
