package org.example.tapad1.get;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.server.Server;
import com.linecorp.armeria.server.ServerBuilder;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Application {
  public static void main(String[] args) {
    String id = UUID.randomUUID().toString();

    final AgentClient agentClient = registerServiceDiscovery(id);

    ServerBuilder sb = Server.builder();
    sb.http(8081);
    // Add a simple 'Hello, world!' service.
    sb.service("/", (ctx, res) -> HttpResponse.of("Hello, world! from "+id));
    Server server = sb.build();
    CompletableFuture<Void> future = server.start();
    future.join();

    CompletableFuture.supplyAsync(
            () -> {
                    try {
                      while (true) {
                        Thread.sleep(1_000);
                        agentClient.pass(id);
                        //System.out.println("Pass");
                      }
                    } catch (NotRegisteredException | InterruptedException e) {
                      e.printStackTrace();
                      server.stop();
                    }
              return null;
        });
        //.exceptionally(
        //    throwable -> {
        //      System.out.println("print exception from child thread in main");
        //      throwable.printStackTrace();
        //      return throwable;
        //    })
        //.thenApply(ignored -> {
        //  System.out.println("Server is going to shutdown 'thenApply' ");
        //  return server.stop(); // will trigger also shutdown line 16-19
        //})
        //.thenAccept(CompletableFuture::join); // not necessary

  }

  private static AgentClient registerServiceDiscovery(String serviceId) {
    Consul client = Consul.builder().build();
    AgentClient agentClient = client.agentClient();

    Registration service = ImmutableRegistration.builder()
        .id(serviceId)
        .name("application-tapad1-get")
        .port(8081)
        .address("localhost")
        .check(Registration.RegCheck.ttl(10)) // registers with a TTL of 3 seconds
        .tags(Collections.singletonList("tapad1-get"))
        .meta(Collections.singletonMap("version", "1.0"))
        .build();

    agentClient.register(service);


    // Check in with Consul (serviceId required only).
    // Client will prepend "service:" for service level checks.
    // Note that you need to continually check in before the TTL expires, otherwise your service's state will be marked as "critical".
    try {
      agentClient.pass(serviceId);
    } catch (NotRegisteredException e) {
      e.printStackTrace();
    }

    return agentClient;
  }
}
