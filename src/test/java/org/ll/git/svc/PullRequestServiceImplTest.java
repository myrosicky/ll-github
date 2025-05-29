package org.ll.git.svc;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PullRequestServiceImpl.class)
@Slf4j
class PullRequestServiceImplTest {

    @Autowired private PullRequestServiceImpl svc;
    @Value("${github.api.token}") private String token;

    @Test
    void createPullRequest(){
        StepVerifier.create(svc.createPullRequest("myrosicky", "ll-github", token,
                "master", "poc/a",
                "test pr title", "test pr message haha"))
                .expectComplete()
        ;
    }
}