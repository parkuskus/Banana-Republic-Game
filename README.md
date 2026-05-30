# Banana Republic - Tugas Besar 2 IF2010 OOP 2526

[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/FE4lxIC7)

## Prerequisites

- **Java Development Kit (JDK) 21** (LTS)
- **Apache Maven 3.9+**

> No manual JavaFX SDK download is required — JavaFX is managed via Maven dependencies.

## Project Structure

The project follows the class diagram specifications in `class-diagram/`:

| Module | Package(s) | Responsibility |
|--------|-----------|----------------|
| Module 1 | `board`, `dice`, `plugin` | Board/map generation, dice, plugins |
| Module 2 | `player`, `building`, `resource` | Players, buildings, bank, resources |
| Module 3 | `card`, `card.special`, `robber`, `timer` | Cards, robber, turn timer |
| Module 4 | `core`, `trade` | Game engine, trade system |
| Module 5 | `ui`, `plugin`, `save` | JavaFX UI, plugin loader, save/load |

## Quick Start

### Compile
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```

Tests run in headless mode (suitable for CI/autograder) using TestFX + Monocle.

### Run Application (Development)
```bash
mvn javafx:run
```

### Build & Package
```bash
mvn clean package
```

This creates:
- `target/if2010-oop2526-tubes2-1.0-SNAPSHOT.jar` — standard JAR (manifest set)
- `target/if2010-oop2526-tubes2-1.0-SNAPSHOT-shaded.jar` — uber JAR with all dependencies
- `target/lib/` — individual dependency JARs

### Run Packaged JAR
**Recommended (using module path, no warnings):**
```bash
java --module-path target/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics \
  -cp "target/if2010-oop2526-tubes2-1.0-SNAPSHOT.jar:target/lib/*" banana.republic.Main
```

**Simple (shaded JAR — may show a non-fatal module warning):**
```bash
java -jar target/if2010-oop2526-tubes2-1.0-SNAPSHOT-shaded.jar
```

### Makefile Targets
```bash
make build    # mvn clean package
make test     # mvn test
make run      # mvn javafx:run
make run-jar  # build + run the shaded JAR
make verify   # mvn clean verify
make clean    # mvn clean
```

## IntelliJ IDEA Setup

1. Open the project folder in IntelliJ IDEA.
2. IntelliJ should auto-detect the Maven project.
3. Ensure Project SDK is set to **JDK 21**:
   - `File → Project Structure → Project SDK → 21`
4. Reload Maven project if needed:
   - Click the Maven reload button in the top-right or right-click `pom.xml → Maven → Reload Project`

## Headless Testing Configuration

Headless GUI testing is configured automatically in `pom.xml` via the `maven-surefire-plugin`:

```xml
<testfx.robot>glass</testfx.robot>
<testfx.headless>true</testfx.headless>
<prism.order>sw</prism.order>
<glass.platform>Monocle</glass.platform>
<monocle.platform>Headless</monocle.platform>
```

No additional flags are needed when running `mvn test`.

## Common Issues

### "JavaFX runtime components are missing"
If you try to run the plain `.class` files without the module path, you may see this error. Use one of the provided methods:
- `mvn javafx:run`
- `java -jar target/if2010-oop2526-tubes2-1.0-SNAPSHOT.jar`

### Rendering Issues on macOS (Development only)
If developing on macOS with Apple Silicon, the JavaFX Maven dependencies will resolve the correct native libraries automatically. No manual SDK download is needed.

If you still encounter issues, you can force software rendering:
```bash
mvn javafx:run -Djavafx.prism.order=sw
```

## Notes

- The project targets **Java 21** (`<release>21</release>`).
- JavaFX version is **21.0.2** (matches the Java version major release).
- All UI updates from non-UI threads must use `Platform.runLater()`.
