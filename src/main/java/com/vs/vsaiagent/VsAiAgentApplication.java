package com.vs.vsaiagent;

import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
public class VsAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(VsAiAgentApplication.class, args);
    }

}
