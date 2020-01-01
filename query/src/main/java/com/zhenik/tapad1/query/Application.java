package com.zhenik.tapad1.query;

import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import java.util.concurrent.CompletableFuture;

public class Application {
  public static void main(String[] args) {
    /* Config */
    final Config config = Config.loadConfiguration();

    /* http server */
    ServerBuilder sb = Server.builder();
    sb.http(config.port);
    // GET /analytics?timestamp={millis_since_epoch}
    sb.annotatedService(new HttpService(new QueryRedisService(config.redisUri)));
    Server server = sb.build();
    CompletableFuture<Void> future = server.start();
    future.join();
  }
}
