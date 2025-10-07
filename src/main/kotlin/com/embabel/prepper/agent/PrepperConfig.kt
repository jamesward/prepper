package com.embabel.prepper.agent

import com.embabel.agent.prompt.persona.Actor
import com.embabel.agent.prompt.persona.RoleGoalBackstory
import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.validation.annotation.Validated

@ConfigurationProperties(prefix = "prepper")
@Validated
data class PrepperConfig(
    @field:NotNull val researcher: Actor<RoleGoalBackstory>,
    @field:NotNull val industryAnalyzer: Actor<RoleGoalBackstory>,
    @field:NotNull val meetingStrategist: Actor<RoleGoalBackstory>,
    @field:NotNull val briefingWriter: Actor<RoleGoalBackstory>,
    @DefaultValue("8") val maxConcurrency: Int = 8
)
