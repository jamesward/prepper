package com.embabel.prepper.shell;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.embabel.prepper.agent.Domain;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;

@ShellComponent
record PrepperShell(AgentPlatform agentPlatform) {

    @ShellMethod("prep")
    String prep() {
        var meeting = new Domain.Meeting(
                "Initial discussion about Embabel and Tanzu Division Broadcom",
                """
                        Initial meeting for Rod Johnson from Embabel to establish whether or not there
                        are customer and co-marketing or product development or other business opportunities
                        with Broadcom and Embabel.
                        """,
                List.of(
                        "James Watters Broadcom", "Purnima Padmanabhan Broadcom", "Ryan Morgan Broadcom")
        );
        var briefing = AgentInvocation.builder(agentPlatform)
                .options(ProcessOptions.builder().verbosity(v -> v.showPrompts(true)).build())
                .build(Domain.Briefing.class)
                .invoke(meeting);
        return briefing.briefing();
    }

}
