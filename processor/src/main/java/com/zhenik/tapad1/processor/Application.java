package com.zhenik.tapad1.processor;

public class Application {

  public static void main(String[] args) throws Exception {
    final Config config = Config.loadConfiguration();
    final AnalyticsKafkaProcessor service = new AnalyticsKafkaProcessor(config);
    service.start();
    addShutdownHookAndBlock(service);
  }

  public static void addShutdownHookAndBlock(final AnalyticsKafkaProcessor service) throws InterruptedException {
    Thread.currentThread().setUncaughtExceptionHandler((t, e) -> service.stop());
    Runtime.getRuntime().addShutdownHook(
        new Thread(() -> {
          try { service.stop(); }
          catch (final Exception ignored) { }
        }));
    Thread.currentThread().join();
  }

}
