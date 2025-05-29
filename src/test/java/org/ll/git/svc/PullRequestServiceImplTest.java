package org.ll.git.svc;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = PullRequestServiceImpl.class)
@Slf4j
class PullRequestServiceImplTest {

    @Autowired private PullRequestServiceImpl svc;

    @Test
    void a(){
        svc.createPullRequest("https://github.com/myrosicky/ll-github.git", "master", "pos/a",
                "test pr title", "test pr message haha");
    }
}