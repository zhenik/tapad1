package com.zhenik.tapad1.query;

import com.linecorp.armeria.common.HttpParameters;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.annotation.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.zhenik.tapad1.schema.Schema.TIMESTAMP;

public class HttpService {
  private static final Logger log = LoggerFactory.getLogger(HttpService.class);

  private final QueryRedisService queryRedisService;

  public HttpService(QueryRedisService queryRedisService) {
    this.queryRedisService = queryRedisService;
  }

  // GET /analytics?timestamp={millis_since_epoch}
  @Get("/analytics")
  public HttpResponse query(HttpParameters parameters) {
    if (valid(parameters)) {
      final long epoch = parameters.getLong(TIMESTAMP);
      log.info("query timestamp: {}", epoch);
      return HttpResponse.of(queryRedisService.get(String.valueOf(epoch)));
    } else {
      log.error("params not valid: {}", parameters.toString());
      return HttpResponse.of(HttpStatus.BAD_REQUEST);
    }
  }

  /** Http query params validation */
  private boolean valid(final HttpParameters parameters) {
    if (parameters.size() != 1) {
      return false;
    } else if (parameters.contains(TIMESTAMP)) {
      long epoch = parameters.getLong(TIMESTAMP);
      log.info("query timestamp: {}", epoch);
      return true;
    } else {
      return false;
    }
  }
}
