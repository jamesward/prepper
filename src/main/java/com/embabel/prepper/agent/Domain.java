package com.embabel.prepper.agent;

import com.embabel.common.ai.prompt.PromptContributor;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;

import java.util.List;

public abstract class Domain {

    public record Meeting(
            String context,
            String objective,
            @JsonPropertyDescription("A list of participant email addresses or however else we identify them")
            List<String> participants
    ) {

        @NonNull
        public String purpose() {
            return "Meeting:\nContext: %s\nObjective: %s\n".formatted(context, objective);
        }
    }

    /**
     * Fleshed out participant
     *
     * @param email
     * @param writeup
     */
    public record ResearchedParticipant(
            String email,
            String writeup) implements PromptContributor {

        @NotNull
        @Override
        public String contribution() {
            return "- %s: %s".formatted(email, writeup);
        }
    }

    public record Participants(
            List<ResearchedParticipant> participants
    ) implements PromptContributor {

        @NotNull
        @Override
        public String contribution() {
            return participants.stream()
                    .map(ResearchedParticipant::contribution)
                    .collect(java.util.stream.Collectors.joining("\n"));
        }
    }

    public record IndustryAnalysis(
            String analysis
    ) {
    }

    public record MeetingStrategy(
            @JsonPropertyDescription("Complete report with a list of key talking points and strategic questions" +
                    " to ask, to help achieve the meeting's objective")
            String strategy
    ) {
    }

    public record Briefing(
            Meeting meeting,
            Participants participants,
            IndustryAnalysis industryAnalysis,
            MeetingStrategy meetingStrategy,
            String briefing
    ) {
    }

}
