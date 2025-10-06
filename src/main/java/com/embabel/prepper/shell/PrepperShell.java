package com.embabel.prepper.shell;

import com.embabel.agent.core.AgentPlatform;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
record PrepperShell(AgentPlatform agentPlatform) {

}
