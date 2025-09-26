package com.embabel.prepper.agent.agent;

import com.embabel.agent.prompt.persona.Actor;
import com.embabel.agent.prompt.persona.RoleGoalBackstory;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.prepper.agent.PrepperConfig;
import org.junit.jupiter.api.Test;

class PrepperAgentTest {

    final LlmOptions llm = LlmOptions.withAutoLlm();

    final PrepperConfig config = new PrepperConfig(
            new Actor<>(RoleGoalBackstory.withRole("researcher").andGoal("perform reseearch").andBackstory("i am clever"), llm),
            new Actor<>(RoleGoalBackstory.withRole("industry analyzer").andGoal("goal").andBackstory("i am thorough"), llm),
            new Actor<>(RoleGoalBackstory.withRole("meeting strategist").andGoal("goal").andBackstory("i am strategic"), llm),
            new Actor<>(RoleGoalBackstory.withRole("briefing writer").andGoal("goal").andBackstory("i am eloquent"), llm),
            2
    );

    @Test
    void testPrepper() {
//        var context = FakeOperationContext.create();
//        var promptRunner = (FakePromptRunner) context.promptRunner();
//        context.expectResponse(new Domain.Participants(List.of()));
//
//        var agent = new PrepperAgent(config);
//        var meeting = new Domain.Meeting(
//                "context", "to rule the world", List.of("Tony Blair")
//        );
//        agent.researchParticipants(meeting, context);
//
//        String prompt = promptRunner.getLlmInvocations().getFirst().getPrompt();
//        assertTrue(prompt.contains(meeting.objective()), "Expected prompt to contain objective:\n" + prompt);
    }


}