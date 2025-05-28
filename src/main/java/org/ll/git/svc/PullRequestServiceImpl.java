package org.ll.git.svc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service @Slf4j
public class PullRequestServiceImpl {

    @Value("${github.api.url:https://api.github.com}") private String githubApiUrl;
    @Value("${github.api.username}") private String username;
    @Value("${github.api.token}") private String token;

    public void createPullRequest(String repo, String baseBranch, String fromBranch,
                                        String title, String body, String token) {

        String jsonBody = """
        {
            "title": "%s",
            "body": "%s",
            "head": "%s",
            "base": "%s"
        }
        """.formatted(title, body, fromBranch, baseBranch);


        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("%s/repos/%s/%s/pulls".formatted(githubApiUrl, username, repo)))
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer %s".formatted(token))
                .header("X-GitHub-Api-Version", "2022-11-28") // refer: https://docs.github.com/en/rest/about-the-rest-api/api-versions
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    return switch (response.statusCode()) {
                        case 201 -> {
                            log.info("Pull Request success");
                            yield response.body();
                        }
                        case 401 -> {
                            log.info("401");
                            yield null;
                        }
                        case 404 -> {
                            log.info("repository not found");
                            yield null;
                        }
                        default -> {
                            log.info("failed - {}, {}", response.statusCode(), response.body());
                            yield null;
                        }
                    };
                })
                .join();
    }
}