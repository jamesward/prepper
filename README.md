<img align="left" src="https://github.com/embabel/embabel-agent/blob/main/embabel-agent-api/images/315px-Meister_der_Weltenchronik_001.jpg?raw=true" width="180">

![Build](https://github.com/embabel/java-agent-template/actions/workflows/maven.yml/badge.svg)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Apache Tomcat](https://img.shields.io/badge/apache%20tomcat-%23F8DC75.svg?style=for-the-badge&logo=apache-tomcat&logoColor=black)
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![ChatGPT](https://img.shields.io/badge/chatGPT-74aa9c?style=for-the-badge&logo=openai&logoColor=white)
![JSON](https://img.shields.io/badge/JSON-000?logo=json&logoColor=fff)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)

&nbsp;&nbsp;&nbsp;&nbsp;

&nbsp;&nbsp;&nbsp;&nbsp;

# Meeting Prep Agent

Reimplementation
of [Crew AI](https://www.crewai.com/)'
s [Prep for a Meeting Agent](https://github.com/crewAIInc/crewAI-examples/tree/main/crews/prep-for-a-meeting)
in Embabel.

Shows:

- A [domain](src/main/java/com/embabel/prepper/agent/Domain.java) model, ensuring that LLM calls are type safe and
  offering extensibility

# Configuration

You will need an `OPENAI_API_KEY` environment variable set to your OpenAI API key.

Alternatively you can change the starters in [the Maven pom](pom.xml) to use
another Embabel model provider, such as Anthropic.
> You can also use local LLMs with Ollama.

This project relies on MCP tools. You will need Docker Desktop, with
the [Docker MCP gateway](https://docs.docker.com/ai/mcp-gateway/) running, with Brave
web search, wikipedia, and LinkedIn tools enabled. Your configuration
should look like this:

![Docker Desktop](images/docker_desktop.jpg)

# Running

Run the shell script to start Embabel under Spring Shell:

```bash
./scripts/shell.sh
```

Run the `prep` command.
