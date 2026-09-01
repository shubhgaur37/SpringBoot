# Use a Maven image with JDK 21 and Alpine Linux
FROM mvkvl/maven:jdk-21-alpine

# Set the working directory inside the container
WORKDIR /app_dir

# SECURITY UPDATE: Removed ARG API_KEY and ENV API_KEY declaration layers.
# Storing secrets via ARG/ENV during build time permanently bakes them into
# the image layers, exposing them to anyone with access via 'docker history'.
# Instead, Spring Boot automatically detects the API_KEY environment
# variable injected safely at runtime using 'docker run -e API_KEY="..."'.

# Copy the generated Spring Boot JAR into the container
COPY target/CurrencyConverter-0.0.1-SNAPSHOT.jar .

# Define the main command that runs when the container starts
ENTRYPOINT ["java", "-jar", "CurrencyConverter-0.0.1-SNAPSHOT.jar"]
