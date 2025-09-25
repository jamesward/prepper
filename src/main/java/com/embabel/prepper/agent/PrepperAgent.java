package com.embabel.prepper.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Agent(description = "A meeting prepper agent that helps users prepare for meetings ")
public record PrepperAgent(
        PrepperConfig config
) {

    private final static Logger logger = LoggerFactory.getLogger(PrepperAgent.class);

    public PrepperAgent {
        logger.info("Initialized PrepperAgent with config: {}", config);
    }

    @Action
    public Domain.Participants researchParticipants(Domain.Meeting meeting) {
        return new Domain.Participants(meeting.participants().stream()
                .map(email -> new Domain.Participant(email, "Writeup for " + email))
                .toList());
    }

    @Action
    public Domain.IndustryAnalysis analyzeIndustry(Domain.Meeting meeting, Domain.Participants participants) {
        return new Domain.IndustryAnalysis("Industry analysis for meeting with context: " + meeting.context());
    }

    @Action
    public Domain.MeetingStrategy formulateMeetingStrategy(Domain.Meeting meeting, Domain.Participants participants, Domain.IndustryAnalysis industryAnalysis) {
        return new Domain.MeetingStrategy("Meeting strategy for meeting with objective: " + meeting.objective());
    }

    @Action
    @AchievesGoal(description = "Produce a briefing for the meeting",
            export = @Export(remote = true, startingInputTypes = {Domain.Meeting.class}))
    public Domain.Briefing produceBriefing(
            Domain.Meeting meeting, Domain.Participants participants, Domain.IndustryAnalysis industryAnalysis,
            Domain.MeetingStrategy meetingStrategy) {
        return new Domain.Briefing(
                meeting, participants, industryAnalysis, meetingStrategy,
                "Summary for meeting with objective: " + meeting.objective()
        );
    }
}
