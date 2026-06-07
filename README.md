# Banana Republic - Tugas Besar 2 IF2010 OOP 2526

Banana Republic adalah permainan strategi berbasis papan yang diimplementasikan sebagai aplikasi desktop menggunakan Java dan JavaFX. Permainan ini mengadaptasi mekanisme klasik settlers dengan setting eksperimental di sebuah republik pisang, di mana para pemain berkompetisi membangun Pos Pantau dan Laboratorium untuk mengumpulkan poin prestasi. Setiap pemain mengumpulkan lima jenis sumber daya (Kayu, Batu Bata, Gandum, Bijih, dan Pisang) yang diproduksi oleh petak-petak heksagonal sesuai hasil lemparan dua dadu. 

Aplikasi ini mendukung permainan oleh tiga hingga empat pemain, baik manusia maupun bot, dengan sistem giliran yang dilengkapi timer 90 detik per fase. Fitur permainan mencakup: perdagangan sumber daya domestik dan maritim, pembangunan jalan, penggunaan kartu temuan, sistem save/load berbasis JSON, dan arsitektur plugin yang memungkinkan penggantian generator peta serta penambahan strategi bot secara dinamis.

## Tim Pengembang (Kelompok NUL - nullptr)

- 13524031 / Vincent Rionarlie
- 13524033 / Ray Owen Martin
- 13524037 / Nicholas Wise Saragih Sumbayak
- 13524061 / Muhammad Aufar Rizqi Kusuma
- 13524065 / Kurt Mikhael Purba

## Dependencies & Prerequisites

- **Java Development Kit (JDK) 21** (LTS)
- **Apache Maven 3.9+**
- **JavaFX 21.0.2** (Managed via Maven: controls, fxml, graphics, base)
- **Google Gson 2.11.0** (JSON save/load, managed via Maven)
- **JLayer 1.0.1** (Pure Java MP3 playback, managed via Maven)
- **JUnit 5, AssertJ, TestFX** (for headless GUI testing, managed via Maven)

> No manual SDK or library downloads are required — everything is managed via Maven dependencies.

## How to Run

### Compile
```bash
mvn clean compile
```

### Run Tests
```bash
mvn test
```
*(Tests run in headless mode suitable for CI/autograder using TestFX + Monocle)*

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
