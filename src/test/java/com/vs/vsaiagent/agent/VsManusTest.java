package com.vs.vsaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class VsManusTest {

    @Resource
    private VsManus vsManus;

    @Test
    public void run() {
        String userPrompt = """
                我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点，
                并以 PDF 格式输出""";
        String answer = vsManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }

}