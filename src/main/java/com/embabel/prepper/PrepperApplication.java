package com.embabel.prepper;

import com.embabel.agent.core.CoreToolGroups;
import com.embabel.agent.core.ToolGroup;
import com.embabel.agent.core.ToolGroupDescription;
import com.embabel.agent.core.ToolGroupPermission;
import com.embabel.agent.tools.mcp.McpToolGroup;
import com.embabel.prepper.agent.PrepperConfig;
import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.embabel.agent.config.models.bedrock.BedrockModelLoader;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;
import java.util.Set;

@SpringBootApplication
@EnableConfigurationProperties(PrepperConfig.class)
class PrepperApplication {

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

@Configuration
class McpToolGroupsConfiguration {

	private final List<McpSyncClient> mcpSyncClients;

	public McpToolGroupsConfiguration(List<McpSyncClient> mcpSyncClients) {
		this.mcpSyncClients = mcpSyncClients;
	}

	@Bean
	public ToolGroup mcpWebToolsGroup() {
		return new McpToolGroup(ToolGroupDescription.create("Tools for web search and scraping", CoreToolGroups.WEB),
				"mcp", "web", Set.of(ToolGroupPermission.INTERNET_ACCESS), mcpSyncClients, callback -> true);
	}

}
