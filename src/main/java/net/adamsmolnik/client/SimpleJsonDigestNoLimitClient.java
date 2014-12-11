package net.adamsmolnik.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * @author ASmolnik
 *
 */
public class SimpleJsonDigestNoLimitClient {

    private static final String REQUEST_TEMPLATE = "{\"type\":\"digestRequest\",\"id\":\"1234567890\",\"algorithm\":\"%s\",\"objectKey\":\"%s\"}";

    private final String url;

    public SimpleJsonDigestNoLimitClient(String host) {
        this.url = "http://" + host + "/digest-service-no-limit/ds/digest";
    }

    public String send(String algorithm, String objectKey, int timeoutInSec) throws IOException {
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        int timeoutInMs = timeoutInSec * 1000;
        con.setConnectTimeout(timeoutInMs);
        con.setReadTimeout(timeoutInMs);
        con.setConnectTimeout(timeoutInSec * 1000);
        con.setRequestMethod("POST");
        con.addRequestProperty("Content-Type", "application/json");
        con.addRequestProperty("Accept", "text/html; q=.2, */*; q=.2");
        String request = String.format(REQUEST_TEMPLATE, algorithm, objectKey).toString();
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            os.write(request.getBytes(StandardCharsets.UTF_8));
            try (InputStream is = con.getInputStream()) {
                JsonParser parser = Json.createParser(is);
                while (parser.hasNext()) {
                    Event event = parser.next();
                    String key = null;
                    if (Event.KEY_NAME == event && "digest".equals(key = parser.getString())) {
                        response.append(key);
                        response.append("=");
                        Event eventValue = parser.next();
                        if (Event.VALUE_STRING == eventValue) {
                            response.append(parser.getString());
                        }
                    }
                }
            }

        }
        return response.toString();
    }

    public static void main(String[] args) throws Exception {
        String algorithm = "SHA-256";
        String objectKey = "largefiles/file_sizedOf10000000";
        System.out.println(new SimpleJsonDigestNoLimitClient("digest.adamsmolnik.com").send(algorithm, objectKey, 30));

    }

}
