package net.adamsmolnik.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author ASmolnik
 *
 */
public class SimpleJsonDigestNoLimitUnderHeavyLoadClient {

    public static void main(String[] args) throws Exception {
        final SimpleJsonDigestNoLimitClient client = new SimpleJsonDigestNoLimitClient("elb-student099-189780020.us-east-1.elb.amazonaws.com");
        final String algorithm = "SHA-256";
        final String objectKey = "largefiles/file_sizedOf10000000";
        ExecutorService es = null;
        try {
            es = Executors.newFixedThreadPool(20);
            for (int i = 0; i < 2000; i++) {
                es.submit(() -> {
                    String response;
                    try {
                        response = client.send(algorithm, objectKey, 30);
                        System.out.println("response: " + response);
                    } catch (Exception e) {
                        System.out.println(e.getLocalizedMessage());
                    }
                });
                TimeUnit.MILLISECONDS.sleep(10);
            }
            TimeUnit.MINUTES.sleep(5);
        } finally {
            if (es != null) {
                es.shutdownNow();
            }
        }
    }

}
