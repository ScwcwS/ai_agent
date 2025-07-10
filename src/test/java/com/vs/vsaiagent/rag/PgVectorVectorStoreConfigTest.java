package com.vs.vsaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorVectorStore;
    @Test
    void pgVectorVectorStore() {
//        List<Document> documents = List.of(
//                new Document("宋晨炜，男，175，爱好健身", Map.of("meta1", "meta1")),
//                new Document("宋晨炜健身水平：卧推100kg，引体30个"),
//                new Document("宋晨炜这小伙子比较帅气", Map.of("meta2", "meta2")));
        // 添加文档
//        pgVectorVectorStore.add(documents);
        // 相似度查询
        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("教陈一凡怎么脱单").topK(3).build());
        Assertions.assertNotNull(results);
    }
}