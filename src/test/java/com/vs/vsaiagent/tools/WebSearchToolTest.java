package com.vs.vsaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class WebSearchToolTest {


    @Value("${search-api.api-key}")
    private String searchApikey;

    @Test
    void searchWeb() {

        WebSearchTool webSearchTool = new WebSearchTool(searchApikey);
        String result = webSearchTool.searchWeb("怎么样才能找到好的工作");
        Assertions.assertNotNull(result);

    }
}