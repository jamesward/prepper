package com.embabel.prepper.agent;

import com.embabel.agent.prompt.persona.Actor;
import com.embabel.agent.prompt.persona.RoleGoalBackstory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;


@ConfigurationProperties(prefix = "prepper")
@Validated
public record PrepperConfig(
        @NotNull Actor<RoleGoalBackstory> researcher,
        @NotNull Actor<RoleGoalBackstory> industryAnalyzer,
        @NotNull Actor<RoleGoalBackstory> meetingStrategist,
        @NotNull Actor<RoleGoalBackstory> briefingWriter
) {
}
