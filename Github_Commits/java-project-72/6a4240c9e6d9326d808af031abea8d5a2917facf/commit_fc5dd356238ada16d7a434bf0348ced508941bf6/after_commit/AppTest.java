package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;

//import com.mashape.unirest.http.Unirest;
//import com.mashape.unirest.http.exceptions.UnirestException;
import hexlet.code.database.UrlChecksRepository;
import hexlet.code.database.UrlsRepository;
import hexlet.code.model.Url;
import hexlet.code.util.Routes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
//import java.net.URI;
import java.sql.SQLException;

class AppTest {
    private Javalin app;
    private MockWebServer mockServer;

    @BeforeEach
    public final void setUp() throws SQLException, IOException {
        app = App.getApp();
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterEach
    public final void shutdown() throws IOException {
        mockServer.shutdown();
        app.close();
    }

    @Test
    public void testMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(Routes.rootPath());
            assertThat(response.code()).isEqualTo(200);
            String responseBody = response.body().string();
            assertThat(responseBody.contains("<form"));
            assertThat(responseBody.contains("name=\"url\""));
        });
    }

    @Test
    public void testEmptyUrlsPage() {
        JavalinTest.test(app, ((server, client) -> {
            var response = client.get(Routes.urlsPath());
            assertThat(response.code()).isEqualTo(200);
        }));
    }

    @ParameterizedTest
    @ValueSource(strings = {"github.com", "www.github.com", "https://github.com", "https://www.github.com"})
    public void testCreateUrl(String url) {
        JavalinTest.test(app, ((server, client) -> {
            var requestBody = "url=" + url;
            var response = client.post(Routes.urlsPath(), requestBody);
            assertThat(response.code()).isEqualTo(200);
            var responseBody = response.body().string();
            assertThat(responseBody.contains("https://github.com"));
        }));
    }

    @ParameterizedTest
    @ValueSource(strings = {"github.com:8000", "www.github.com:8000", "https://github.com:8000",
                            "https://www.github.com:8000"})
    public void testCreateUrlWithPort(String url) {
        JavalinTest.test(app, ((server, client) -> {
            var requestBody = "url=" + url;
            var response = client.post(Routes.urlsPath(), requestBody);
            assertThat(response.code()).isEqualTo(200);
            var responseBody = response.body().string();
            assertThat(responseBody.contains("https://github.com:8000"));
        }));
    }

    @Test
    public void testCreateInvalidUrl() {
        JavalinTest.test(app, (server, client) -> {
            client.post(Routes.urlsPath(), "url=google1.com");
            client.post(Routes.urlsPath(), "url=google2.com");
            client.post(Routes.urlsPath(), "url=google3.com");

            client.post(Routes.urlsPath(), "url=github");

            var responseBody = client.get(Routes.urlsPath()).body().string();
            var doc = Jsoup.parse(responseBody);
            var ulElements = doc.select("ul");

            boolean containsInvalidUrl = ulElements.stream()
                    .anyMatch(ul -> ul.text().contains("github"));

            assertThat(containsInvalidUrl).isFalse();
        });
    }

    @Test
    public void testUrlPage() throws SQLException {
        var url = new Url("https://github.com");
        UrlsRepository.save(url);
        JavalinTest.test(app, ((server, client) -> {
            var response = client.get(Routes.urlPath(url.getId()));
            assertThat(response.code()).isEqualTo(200);
        }));
    }

    @Test
    public void testInvalidUrlPage() {
        JavalinTest.test(app, ((server, client) -> {
            var response = client.get(Routes.urlPath("1234"));
            assertThat(response.code()).isEqualTo(404);
        }));
    }

    @Test
    public void testUrlCheck() {
        var mockServerUrl = mockServer.url("/").toString();

        var mockResponse = new MockResponse();
        var mockContent = "<meta name=\"description\" content=\"some description\">\n"
                + "<title>some title</title>\n"
                + "<h1>some header</h1>\n";
        mockResponse.setBody(mockContent);
        mockServer.enqueue(mockResponse);

        JavalinTest.test(app, ((server, client) -> {
            var url = new Url(mockServerUrl);
            UrlsRepository.save(url);
            var id = url.getId();

            var response = client.post(Routes.checkUrlPath(id));
            assertThat(response.code()).isEqualTo(200);
            var responseBody = response.body().string();
            assertThat(responseBody.contains("200"));
            assertThat(responseBody.contains("some title"));
            assertThat(responseBody.contains("some header"));
            assertThat(responseBody.contains("some description"));

            var urlCheck = UrlChecksRepository.getEntities(1L).get(0);
            assertThat(urlCheck.getId() == 1L);
            assertThat(urlCheck.getUrlId().equals(id));
            assertThat(urlCheck.getCreatedAt()).isNotNull();
        }));
    }

//    @Test
//    public void testInvalidUrlCheck() throws SQLException, UnirestException {
//        var url = new Url("invalid_url.invalid_dom");
//        UrlsRepository.save(url);
//        var id = url.getId();
//
//        var response = Unirest.post("http://localhost:7070" + Routes.checkUrlPath(id)).asJson();
//        var redirectUrl = response.getHeaders().get("location").get(0);
//        if (redirectUrl != null) {
//            URI redirectUri = URI.create(redirectUrl);
//            response = Unirest.get(redirectUri.toString()).asJson();
//        }
//
//        String responseBody = response.getBody().toString();
//        assertThat(responseBody).contains("Не удалось выполнить проверку");
//    }
}
