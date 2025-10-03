# Embabel Workshop

## Prerequisites

1. Browser
2. Access to your email

## AWS Provisioning

1. Visit the provided workshop URL *in a guest browser*
2. Sign in using the *Email OTP* option
3. Follow the instructions to authenticate to the temporary AWS account
4. Follow the instructions for *Workshop setup*

## Model Access

1. Follow the instructions for *Model Access* except enable these models:
    `Amazon Nova Pro`
    `Amazon Nova Lite`

1. Now *STOP* following the instructions in the workshop and continue here.

## Get the starter code

1. In the hosted IDE's shell:
    git clone https://github.com/jamesward/prepper.git
    cd prepper
2. Verify the Embabel shell starts:
    ./mvnw spring-boot:run
3. Verify the models in the Embabel shell:
    models

## Add the domain model

1. Code

## Add a Contact Service & Repository

1. Code

## Add an Embabel shell command to list contacts

1. Code
2. Restart the Embabel shell
3. Verify the new `contacts` command works (you shouldn't see any contacts yet)

## Create the Agent

1. Code
1. Config
1. Restart the Embabel shell
1. Verify the new `agents` command lists your agent
1. Add the Embabel shell command to run the agent
1. Restart the Embabel shell
1. Verify the new `prep` command runs the agent (likely with bad results)
1. Verify a Contact was created

## Add the Brave MCP Tool

1. Config
1. Restart the Embabel shell
1. Run the `tools` command to verify the Brave MCP tool is installed
1. Run the `prep` command to run the agent again
1. Verify the new `prep` command runs the agent (hopefully with better results)

## Celebrate!
