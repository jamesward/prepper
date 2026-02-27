package com.embabel.prepper;

import com.embabel.agent.config.models.bedrock.BedrockModelLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;

@SpringBootApplication
public class PrepperApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrepperApplication.class, args);
	}

}

@Configuration
class AdditionalBedrockModels {

	@Bean
	BedrockModelLoader bedrockModels() {
		return new BedrockModelLoader(new DefaultResourceLoader(), "classpath:models/additional-bedrock.yaml");
	}

}
