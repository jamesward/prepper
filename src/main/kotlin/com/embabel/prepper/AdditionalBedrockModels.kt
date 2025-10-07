package com.embabel.prepper

import com.embabel.agent.config.AgentPlatformConfiguration
import com.embabel.agent.config.models.bedrock.BedrockModels
import com.embabel.agent.config.models.bedrock.BedrockOptionsConverter
import com.embabel.common.ai.model.Llm
import com.embabel.common.ai.model.PerTokenPricingModel
import io.micrometer.observation.ObservationRegistry
import org.springframework.ai.bedrock.converse.BedrockProxyChatModel
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.observation.ChatModelObservationConvention
import org.springframework.ai.model.bedrock.autoconfigure.BedrockAwsConnectionProperties
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate
import org.springframework.ai.model.tool.ToolCallingChatOptions
import org.springframework.ai.model.tool.ToolCallingManager
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.regions.providers.AwsRegionProvider
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import java.time.LocalDate

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(AgentPlatformConfiguration::class)
class AdditionalBedrockModels(
    private val credentialsProvider: AwsCredentialsProvider,
    private val regionProvider: AwsRegionProvider,
    private val connectionProperties: BedrockAwsConnectionProperties,
    private val observationRegistry: ObjectProvider<ObservationRegistry>,
    private val observationConvention: ObjectProvider<ChatModelObservationConvention>,
    private val bedrockRuntimeClientProvider: ObjectProvider<BedrockRuntimeClient>,
    private val bedrockRuntimeAsyncClientProvider: ObjectProvider<BedrockRuntimeAsyncClient>
) {

    fun chatModelOf(model: String): ChatModel {
        val bedrockRuntimeClient = bedrockRuntimeClientProvider.getIfAvailable {
            BedrockRuntimeClient.builder()
                .region(regionProvider.region)
                .httpClientBuilder(null)
                .credentialsProvider(credentialsProvider)
                .overrideConfiguration { c -> c.apiCallTimeout(connectionProperties.timeout) }
                .build()
        }

        val bedrockRuntimeAsyncClient = bedrockRuntimeAsyncClientProvider.getIfAvailable {
            BedrockRuntimeAsyncClient.builder()
                .region(regionProvider.region)
                .httpClientBuilder(
                    NettyNioAsyncHttpClient.builder()
                        .tcpKeepAlive(true)
                        .connectionAcquisitionTimeout(java.time.Duration.ofSeconds(30))
                        .maxConcurrency(200)
                )
                .credentialsProvider(this.credentialsProvider)
                .overrideConfiguration { c -> c.apiCallTimeout(connectionProperties.timeout) }
                .build()
        }

        return BedrockProxyChatModel(
            bedrockRuntimeClient,
            bedrockRuntimeAsyncClient,
            ToolCallingChatOptions.builder().model(model).build(),
            observationRegistry.ifUnique ?: ObservationRegistry.NOOP,
            ToolCallingManager.builder().build(),
            DefaultToolExecutionEligibilityPredicate()
        ).apply {
            observationConvention.ifAvailable?.let { setObservationConvention(it) }
        }
    }

    @Bean("bedrockModel-us.amazon.nova-pro-v1:0")
    fun usNovaPro(): Llm {
        val model = "us.amazon.nova-pro-v1:0"
        return Llm(
            model,
            BedrockModels.PROVIDER,
            chatModelOf(model),
            BedrockOptionsConverter,
            LocalDate.parse("2025-03-01"), // todo
            arrayListOf(),
            PerTokenPricingModel(1.0, 1.0) // todo
        )
    }

    @Bean("bedrockModel-us.amazon.nova-lite-v1:0")
    fun usNovaLite(): Llm {
        val model = "us.amazon.nova-lite-v1:0"
        return Llm(
            model,
            BedrockModels.PROVIDER,
            chatModelOf(model),
            BedrockOptionsConverter,
            LocalDate.parse("2025-03-01"), // todo
            arrayListOf(),
            PerTokenPricingModel(1.0, 1.0) // todo
        )
    }
}
