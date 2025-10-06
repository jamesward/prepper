/*
 * Copyright 2024-2025 Embabel Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.embabel.prepper;

import com.embabel.agent.config.annotation.EnableAgents;
import com.embabel.agent.config.annotation.LoggingThemes;
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
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Set;


@SpringBootApplication
@EnableConfigurationProperties(PrepperConfig.class)
@EnableJpaRepositories(basePackages = "com.embabel.prepper")
@EnableJpaAuditing
@EnableAgents(
        loggingTheme = LoggingThemes.SEVERANCE
//        mcpServers = {McpServers.DOCKER}
//        mcpServers = {McpServers.DOCKER_DESKTOP}
)
class PrepperApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrepperApplication.class, args);
    }

    /*
    @Bean
    ToolGroup linkedin(List<McpSyncClient> clients) {
       return new McpToolGroup(
               ToolGroupDescription.create("LinkedIn Tool Group", "linkedin"),
               "remote",
               "linkedin",
               Set.of(ToolGroupPermission.INTERNET_ACCESS),
               clients,
               tool -> tool.getToolDefinition().name().contains("linkedin")
       );
    }
     */
}
