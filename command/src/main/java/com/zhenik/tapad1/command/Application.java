package com.zhenik.tapad1.command;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import java.util.concurrent.CompletableFuture;

public class Application {
  public static void main(String[] args) {
    /* Config */
    final Config config = Config.loadConfiguration();

    /* Service discovery */
    //Consul client = Consul.builder().build();
    //AgentClient agentClient = client.agentClient();
    //Registration service =
    //    ImmutableRegistration.builder()
    //        .id(config.name)
    //        .name("app-tapad-read")
    //        .port(config.port)
    //        .address(config.host)
    //        .check(Registration.RegCheck.ttl(10)) // registers with a TTL of 3 seconds
    //        .tags(Collections.singletonList("tapad-read"))
    //        .meta(Collections.singletonMap("version", "1.0"))
    //        .build();
    //agentClient.register(service);

    /* http server */

    ServerBuilder sb = Server.builder();
    sb.http(config.port);
    // Add a simple 'Hello, world!' service.
    sb.service("/", (ctx, res) -> HttpResponse.of("Hello, world! from " + config.clientId));
    // POST /analytics?timestamp={millis_since_epoch}&user={username}&{click|impression}
    sb.annotatedService(new HttpService(new CommandKafkaProducer(config)));
    Server server = sb.build();
    CompletableFuture<Void> future = server.start();
    future.join();

    /* health check */
    //CompletableFuture.supplyAsync(
    //    () -> {
    //      try {
    //        while (true) {
    //          Thread.sleep(3_000);
    //          agentClient.pass(config.name);
    //        }
    //      } catch (NotRegisteredException | InterruptedException e) {
    //        e.printStackTrace();
    //        server.stop();
    //      }
    //      return null;
    //    });
  }
}
