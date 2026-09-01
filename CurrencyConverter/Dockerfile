# Use a Maven image with JDK 21 and Alpine Linux
# This image contains Java and Maven
FROM mvkvl/maven:jdk-21-alpine

# Set the working directory inside the container
# All following commands will use /app_dir as the default directory
WORKDIR /app_dir

# Earlier was using the api_key directly
ARG API_KEY
# Set an environment variable inside the container.
# Note: ENV variable names should not contain spaces around "=".
#
# For sensitive API keys, it is better NOT to put the actual key
# directly in the Dockerfile. Pass it when running the container instead.
ENV API_KEY=${API_KEY}


# This requires manually building the jar and copying it to the container and then running it
# Copy the generated Spring Boot JAR from the target directorydock
# on your computer into the /app_dir directory inside the container
COPY target/CurrencyConverter-0.0.1-SNAPSHOT.jar .

# Define the main command that runs when the container starts.
#
# Docker will execute:
# java -jar CurrencyConverter-0.0.1-SNAPSHOT.jar
#
# Each command and argument must be a separate item in the array.
ENTRYPOINT ["java", "-jar", "CurrencyConverter-0.0.1-SNAPSHOT.jar"]


