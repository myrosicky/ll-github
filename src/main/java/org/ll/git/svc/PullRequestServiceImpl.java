package org.ll.git.svc;

import io.netty.handler.ssl.ApplicationProtocolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;


@Service
@Slf4j
public class PullRequestServiceImpl {

    @Value("${github.api.url:https://api.github.com}") private String githubApiUrl;

    public Mono<String> createPullRequest(String repoOwnerName, String repoName, String token, String baseBranch, String fromBranch,
                                    String title, String body) {

        String jsonBody = """
        {
            "title": "%s",
            "body": "%s",
            "head": "%s",
            "base": "%s"
        }
        """.formatted(title, body, fromBranch, baseBranch);
        reactor.netty.http.client.HttpClient httpClient = HttpClient.create()
                    .protocol(HttpProtocol.H2)
                    .responseTimeout(Duration.ofSeconds(5));

        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(githubApiUrl)
                .build();

        return webClient.post()
                .uri("%s/repos/%s/%s/pulls".formatted(githubApiUrl, repoOwnerName, repoName))
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "token %s".formatted(token))
                .header("X-GitHub-Api-Version", "2022-11-28") // refer: https://docs.github.com/en/rest/about-the-rest-api/api-versions
                .bodyValue(jsonBody)
                .retrieve()
                .onRawStatus(s -> s == 401, resp -> Mono.error(new Exception("401")))
                .onRawStatus(s -> s == 404, resp -> {
                    log.info("repository not found");
                    return Mono.error(new Exception("repository not found"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, resp -> {
                    log.warn("failed with: {}", resp.statusCode().value());
                    return Mono.error(new Exception("failed"));
                })
                .bodyToMono(String.class)
                .doOnError(t -> log.error("createPullRequest failed", t))
                .doOnNext(respBody -> {
                    log.info("Pull Request success");
                })
                ;


//        HttpClient client = HttpClient.newBuilder()
//                .version(HttpClient.Version.HTTP_2)
//                .build();
//
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("%s/repos/%s/%s/pulls".formatted(githubApiUrl, repoOwnerName, repoName)))
//                .header("Accept", "application/vnd.github+json")
//                .header("Authorization", "token %s".formatted(token))
//                .header("X-GitHub-Api-Version", "2022-11-28") // refer: https://docs.github.com/en/rest/about-the-rest-api/api-versions
//                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
//                .build();
//
//        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                .thenApply(response -> {
//                    return switch (response.statusCode()) {
//                        case 201 -> {
//                            log.info("Pull Request success");
//                            yield response.body();
//                        }
//                        case 401 -> {
//                            log.info("401");
//                            yield null;
//                        }
//                        case 404 -> {
//                            log.info("repository not found");
//                            yield null;
//                        }
//                        default -> {
//                            log.info("failed - {}, {}", response.statusCode(), response.body());
//                            yield null;
//                        }
//                    };
//                })
//                .join();
    }
}