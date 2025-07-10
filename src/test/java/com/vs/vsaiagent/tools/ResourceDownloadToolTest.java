package com.vs.vsaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ResourceDownloadToolTest {

    @Test
    public void testDownloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String url = "url";
        String fileName = "xxx.png";
        String result = tool.downloadResource(url, fileName);
        assertNotNull(result);
    }
}