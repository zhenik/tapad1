package com.zhenik.tapad1.materializer;

public class Application {
  public static void main(String[] args) throws InterruptedException {
    final Config config = Config.loadConfiguration();
    final KafkaRedisMaterializer service = new KafkaRedisMaterializer(config);
    service.start();
    addShutdownHookAndBlock(service);
  }

  public static void addShutdownHookAndBlock(final KafkaRedisMaterializer service)
      throws InterruptedException {
    Thread.currentThread().setUncaughtExceptionHandler((t, e) -> service.stop());
    Runtime.getRuntime().addShutdownHook(
        new Thread(() -> {
          try { service.stop(); }
          catch (final Exception ignored) { }
        }));
    Thread.currentThread().join();
  }
}
