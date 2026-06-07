.PHONY: build run test clean all verify plugin

# Project settings (must match pom.xml)
JAR_NAME=if2010-oop2526-tubes2-1.0-SNAPSHOT.jar
SHADED_JAR_NAME=if2010-oop2526-tubes2-1.0-SNAPSHOT-shaded.jar

# Build the project with Maven
build:
	mvn clean package

# Run tests (headless compatible for CI/autograder)
test:
	mvn test

# Run the application using Maven JavaFX plugin (recommended for dev)
run:
	mvn javafx:run

# Run the shaded uber JAR (after build)
run-jar: build
	java -jar target/$(SHADED_JAR_NAME)

# Run with module path (no JavaFX warnings)
run-module: build
	java --module-path target/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics \
	  -cp "target/$(JAR_NAME):target/lib/*" banana.republic.Main

# Full verification: compile + test + package
verify:
	mvn clean verify

# Clean build artifacts
clean:
	mvn clean

# Compile all plugin JARs (requires mvn compile first)
plugin:
	@echo "=== Compiling all plugins ==="
	mvn compile
	@cd plugins-src && ./build.sh

# Build and run
all: build run
