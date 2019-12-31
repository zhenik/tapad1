# Tech task
[Task link](https://gist.github.com/jeffreyolchovy/a961dfc3570b7e9004a9df4ac6290963)

## Solution
![img](todo)

Eventual consistency. Async writes, Sync reads.

1. `command module` - http server, kafka producer. Post requests. Data validation on ingestion, produce record. 
2. `processor module` - kafka streams application. Business logic (analytics - aggregation)
3. `materializer module` - kafka streams application, redis api. Downstream analytics redis.
4. `query module` - http server, redis api. Get requests. Query redis (cache)  

### Limitations
* processor and materializer scale up to number of partitions
* CAP theorem
 
### Improvements
1. External kafka config
2. Serde with Avro, Protobuf, ...
3. Materializer flow control (throttling, buffering, non-blocking). Reactive
4. Cache ttl control and additional database for long term storing
5. Health-checks, circuit breaker
6. Container orchestration (k8s, nomad)
7. Proxy, gateway on front
8. Service discovery
9. Schema management (AVRO -> schema registry) 
