package com.vs.vsaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void scrapeWebPage() {

        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String result = webScrapingTool.scrapeWebPage("https://www.baidu.com");
        Assertions.assertNotNull(result);
    }
}