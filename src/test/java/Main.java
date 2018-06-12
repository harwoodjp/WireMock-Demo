import com.github.tomakehurst.wiremock.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class Main {

    private String convertResponseToString(HttpResponse response) throws IOException {
        InputStream responseStream = response.getEntity().getContent();
        Scanner scanner = new Scanner(responseStream, "UTF-8");
        String responseString = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return responseString;
    }

    @Test
    public void wireMockServerTest() {
        Logger logger = LoggerFactory.getLogger(Main.class);

        WireMockServer wireMockServer = new WireMockServer();
        wireMockServer.start();

        configureFor("localhost", 8080);
        stubFor(get(urlEqualTo("/foo")).willReturn(aResponse().withBody("bar")));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet("http://localhost:8080/foo");
        HttpResponse httpResponse;

        try {
            httpResponse = httpClient.execute(request);
            String stringResponse = convertResponseToString(httpResponse);
            verify(getRequestedFor(urlEqualTo("/foo")));
            Assert.assertEquals("bar", stringResponse);
        } catch (IOException e) {
            logger.info(e.toString());
        } finally {
            wireMockServer.stop();
        }

    }
}