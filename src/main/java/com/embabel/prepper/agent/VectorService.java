package com.embabel.prepper.agent;

import com.embabel.agent.api.common.OperationContext;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class VectorService {

	@Bean
	VectorStore vectorStore(OperationContext embabel) {
		return SimpleVectorStore.builder(embabel.ai().withDefaultEmbeddingModel()).build();
	}

}
