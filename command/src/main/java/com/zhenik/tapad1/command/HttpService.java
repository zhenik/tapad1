package com.zhenik.tapad1.command;

import com.linecorp.armeria.common.HttpParameters;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.annotation.Post;
import com.zhenik.tapad1.command.messaging.CommandKafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.zhenik.tapad1.schema.Schema.*;

public class HttpService {
  private static final Logger log = LoggerFactory.getLogger(HttpService.class);

  private final CommandKafkaProducer commandKafkaProducer;

  public HttpService(CommandKafkaProducer commandKafkaProducer) {
    this.commandKafkaProducer = commandKafkaProducer;
  }

  @Post("/analytics")
  public HttpResponse addCommand(HttpParameters parameters) {
    log.info("params: {}", parameters.toString());
    if (valid(parameters)) {
      final long epoch = parameters.getLong(TIMESTAMP);
      final String user = parameters.get(USER).trim();
      final String action = getAction(parameters);
      log.info("timestamp: {} ; user: {} ; action: {}", epoch, user, action);
      commandKafkaProducer.send(String.valueOf(epoch), user, action);
      return HttpResponse.of(HttpStatus.ACCEPTED);
    } else {
      return HttpResponse.of(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Http query params validation 1. params size 2. keys are present 3. valid values ...
   * todo: whitespaces (as a delimiter); epoch as actually past time (not future); null checks; etc...
   */
  private boolean valid(final HttpParameters parameters) {
    if (parameters.size() != 3) {
      return false;
    } // query keys size

    if (parameters.contains(TIMESTAMP)
        && parameters.contains(USER)
        && (parameters.contains(CLICK)
            || parameters.contains(IMPRESSION)) // all keys are present
    ) {
      long epoch = parameters.getLong(TIMESTAMP);
      String user = parameters.get(USER).trim();
      log.info("user: {} ; timestamp: {}", user, epoch);
      return true;
    }
    return false;
  }

  private String getAction(HttpParameters parameters) {
    if (parameters.contains(CLICK)) { return CLICK; }
    if (parameters.contains(IMPRESSION)) {return IMPRESSION; }
    throw new IllegalArgumentException("Unknown third parameter(action) in query parameters " + parameters.toString());
  }
}
