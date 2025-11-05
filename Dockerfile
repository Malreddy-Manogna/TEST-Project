FROM openjdk:17

# Set working directory inside container
WORKDIR /app

# Copy source code and UI folder into container
COPY src/ ./src/
COPY ui/ ./ui/

# Compile all Java files inside src
RUN javac src/*.java

# Expose the port
EXPOSE 8080

# Environment variable for Render dynamic port
ENV PORT 8080

# Run the Main class (classpath includes src/)
CMD ["java", "-cp", "src", "Main"]
