package com.vs.vsaiagent.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class MultiQueryExpanderDemoTest {


    @Resource
    private MultiQueryExpanderDemo multiQueryExpanderDemo;
    @Test
    void expand() {
        List<Query> queries = multiQueryExpanderDemo.expand("谁是陈一凡,请回答我。。。。。哈哈哈哈哈哈哈哈哈哈");
        Assertions.assertNotNull(queries);
    }
}