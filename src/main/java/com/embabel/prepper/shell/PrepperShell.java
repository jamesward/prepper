package com.embabel.prepper.shell;

import com.embabel.agent.core.AgentPlatform;
import org.springframework.shell.standard.ShellComponent;

@ShellComponent
record PrepperShell(AgentPlatform agentPlatform) {

}
