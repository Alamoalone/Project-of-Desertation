package hexlet.code.controller;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import hexlet.code.database.UrlChecksRepository;
import hexlet.code.database.UrlsRepository;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.util.Routes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.jsoup.Jsoup;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collections;

public class UrlsController {
    public static void index(Context ctx) throws SQLException {
        var urls = UrlsRepository.getEntities();
        var page = new UrlsPage(urls);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("urls/index.jte", Collections.singletonMap("page", page));
    }

    public static void create(Context ctx) throws SQLException {
        var userInput = ctx.formParam("url").trim().toLowerCase();

        try {
            if (!userInput.startsWith("http://") && !userInput.startsWith("https://")) {
                userInput = "https://" + userInput;
            }

            URI uri = new URI(userInput);
            var scheme = uri.getScheme();
            var host = uri.getHost();
            var port = uri.getPort();

            if (host != null && host.contains(".")) {
                if (host.startsWith("www.")) {
                    host = host.substring(4);
                }

                StringBuilder sb = new StringBuilder();
                sb.append(scheme).append("://").append(host);
                if (port != -1) {
                    sb.append(":").append(port);
                }

                var url = new Url(sb.toString());

                if (UrlsRepository.findByName(url.getName()).isEmpty()) {
                    UrlsRepository.save(url);
                    ctx.sessionAttribute("flash", "Страница успешно добавлена"); // TODO check message
                    ctx.sessionAttribute("flashType", "success");
                    ctx.redirect(Routes.urlsPath());

                } else {
                    ctx.sessionAttribute("flash", "Страница уже существует"); // TODO check message
                    ctx.sessionAttribute("flashType", "info");
                    ctx.redirect(Routes.urlsPath());
                }

            } else {
                throw new URISyntaxException(userInput, "Invalid URL");
            }
        } catch (URISyntaxException e) {
            ctx.sessionAttribute("flash", "Некорректный URL"); // TODO check message
            ctx.sessionAttribute("flashType", "danger");
            ctx.redirect(Routes.rootPath());
        }
    }

    public static void show(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        var url = UrlsRepository.findById(id)
                .orElseThrow(() -> new NotFoundResponse("Page not found")); // TODO mb create not found page?
        var urlChecks = UrlChecksRepository.getEntities(url.getId());
        var page = new UrlPage(url, urlChecks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setFlashType(ctx.consumeSessionAttribute("flashType"));
        ctx.render("urls/show.jte", Collections.singletonMap("page", page));
    }

    public static void check(Context ctx) throws SQLException {
        var urlId = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        var url = UrlsRepository.findById(urlId)
                .orElseThrow(() -> new NotFoundResponse("Page not found")); // TODO mb create not found page?
        try {
            var response = Unirest.get(url.getName()).asString();
            var code = response.getStatus();

            var doc = Jsoup.parse(response.getBody());
            var title = doc.title();
            var h1 = doc.selectFirst("h1").text();
            var description = doc.selectFirst("meta[name=description]").attr("content");

            var urlCheck = new UrlCheck(code, title, h1, description, urlId);
            UrlChecksRepository.save(urlCheck);
            ctx.sessionAttribute("flash", "Проверка успешно выполнена"); // TODO check message
            ctx.sessionAttribute("flashType", "success");

        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Не удалось выполнить проверку"); // TODO check message
            ctx.sessionAttribute("flashType", "danger");
        }

        ctx.redirect(Routes.urlPath(urlId));
    }
}
