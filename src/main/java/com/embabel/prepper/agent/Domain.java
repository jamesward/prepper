package com.embabel.prepper.agent;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public abstract class Domain {

    public record Meeting(
            String context,
            String objective,
            @JsonPropertyDescription("A list of participant email addresses or however else we identify them")
            List<String> participants
    ) {
    }

    /**
     * Fleshed out participant
     *
     * @param email
     * @param writeup
     */
    public record Participant(
            String email,
            String writeup) {
    }

    public record Participants(
            List<Participant> participants
    ) {
    }

    public record IndustryAnalysis(
            String analysis
    ) {
    }

    public record MeetingStrategy(
            String strategy
    ) {
    }

    public record Briefing(
            Meeting meeting,
            Participants participants,
            IndustryAnalysis industryAnalysis,
            MeetingStrategy meetingStrategy,
            String summary
    ) {
    }

}
