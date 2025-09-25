package com.embabel.prepper.shell;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.prepper.agent.Domain;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;

@ShellComponent
record PrepperShell(AgentPlatform agentPlatform) {

    @ShellMethod("prep")
    String prep() {
        var meeting = new Domain.Meeting(
                "Project Kickoff for New Website",
                "Define project scope, timelines, and assign roles",
                List.of(
                        "bob.smith@example.com")
        );
        var briefing = AgentInvocation
                .create(agentPlatform, Domain.Briefing.class)
                .invoke(meeting);
        return briefing.summary();
    }

}
