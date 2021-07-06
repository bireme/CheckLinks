package org.bireme.cl;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;

public class MyResponseHandler implements ResponseHandler<Integer> {
    private String location = null;

    @Override
    public Integer handleResponse(final HttpResponse response) {
        final Header header = response.getFirstHeader("Location");
        if (header != null) {
            location = header.getValue();
        }
        return response.getStatusLine().getStatusCode();
    }

    public String handleLocation() {
        return location;
    }
}
