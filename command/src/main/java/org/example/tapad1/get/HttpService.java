package org.example.tapad1.get;

import com.linecorp.armeria.common.HttpParameters;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.server.annotation.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpService {
  private static final Logger log = LoggerFactory.getLogger(HttpService.class);

  @Post("/analytics")
  public HttpResponse addRecord(HttpParameters parameters) {
    log.info("params: {}", parameters.toString());
    return HttpResponse.of(HttpStatus.OK);
  }
}
