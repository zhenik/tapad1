package org.example.tapad1.get;

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import java.util.Collections;

public class ServiceDiscovery implements Runnable {
  private AgentClient agentClient;
  private String serviceId;

  public ServiceDiscovery(String serviceId) {
    this.serviceId = serviceId;
    Consul client = Consul.builder().build();
    this.agentClient = client.agentClient();

    Registration service =
        ImmutableRegistration.builder()
            .id(serviceId)
            .name("application-tapad1-get")
            .port(8080)
            .check(Registration.RegCheck.ttl(3L)) // registers with a TTL of 3 seconds
            .tags(Collections.singletonList("tapad1-get"))
            .meta(Collections.singletonMap("version", "1.0"))
            .build();

    agentClient.register(service);


    // Check in with Consul (serviceId required only).
    // Client will prepend "service:" for service level checks.
    // Note that you need to continually check in before the TTL expires, otherwise your service's
    // state will be marked as "critical".

    try {
      agentClient.pass(serviceId);
    } catch (NotRegisteredException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    //Thread.sleep(3_000);
    //agentClient.pass(serviceId);
  }
}
