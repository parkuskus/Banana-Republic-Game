.PHONY: build run test clean all

# JavaFX version
JFX_VERSION=21.0.2
# https://gluonhq.com/products/javafx/
JAVAFX_SDK_URL=https://download2.gluonhq.com/openjfx/21.0.11/openjfx-21.0.11_linux-x64_bin-sdk.zip
JAR_NAME=template-if2010-2526-tubes-2-1.0-SNAPSHOT.jar

# Download JavaFX SDK if needed
javafx-sdk:
    curl -L $(JAVAFX_SDK_URL) -o javafx-sdk.zip
    unzip -o javafx-sdk.zip
    rm javafx-sdk.zip

# Build with JavaFX
build: javafx-sdk
    export PATH_TO_FX="$(PWD)/javafx-sdk-$(JFX_VERSION)/lib" && \
    mvn clean package

# Run tests with JavaFX
test: javafx-sdk
    export PATH_TO_FX="$(PWD)/javafx-sdk-$(JFX_VERSION)/lib" && \
    mvn test -Dtestfx.robot=glass -Dglass.platform=Monocle -Dmonocle.platform=Headless -Dprism.order=sw

# Run the application
run: javafx-sdk
    export PATH_TO_FX="$(PWD)/javafx-sdk-$(JFX_VERSION)/lib" && \
    java --module-path "$$PATH_TO_FX" --add-modules javafx.controls,javafx.fxml -jar target/$(JAR_NAME)

# Clean the build artifacts
clean:
    mvn clean
    rm -rf javafx-sdk-$(JFX_VERSION)

# Build and run
all: build run