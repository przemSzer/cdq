package com.cdq.countries.rest;

import java.io.IOException;
import java.net.URI;

public interface HttpGetter {

    HttpResult get(URI uri) throws IOException, InterruptedException;
}
