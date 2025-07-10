package com.vs.vsaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "text.pdf";
        String content = "宋晨炜加油";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }


}