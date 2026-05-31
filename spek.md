# Spesifikasi Tugas Besar 2: Banana Republic

## IF2010 Pemrograman Berorientasi Objek

Version history:

## Daftar Gambar

- Daftar Isi
- Daftar Gambar
- Deskripsi Persoalan
- Panduan Pengerjaan
  - Spesifikasi Sistem
  - Ketentuan Teknis
- Referensi Antarmuka
  - User Flow
  - Referensi Layar
    - Layar Main Menu
    - Layar Lobby (Game Setup)
    - Layar Game
    - Trade Dialog
    - Experiment Cards Dialog
    - Settings & Plugins Dialog
    - Victory Declarations Dialog
    - Steal Card Dialog
    - Discard Card Dialog
    - Layar Transisi Turn
    - Layar Game Result
- Mekanisme
  - Inisiasi Permainan
    - Persiapan Awal
    - Fase Set-Up: Inisiasi Papan
      - Menentukan Urutan Giliran
      - Putaran Pertama (Clockwise)
      - Putaran Kedua (Counter-Clockwise)
      - Sumber Daya Awal
    - Mode Peta
  - Peta Kepulauan Banana Republic
    - Ilustrasi Peta
    - Jenis-Jenis Petak Terrain
    - Token Angka
    - Struktur Papan
    - Pelabuhan (Harbor)
  - Alur Giliran & Mekanisme Dadu
    - Urutan Giliran
    - Resource Gathering
    - Trade/Build
    - End Turn
  - Sumber Daya Kepulauan
    - Jenis Sumber Daya
    - Cara Mendapatkan Sumber Daya
    - Bank Sumber Daya (Sistem Finite)
  - Nimon Ungu
    - Posisi Awal
    - Efek Nimon Ungu
    - Mencuri
    - Memindahkan Nimon Ungu dengan Kartu Penjaga
  - Biaya Pembangunan
    - Tabel Biaya Pembangunan
    - Aturan Pembangunan
      - Pipa Transportasi
      - Pos Pantau
      - Laboratorium
      - Kartu Temuan Dr. Neroifa
  - Kartu Temuan Dr. Neroifa & Kartu Spesial
    - Aturan Umum Kartu Temuan
    - Jenis Kartu Temuan
    - Kartu Spesial
  - Jual Beli & Pelabuhan
    - Barter Antar Nimon (Domestic Trade)
    - Terminal Dagang Gro / Pelabuhan (Maritime Trade)
  - Kondisi Menang & Poin Prestasi
  - Multithreading
    - Countdown Timer Giliran
  - Sistem Plugin
    - Kartu Temuan Baru (Plugin JAR)
  - Save & Load Permainan
    - Kebutuhan Save/Load
    - Rekomendasi Format & Struktur
- Fitur Bonus
  - Generator Peta Baru
  - Pemain Bot
  - BGM & SFX Engine (Audio Player)
  - Animasi Background
- Deliverables
- Asistensi & QnA
  - Penting!
- Daftar Referensi
  - Extras
- Gambar 1. Ilustrasi kejadian di markas bawah tanah (Source: Gemini AI) Daftar Gambar
- Gambar 2. User flow Banana Republic
- Gambar 3. Referensi Layar Main Menu
- Gambar 4. Referensi Layar Lobby
- Gambar 5. Referensi Layar Game
- Gambar 6. Referensi Layar Trade Dialog
- Gambar 7. Referensi Layar Experiment Cards Dialog
- Gambar 8. Referensi Layar Settings & Plugins Dialog
- Gambar 9. Referensi Layar Victory Declarations Dialog
- Gambar 10. Referensi Layar Steal Card Dialog
- Gambar 11. Referensi Layar Discard Dialog
- Gambar 12. Referensi Layar Transisi Turn
- Gambar 13. Referensi Layar Game Result
- Gambar 14. Ilustrasi Peta Banana Republic.

## Deskripsi Persoalan

**_Gambar 1._** _Ilustrasi kejadian di markas bawah tanah (Source: Gemini AI)_^
Suatu sore di markas bawah tanah, terdengar keributan luar biasa dari laboratorium Dr. Neroifa.
Gro dan Luiy yang sedang bersantai segera berlari turun untuk memeriksa keadaan. Di sana,
mereka mendapati Kebin, Stewart, Pop, dan Toto sedang saling tarik-menarik kacamata pelindung
(goggles) dan melempar alat perkakas. Di tengah kekacauan tersebut, bersinar sebuah proyeksi
hologram canggih berbentuk pulau dengan petak-petak heksagonal.
Dr. Neroifa, yang sedang berlindung di balik meja kerjanya sambil memegang remot kontrol,
menjelaskan bahwa ia baru saja memindai sebuah kepulauan tak berpenghuni. Pulau heksagonal
tersebut sangat kaya akan sumber daya alam: Kayu, Batu Bata, Besi, Gandum, dan yang paling
memicu keributan di antara para Nimons... ladang Pisang raksasa yang tak berujung! Keempat
Nimons tersebut rupanya saling mengklaim dan memperebutkan hak untuk mengekspansi pulau
itu demi mendirikan "Banana Republic" impian mereka.
Menyadari bahwa membiarkan para Nimons pergi ke pulau asli dengan kapal selam hanya akan
memicu bencana internasional, Gro mendapat ide cemerlang. Sebelum terjadi perang saudara
sesama Nimons, Gro memutuskan untuk mengalihkan ambisi ekspedisi tersebut ke dalam sebuah
simulasi permainan digital. Dalam permainan strategi ini, para Nimons harus bersaing
menggunakan otak, bukan otot. Mereka dituntut untuk mengumpulkan sumber daya, melakukan
barter dengan cerdik, membangun jalan, mendirikan pos pantau, hingga memperluas peradaban
mereka di atas pulau heksagonal tersebut secara bergiliran.
Sayangnya, Dr. Neroifa terlalu sibuk memperbaiki alat-alat lab yang hancur, dan Gro tidak memiliki
keahlian memprogram Graphical User Interface (GUI). Oleh karena itu, Gro menunjuk Anda

sebagai Lead Developer! Bantulah Gro dan Dr. Neroifa membangun gim "Nimons: Banana
Republic" menggunakan bahasa Java dengan antarmuka JavaFX. Terapkan pemahaman OOP
terbaik Anda agar Kebin, Stewart, Pop, dan Toto dapat menyalurkan ambisi peradaban pisang
mereka secara virtual tanpa harus menghancurkan markas!

## Panduan Pengerjaan

Tugas besar ini dilakukan secara berkelompok menggunakan kelompok yang sebelumnya dibuat.
Tidak perlu membuat kelompok lagi karena sudah di-assign menggunakan kelompok sebelumnya
pada GH classroom. Untuk mengerjakan assignment silahkan gunakan link berikut:
Github Classroom (deadline ada di sini yah)
Pada repository tersebut, sudah disediakan template yang bisa di **RUN** langsung dengan
panduannya untuk menggunakan JavaFX sebagai GUI. Semoga ini dapat membantu mempercepat
pengerjaan tugas besar.
Semua link kebutuhan praktikum ada di drive ini: public
Adapun ketentuan seperti berikut.

1. JAR File Wajib bisa dijalankan di sistem operasi **LINUX**.
   a. Akan ada **pengurangan nilai** jika terjadi anomali.
   b. Berhak tidak dinilai jika program **tidak jalan total** atau gagal mensimulasikan
   **mayoritas kondisi** untuk _test case_ yang dibuat asisten.
   c. Windows Subsystem Linux atau **WSL** diperbolehkan untuk substitusi sistem
   operasi **LINUX**.
   d. Dihimbau untuk setiap kelompok mengerjakan di **WSL** atau **Linux** daripada diakhir
   saat demo tidak jalan sama sekali/ada kasus unik.
2. Diperbolehkan memakai versi java **8** atau **diatasnya** (11, 17, 19, dsb.)
   a. Harus jalan di **salah satu versi Java** 8/9/11/15/17/19++. Tuliskan versi Java yang
   kalian pakai sebagai pada java-version.txt pada zip pengumpulan.
3. Aplikasi GUI harus desktop app **non web based** tidak boleh diakali seperti menggunakan
   _webview_ dari Java untuk menampilkan tampilan HTML & CSS.
4. Tidak boleh plagiat dari internet maupun kelompok lain.
5. Jangan asal spesifikasi selesai atau program jalan. Nilai bisa hancur meskipun selesai
   namun desain jelek dan tidak memaksimalkan OOP. Terlebih lagi, nilai individu bisa jelek
   kalau tidak paham apa-apa mengenai hal yang dikerjakan.

### Spesifikasi Sistem

#### GUI IDE dan Build Tools

```
● Aplikasi wajib menggunakan JavaFX
sebagai framework GUI.
● Desain antarmuka dibebaskan, namun
harus tetap mendukung seluruh
fungsionalitas sistem.
● Seluruh fitur aplikasi harus dapat diakses
melalui GUI.
● CSS diperbolehkan. HTML dan Web
Rendering dilarang.
```

```
● Disarankan menggunakan IntelliJ IDEA
karena mendukung pengelolaan JDK,
library, dan integrasi build tools dengan
baik.
● Aplikasi wajib menggunakan Maven
sebagai build automation tool.
● Build tools digunakan untuk:
○ Menambahkan library eksternal
seperti JSON/XML parser, PDF
generator, dll.
○ Otomatisasi proses kompilasi dan
build.
○ Pengelolaan struktur proyek secara
modular.
```

### Ketentuan Teknis

Berikut adalah hal-hal yang **minimal** wajib diimplementasikan di aplikasi yang Anda buat.

```
Basic OOP
```

1. Inheritance, Composition & Polymorphism
2. Interface, Generics, Wildcard, Exception
3. Gunakan Java Collection dan/atau Stream API sesuai kebutuhan
4. Multithreading dan Reflection melalui fitur load plugin
5. Terapkan prinsip SOLID dan minimal 5 design pattern referensi
   **Assertion**

```
Wajib mengimplementasikan konsep Assertion minimal pada 3 hingga 5
fungsi krusial yang melibatkan perubahan state permainan (Game State)
atau algoritma yang kompleks.
```

```
Additional Notes
```

1. Hindari menggunakan konsep OOP diatas hanya karena diwajibkan
   saja! Gunakanlah konsep OOP yang sesuai dengan kasusnya!
2. DRY (Don’t Repeat Yourself), tidak memiliki kode duplikat, pindahkan
   ke fungsi/class.
3. Memiliki struktur kelas yang mudah dipahami.
4. Dekomposisi yang baik dan implementasi yang tidak terlalu kompleks
   (sebuah method tidak terlalu panjang). Pecah-pecah dan buat method
   baru agar tidak terlalu kompleks.
5. Maksimalkan SOLID terutama pada open-closed principle dan
   dependency inversion. Karena tugas besar ini memiliki sistem plugin.
6. Usahakan memaksimalkan penggunaan konsep OOP, terutama konsep
   polymorphism, inheritance, composition dan sebagainya.

## Referensi Antarmuka

### User Flow

## Gambar 2. User flow Banana Republic

### Referensi Layar

#### Layar Main Menu

## Gambar 3. Referensi Layar Main Menu

#### Layar Lobby (Game Setup)

## Gambar 4. Referensi Layar Lobby

#### Layar Game

## Gambar 5. Referensi Layar Game

#### Trade Dialog

## Gambar 6. Referensi Layar Trade Dialog

#### Experiment Cards Dialog

## Gambar 7. Referensi Layar Experiment Cards Dialog

#### Settings & Plugins Dialog

## Gambar 8. Referensi Layar Settings & Plugins Dialog

#### Victory Declarations Dialog

## Gambar 9. Referensi Layar Victory Declarations Dialog

#### Steal Card Dialog

## Gambar 10. Referensi Layar Steal Card Dialog

#### Discard Card Dialog

## Gambar 11. Referensi Layar Discard Dialog

#### Layar Transisi Turn

## Gambar 12. Referensi Layar Transisi Turn

#### Layar Game Result

## Gambar 13. Referensi Layar Game Result

## Mekanisme

Program yang Anda buat harus memenuhi beberapa mekanisme berikut

### Inisiasi Permainan

Banana Republic adalah game strategi papan berbasis komputer untuk **3-4 pemain**. Pemain
berperan sebagai Nimon yang **membangun Pos Pantau, mendirikan Laboratorium,
mengumpulkan sumber daya alam Kepulauan Banana, berdagang, dan berlomba mencapai 10
Poin Prestasi terlebih dahulu** untuk memenangkan permainan.

#### Persiapan Awal

Setup Awal Permainan:

1. Setiap pemain akan mendapatkan masing masing satu warna unik
2. Setiap pemain pada awalnya akan memiliki 5 Pos Pantau, 4 Laboratorium dan 15 Pipa
   Transportasi sesuai warna yang dipilih
3. Setiap pemain memiliki Kartu Biaya Pembangunan (tampilan referensi rumus
   pembangunan seperti pada Tabel Biaya Pembangunan)
4. Sumber daya yang dimiliki oleh pemain akan ditampilkan secara terpisah sesuai jenis kartu,
   dengan menampilkan jumlah setiap jenisnya
5. Terdapat deck Kartu Temuan Dr. Neroifa yang akan diambil secara acak ketika terjadi
   penukaran Kartu Temuan Dr. Neroifa
6. Nimon Ungu berada di petak Gurun pada awal permainan

#### Fase Set-Up: Inisiasi Papan

##### Menentukan Urutan Giliran

Setiap pemain melempar kedua dadu. Pemain dengan nilai total tertinggi menjadi pemain pertama.
Jika seri, ulangi lemparan. Giliran berlanjut searah jarum jam.

##### Putaran Pertama (Clockwise)

1. Pemain pertama menempatkan 1 Pos Pantau di persimpangan (intersection) mana pun
   yang kosong sesuai pilihannya. ( _Distance Rule_ langsung berlaku dari Inisiasi Papan, silakan
   lihat bagian Aturan Pembangunan untuk detailnya.)
2. Kemudian pemain pertama menempatkan 1 Pipa Transportasi yang terhubung ke Pos
   Pantau tersebut (salah satu dari 3 sisi yang tersedia).
3. Pemain berikutnya melakukan hal yang sama, searah jarum jam, hingga semua pemain
   telah menempatkan 1 Pos Pantau + 1 Pipa.

##### Putaran Kedua (Counter-Clockwise)

1. Pemain yang terakhir di Putaran Pertama memulai Putaran Kedua.
2. Urutan berkebalikan (berlawanan arah jarum jam), sehingga pemain pertama di Putaran
   Satu menempatkan Pos Pantau keduanya paling akhir.
3. Setiap pemain menempatkan 1 Pos Pantau kedua dan 1 Pipa Transportasi yang terhubung
   ke Pos Pantau kedua tersebut.
4. Pos Pantau kedua tidak harus terhubung ke Pos Pantau pertama. Pipa harus menempel
   pada Pos Pantau kedua.

##### Sumber Daya Awal

```
Sumber Daya Awal
Setelah Putaran Kedua, setiap pemain menerima sumber daya awal dari petak-petak terrain
yang bersebelahan dengan Pos Pantau KEDUA mereka.
Contoh: Jika Pos Pantau kedua Kebin bersebelahan dengan petak Hutan (Kayu), Bukit (Batu Bata),
dan Gunung (Bijih), maka Kebin mengambil 1 Kayu + 1 Batu Bata + 1 Bijih dari bank.
```

#### Mode Peta

```
Mode Keterangan
Peta Fixed Tata letak papan sudah ditentukan (fixed).
Peta Acak (Plugin) (Bonus) Lihat keterangannya di Bonus.
```

### Peta Kepulauan Banana Republic

#### Ilustrasi Peta

```
Gambar ini hanyalah ilustrasiGambar yang bisa^ 14. jadi^ Ilustrasi aturan^ Peta game^ Banana tidak sepenuhnyaRepublic.^ terpenuhi (Source Gemini)
```

#### Jenis-Jenis Petak Terrain

Peta terdiri dari 19 petak heksagonal dengan berbagai jenis terrain:

```
Terrain Sumber Daya Jumlah Petak & Deskripsi
Hutan Kayu (🪵) 4 petak - Pohon-pohon lebat
khas Kepulauan Banana
Bukit Batu Bata (🧱) 3 petak - Bebatuan dan tanah
liat merah
Ladang Gandum (🌾) 4 petak - Ladang gandum
kuning keemasan
Gunung Bijih (⛏) 3 petak - Tambang batu dan
mineral berharga
Kebun Pisang Pisang (🍌) 4 petak - Perkebunan pisang
khas Banana Republic
Gurun - (Tidak ada) 1 petak - Kandang Nimon
Ungu nakal, tidak
menghasilkan apapun
```

#### Token Angka

Setiap petak terrain (kecuali Gurun) mendapatkan 1 token angka bulat berlabel 2–12. Token ini
menentukan kapan petak tersebut menghasilkan sumber daya. Berikut merupakan ketentuan
Token Angka:

1. Token 2 dan 12 masing-masing hanya ada 1 token
2. Token 7 tidak ada sama sekali
3. Token 3-6 dan 8 - 11 semuanya ada 2 token
4. Token 6 dan 8 diberi warna berbeda, yaitu merah
   **Pastikan Peta/Papan tidak melanggar ketentuan Jumlah Token diatas**

#### Struktur Papan

Beberapa konsep penting dalam struktur peta:

1. **Persimpangan (Intersection):** Titik pertemuan 3 petak heksagonal (atau 1-2 petak dengan
   bingkai laut). **Hanya di sinilah Pos Pantau dan Laboratorium dapat dibangun**
2. **Jalur (Path):** Sisi antara 2 petak heksagonal, atau antara petak dengan bingkai laut. **Hanya**
   **di sinilah Pipa Transportasi dapat dibangun.** Setiap jalur maksimal 1 Pipa
3. **Bingkai Laut (Frame):** Keping-keping tepi yang membentuk lautan di sekitar pulau.
   Sebagian bingkai memiliki pelabuhan
4. **Pesisir (Coast):** Petak terrain yang berbatasan langsung dengan bingkai laut. Bisa dibangun
   Pos Pantau dan sering terdapat Pelabuhan

#### Pelabuhan (Harbor)

Ada 9 pelabuhan di tepian kepulauan. Untuk memanfaatkan pelabuhan, pemain harus memiliki Pos
Pantau atau Laboratorium di persimpangan yang berbatasan dengan pelabuhan tersebut.

```
Jenis Pelabuhan Efek Trading
Pelabuhan Umum (3:1) Tukarkan 3 sumber daya SEJENIS apapun
dengan 1 sumber daya pilihan dari bank
Pelabuhan Khusus Pisang (2:1) Tukarkan 2 Pisang dengan 1 sumber daya
pilihan dari bank
Pelabuhan Khusus Kayu (2:1) Tukarkan 2 Kayu dengan 1 sumber daya pilihan
dari bank
Pelabuhan Khusus Batu Bata (2:1) Tukarkan 2 Batu Bata dengan 1 sumber daya
pilihan dari bank
Pelabuhan Khusus Gandum (2:1) Tukarkan 2 Gandum dengan 1 sumber daya
pilihan dari bank
Pelabuhan Khusus Bijih (2:1) Tukarkan 2 Bijih dengan 1 sumber daya pilihan
dari bank
```

### Alur Giliran & Mekanisme Dadu

#### Urutan Giliran

Pemain pertama (ditentukan saat inisiasi) memulai giliran pertama di awal permainan. Di setiap
gilirannya, pemain melalui tiga fase: **_Resource Gathering_** , **_Trade/Build_** **,** dan **_End Turn_****.** Pemain yang
sedang melakukan gilirannya disebut sebagai **pemain aktif**.

#### Resource Gathering

Di setiap awal gilirannya, pemain harus melempar 2 dadu enam sisi (2d6) menghasilkan nilai acak.
Jika hasil lemparan dadu **BUKAN** 7,

1. Setiap petak yang memiliki token angka sesuai dengan hasil lemparan dadu akan
   menghasilkan sumber daya sesuai dengan jenis petak tersebut (kecuali jika ada Nimon
   Ungu, Lihat Efek Nimon Ungu nomor 2)
2. Setiap pemain yang memiliki pos pantau dan/atau laboratorium di persimpangan petak
   yang menghasilkan sumber daya, akan mendapatkan sumber daya tersebut.
3. Pemain akan mendapatkan satu kartu sumber daya untuk setiap pos pantau dan dua kartu
   sumber daya untuk setiap laboratorium
4. Lihat bagian Sumber Daya Kepulauan untuk penjelasan lebih lanjut
   Jika hasil lemparan dadu **ADALAH** 7, Nimon Ungu akan aktif! Lihat bagian Nimon Ungu untuk
   penjelasan lebih lanjut.
   Untuk keperluan pengujian dan demonstrasi, **program harus menyediakan fitur lempar dadu
   manual.** Setiap kali program harus melempar dadu, berikan opsi untuk mengatur hasil dadu yang
   dilempar. Pengguna dapat mengatur hasilnya dalam rentang 1-6 untuk satu dadu.

#### Trade/Build

Setelah fase _Resource Gathering_ selesai, pemain aktif memasuki fase _Trade/Build_. Pada fase ini
pemain aktif bebas untuk melakukan jual beli (Lihat Jual Beli & Pelabuhan) dan melakukan
pembangunan (Lihat Pembangunan). Pemain aktif bisa melakukan kedua hal tersebut kapan pun
pada fase ini (jual beli tidak harus setelah pembangunan atau sebaliknya) dan sebanyak mungkin
(jual beli dibatasi oleh kemauan dari pemain lain dan pembangunan dibatasi oleh banyaknya
sumber daya yang dimiliki). Contohnya: Pemain aktif bisa saja melakukan jual beli untuk mendapat
sumber daya, kemudian membangun sebuah pos pantau dekat pelabuhan, lalu melakukan jual beli
maritim memanfaatkan pos pantau yang baru saja dibangun, lalu melakukan jual beli lagi atau
mambangun pada satu giliran yang sama.

#### End Turn

Ketika pemain aktif sudah tidak dapat lagi melakukan jual beli atau membangun atau ingin
mengakhiri gilirannya, pemain aktif dapat menyatakan bahwa gilirannya sudah berakhir (menekan
tombol _end turn_ ). Pemain selanjutnya sesuai dengan urutan permainan akan menjadi pemain aktif
dan memulai gilirannya.

### Sumber Daya Kepulauan

#### Jenis Sumber Daya

```
Sumber Daya Kegunaan Utama
Kayu (🪵) Membangun Pipa & Pos Pantau
Batu Bata (🧱) Membangun Pipa & Pos Pantau
Gandum (🌾) Membangun Pos Pantau, Lab, & Kartu Temuan
Bijih (⛏) Membangun Laboratorium & Kartu Temuan
Pisang (🍌) Membangun Pos Pantau & Kartu Temuan
```

#### Cara Mendapatkan Sumber Daya

Sumber daya diperoleh melalui mekanisme berikut:

1. **Produksi Normal:** Setiap kali dadu dilempar (oleh siapapun), semua petak dengan token
   angka yang sesuai berproduksi. Semua pemain yang punya bangunan di persimpangan
   sekitar petak tersebut menerima sumber daya
2. **Sumber Daya Awal:** Diterima saat inisiasi permainan (setelah Putaran Kedua set-up),
   berdasarkan petak di sekitar Pos Pantau kedua
3. **Barter/Perdagangan:** Menukar sumber daya dengan pemain lain atau dengan terminal
   dagang Gro di pelabuhan
4. **Kartu Inovasi:** Beberapa Kartu Inovasi (Progress Cards) memungkinkan pemain
   mengambil sumber daya langsung dari bank
5. **Mencuri via Nimon Ungu:** Ketika Nimon Ungu aktif, pemain aktif bisa mencuri 1 kartu
   sumber daya acak dari lawan

#### Bank Sumber Daya (Sistem Finite)

```
Keterbatasan Bank Sumber Daya
Bank sumber daya bersifat TERBATAS (finite). Setiap jenis sumber daya memiliki jumlah kartu
yang tetap dalam permainan:
```

1. Jumlah kartu setiap jenis sumber daya = 19 kartu
2. Jika bank sudah habis untuk satu jenis sumber daya dan terjadi produksi di petak
   tersebut:
   a. Jika kekurangan mempengaruhi LEBIH DARI 1 pemain: tidak ada pemain yang
   menerima sumber daya jenis tersebut pada giliran itu.
   b. Jika kekurangan hanya mempengaruhi 1 pemain: pemain tersebut menerima
   sebanyak yang tersisa di bank.
3. Sumber daya yang dikembalikan pemain ke bank (karena Nimon Ungu / pembangunan)
   akan^ mengisi^ kembali^ bank^ tersebut,^ sehingga^ bisa^ diambil^ kembali^ oleh^ pemain^ lain.^

### Nimon Ungu

#### Posisi Awal

Nimon Ungu SELALU memulai permainan di petak Gurun (satu-satunya petak tanpa token angka).

#### Efek Nimon Ungu

Ketika pemain aktif melempar dadu dan hasilnya berjumlah 7:

1. Setiap pemain yang memegang lebih dari 7 kartu sumber daya, WAJIB membuang
   setengah kartu sumber daya tersebut dibulatkan ke bawah. Contoh: 9 kartu sumber daya
   di tangan, buang 4 kartu sumber daya. Kartu Temuan Dr. Neroifa TIDAK DIHITUNG dan
   TIDAK BISA DIBUANG karena efek ini.
2. Pemain aktif WAJIB memindahkan Nimon Ungu ke petak terrain lain (tidak boleh tetap
   berada di petak yang sama). Nimon Ungu bisa dipindahkan ke petak terrain mana pun
   termasuk Gurun. Selama Nimon Ungu berada di suatu petak terrain, petak tersebut TIDAK
   DAPAT menghasilkan sumber daya bahkan jika nomor petak tersebut muncul pada hasil
   lemparan dadu.
3. Setelah memindahkan Nimon Ungu, pemain aktif BISA (tidak wajib) mencuri satu kartu
   sumber daya secara acak dari pemain lain yang memiliki pos pantau atau laboratorium
   yang berada di persimpangan petak baru Nimon Ungu dan memiliki kartu sumber daya.
   Jika terdapat lebih dari satu pemain yang termasuk persyaratan tersebut, pemain aktif
   memilih dari siapa ia mencuri.

#### Mencuri

Detail mekanisme pencurian sumber daya:

1. Pemain aktif dapat melihat seberapa banyak kartu sumber daya yang dipegang pemain lain.
2. Pemain aktif memilih pemain yang akan ia curi.
3. Pemain aktif secara acak mendapatkan kartu sumber daya dari pemain yang dipilih (bisa
   menggunakan sistem RNG yang sama seperti lempar dadu).

#### Memindahkan Nimon Ungu dengan Kartu Penjaga

Nimon Ungu juga bisa dipindahkan dengan memainkan Kartu Penjaga (Knight Card). Lihat Jenis
Kartu Temuan untuk penjelasan lebih lanjut.

### Biaya Pembangunan

#### Tabel Biaya Pembangunan

```
Bangunan / Item Biaya Sumber Daya Efek & Keterangan
Pipa Transportasi 1 🪵 + 1 🧱 Menghubungkan bangunan. Max 15
per pemain. Dibangun di jalur
Pos Pantau 1 🪵 + 1 🧱 + 1 🍌 + 1 🌾 Menghasilkan 1 sumber daya per
produksi. Bernilai 1 Poin Prestasi.
Max 5 per pemain
Laboratorium 3 ⛏ + 2 🌾 Laboratorium didapatkan dengan
melakukan upgrade Pos Pantau
menggunakan Biaya Sumber Daya.
Lab menghasilkan 2 sumber daya per
produksi. Bernilai 2 Poin Prestasi.
Max 4 per pemain
Kartu Temuan 1 ⛏ + 1 🍌 + 1 🌾 Ambil 1 kartu dari tumpukan deck.
Isinya acak antara Penjaga, Inovasi,
atau Poin Prestasi Rahasia
```

#### Aturan Pembangunan

##### Pipa Transportasi

1. Pipa baru HARUS terhubung ke salah satu Pipa, Pos Pantau, atau Laboratorium milik
   pemain yang sama
2. Hanya 1 Pipa yang bisa dibangun di setiap jalur
3. Pipa bisa dibangun di sepanjang pesisir (coast path)
4. Jumlah maksimal Pipa per pemain: 15 buah

##### Pos Pantau

1. WAJIB terhubung ke minimal 1 Pipa milik pemain yang sama (kecuali saat inisiasi)
2. WAJIB mengikuti Aturan Jarak ( **Distance Rule** ): semua 3 persimpangan yang bersebelahan
   harus kosong (tidak ada Pos Pantau atau Laboratorium siapapun)
3. Bernilai 1 Poin Prestasi
4. Jumlah maksimal Pos Pantau per pemain: 5 buah. Jika semua sudah terpakai, harus
   upgrade ke Laboratorium dulu agar mendapat kembali Pos Pantau ke supply

##### Laboratorium

1. TIDAK bisa dibangun langsung (harus mengupgrade Pos Pantau yang sudah ada)
2. Pos Pantau yang telah di- _upgrade_ dikembalikan ke supply pemain (bisa digunakan lagi)
3. Bernilai 2 Poin Prestasi (menggantikan 1 Poin Prestasi Pos Pantau)
4. Menghasilkan 2 kartu sumber daya per produksi
5. Jumlah maksimal Laboratorium per pemain: 4 buah

##### Kartu Temuan Dr. Neroifa

1. Ambil kartu teratas dari tumpukan deck
2. Kartu disembunyikan (tidak diperlihatkan ke pemain lain) hingga dimainkan
3. Tidak bisa diperdagangkan, dibuang ketika kena aturan nimons ungu atau diberikan ke
   pemain lain
4. Tidak bisa membeli kartu jika deck sudah habis

### Kartu Temuan Dr. Neroifa & Kartu Spesial

```
Dr. Neroifa rajin membuat penemuan baru di labnya. Setiap kartu yang diciptakan memiliki kekuatan
unik. Telusuri informasi setiap kartu pada bagian berikut
```

#### Aturan Umum Kartu Temuan

1. Pemain boleh memainkan MAKSIMAL 1 Kartu Temuan per giliran
2. Kartu boleh dimainkan kapan saja selama giliran (sebelum/sesudah lempar dadu,
   sebelum/sesudah membangun)
3. Kartu yang baru dibeli pada giliran yang sama TIDAK boleh langsung dimainkan (kecuali
   Kartu Poin Prestasi Rahasia dalam kondisi tertentu)
4. Kartu disimpan tersembunyi (tidak diperlihatkan ke pemain lain) hingga dimainkan
5. Kartu tidak bisa diperdagangkan, dibuang ketika kena aturan nimons ungu atau diberikan
   ke pemain lain

#### Jenis Kartu Temuan

```
Kartu Penjaga (Knight Card)
Jumlah: 14 kartu
```

```
Efek: Ketika dimainkan, pemain aktif WAJIB memindahkan Nimon Ungu (sama seperti
melempar angka 7), NAMUN tidak ada penalti kartu berlebih untuk pemain lain.
```

1. Pindahkan Nimon Ungu ke petak terrain mana pun (tidak boleh tetap di tempat sama).
2. Setelah memindahkan, boleh mencuri 1 kartu sumber daya acak dari pemain yang punya
   bangunan di sekitar petak baru Nimon Ungu.
   Kartu Penjaga yang dimainkan diperlihatkan ke semua pemain. Tumpukan Kartu Penjaga yang
   diperlihatkan ini sangat penting untuk mendapatkan Kartu Spesial Pasukan Terbesar.

```
Kartu Inovasi (Progress Card)
Jumlah: 6 kartu (3 dari setiap jenis)
```

1. **Konstruksi Cepat (Road Building):** Pemain boleh langsung menempatkan 2 Pipa
   Transportasi baru secara gratis di mana pun sesuai aturan normal pembangunan Pipa
2. **Monopoli Nimon (Monopoly):** Pemain menyebutkan 1 jenis sumber daya. Semua pemain
   lain yang memiliki kartu sumber daya jenis ini HARUS menyerahkan SEMUA kartu
   tersebut ke pemain yang sedang menggunakan kartu ini. Jika terdapat pemain lain yang
   tidak punya, maka dia tidak perlu menyerahkan apa apa
   Setelah dimainkan, Kartu Inovasi dikeluarkan dari permainan ke deck buangan

```
Kartu Poin Prestasi Rahasia (Victory Point Card)
Jumlah: 5 kartu (masing-masing bernilai 1 Poin Prestasi)
Kartu Poin Prestasi Rahasia HANYA dapat dilihat oleh pemilik kartu sampai pemain
memenangkan permainan (atau sampai akhir permainan jika pemain lain menang).
PENGECUALIAN: Jika kartu yang baru dibeli adalah Kartu Poin Prestasi Rahasia yang membuat
total Poin Prestasi pemain mencapai 10 atau lebih, pemain BOLEH langsung mengungkapkan
kartu tersebut dan memenangkan permainan pada giliran itu.
```

#### Kartu Spesial

Ada 2 Kartu Spesial yang tidak dibeli, melainkan diraih melalui pencapaian tertentu. Setiap pemain
yang berhasil meraih Kartu Spesial ini akan mendapatkan 2 Poin Prestasi:
**Jalan Terpanjang (Longest Road)**

1. Pemain pertama yang membangun jalan Pipa Transportasi berurutan sepanjang minimal
   5 ruas (tanpa percabangan) mendapatkan Kartu Spesial ini
2. Jika pemain lain membangun jalan yang lebih panjang, ia langsung mengambil alih Kartu
   Spesial beserta 2 Poin Prestasinya. Misal A awalnya punya Longest Road (Poin: 7). Lalu B
   (Poin: 5) berhasil bangun lebih panjang. Pemain A kehilangan 2 poin (7-2 = 5), Pemain B
   mendapatkan 2 poin (5+2=7)
3. Jika jaringan Pipa bercabang, hanya cabang terpanjang yang dihitung.
4. Pemain lain bisa MEMUTUS jalan terpanjang dengan membangun Pos Pantau di
   persimpangan yang kosong di sepanjang jalan tersebut. Jika jaringan terpanjang
   terpecah, Kartu Spesial berpindah ke pemain baru yang kini punya jalan terpanjang.
5. Jika terpecah dan hasilnya seri antara 2 pemain atau lebih:
   a. Jika Seri karena Perebutan: Pemain lama tetap memegang kartu sampai ada
   pemain lain yang benar-benar melampaui panjang jalannya (misal: dari 6 ruas ke
   7 ruas)
   b. Jika Seri karena Terputus: Jika jalan si pemegang kartu diputus oleh pemain lain,
   lalu terjadi situasi seri (misal: Pemain A dan Pemain B sama-sama punya 5 ruas),
   maka kartu disisihkan (dikembalikan ke bank). Tidak ada yang mendapat poin
   sampai salah satu dari mereka menambah satu ruas lagi

```
Pasukan Terbesar (Largest Army)
```

1. Pemain pertama yang memainkan 3 Kartu Penjaga (3 kartu tersebut dapat dilihat telah
   digunakan di deck pemain tersebut) mendapatkan Kartu Spesial ini.
2. Jika pemain lain memainkan lebih banyak Kartu Penjaga dari pemilik saat ini, ia langsung
   mengambil alih Kartu Spesial beserta 2 Poin Prestasinya. Sama seperti penjelasan 2 di
   kartu Longest Road
3. Kartu Penjaga yang sudah dimainkan TIDAK bisa diambil kembali, tetap diperlihatkan di
   deck sebagai kartu spesial yang sudah dimainkan

### Jual Beli & Pelabuhan

#### Barter Antar Nimon (Domestic Trade)

```
Aturan Barter Langsung
```

1. Hanya bisa dilakukan pada fase 2 giliran (setelah lempar dadu)
2. PENTING: Pemain non-aktif TIDAK BOLEH berdagang di antara mereka sendiri. Semua
   transaksi harus melibatkan pemain aktif
3. Pemain aktif tidak wajib menerima tawaran apapun
4. Tidak boleh "memberi" kartu secara cuma-cuma (setiap transaksi harus ada pertukaran
   dari kedua pihak)
5. Tidak boleh menukar sumber daya sejenis (misalnya 2 Kayu ditukar 1 Kayu)
6. Tidak boleh memperdagangkan Kartu Temuan Dr. Neroifa
7. Untuk menyederhanakan proses tawar-menawar dari game aslinya, sistem perdagangan
   antar pemain di Banana Republic difokuskan menjadi interaksi langsung 1-lawan-1.
   Berikut adalah alur prosesnya:
   a. Inisiasi Penawaran
   i. Pemain Aktif memilih jenis dan jumlah resource yang ingin diberikan,
   serta resource yang ingin diminta
   ii. Sistem mengecek inventaris semua pemain lain. Layar UI hanya akan
   menampilkan daftar Pemain Non-Aktif yang memiliki resource yang
   diminta dengan jumlah yang mencukupi
   iii. Pemain Aktif kemudian memilih satu pemain dari daftar tersebut sebagai

```
target transaksi
b. Respon Pemain Target
Pemain Non-Aktif yang dipilih kemudian meninjau tawaran tersebut dan
memiliki tiga pilihan aksi:
i. Accept: Menyetujui pertukaran. Transaksi dieksekusi dan selesai
ii. Reject: Menolak tawaran. Transaksi dibatalkan
iii. Counter-Offer: Mengajukan penawaran balik dengan mengubah
komposisi resource yang ingin ditukar
c. Negosiasi Berlanjut
i. Jika target melakukan Counter-Offer, hak untuk merespon kembali ke
Pemain Aktif
ii. Pemain Aktif kini dapat memilih untuk Accept, Reject, atau kembali
melakukan Counter-Offer
iii. Proses tawar-menawar bergantian ini terus berlanjut hingga salah satu
pihak akhirnya memilih Accept (transaksi berhasil) atau Reject (transaksi
batal)
```

#### Terminal Dagang Gro / Pelabuhan (Maritime Trade)

Pemain bisa berdagang dengan "bank" (Terminal Dagang Gro) meskipun tidak ada pemain lain
yang mau barter.
**Tiga Tingkat Tarif Terminal Dagang Gro
Tarif Dasar (4:1)** → Selalu tersedia untuk semua pemain, bahkan tanpa bangunan di pelabuhan.
Tukarkan 4 kartu sumber daya SEJENIS dengan 1 kartu sumber daya pilihan bebas dari bank.
**Pelabuhan Umum (3:1)** → Tersedia jika pemain punya Pos Pantau/Lab di persimpangan
Pelabuhan Umum. Tukarkan 3 kartu sumber daya SEJENIS (jenis apa pun) dengan 1 kartu
sumber daya pilihan bebas dari bank.
**Pelabuhan Khusus (2:1)** → Tersedia jika pemain punya Pos Pantau/Lab di persimpangan
Pelabuhan Khusus sumber daya tertentu. Tukarkan 2 kartu sumber daya JENIS YANG TERTERA
di pelabuhan tersebut dengan 1 kartu sumber daya pilihan bebas dari bank. Pelabuhan khusus
HANYA berlaku untuk sumber daya yang tertera, sumber daya lain tetap di tarif dasar.

```
Berdagang dengan Terminal Dagang Gro di pelabuhan HANYA bisa dilakukan saat giliran pemain
aktif. Pemain tidak bisa menggunakan pelabuhan di luar giliran mereka.
```

### Kondisi Menang & Poin Prestasi

Pemain dapat memenangkan permainan dengan mendapatkan **10 poin prestasi pada gilirannya.**
Secara umum, poin prestasi dapat diperoleh dari beberapa hal berikut:

```
Sumber Poin Prestasi Nilai
```

```
Setiap Pos Pantau yang dibangun 1 Poin Prestasi
Setiap Laboratorium yang dibangun 2 Poin Prestasi
Kartu Spesial: Jalan Terpanjang 2 Poin Prestasi
Kartu Spesial: Pasukan Terbesar 2 Poin Prestasi
Setiap Kartu Poin Prestasi Rahasia 1 Poin Prestasi per kartu
```

**Catatan penting soal Laboratorium:** Ketika Pos Pantau di- _upgrade_ menjadi Laboratorium, poin
dari pos pantau tersebut **digantikan** (bukan ditambahkan). Jadi 1 Pos Pantau (1 PP) yang
diupgrade menjadi Laboratorium akan bernilai 2 PP, bukan 3 PP.
Setiap pemain memulai permainan dengan **2 Pos Pantau** yang sudah terpasang di peta (dari fase
Set-Up). Artinya, setiap pemain **sudah mulai dengan 2 PP**.

### Multithreading

#### Countdown Timer Giliran

Setiap pemain hanya memiliki 90 detik untuk menyelesaikan Fase 2 (Jual Beli) dan Fase 3
(Membangun) pada giliran mereka. Lempar dadu tidak terhitung dalam timer.

```
Spesifikasi Timer
```

1. Timer dimulai otomatis setelah pemain aktif menyelesaikan lemparan dadu.
2. Timer ditampilkan secara real-time di UI JavaFX.
3. Jika timer mencapai 0 sebelum pemain menyelesaikan aksi, giliran otomatis berpindah
   ke pemain berikutnya.
4. Tindakan yang sudah dilakukan sebelum timer habis tetap berlaku (tidak di-rollback).
5. Timer harus berjalan di _background thread_ tanpa memblokir antarmuka utama.

### Sistem Plugin

##### Kartu Temuan Baru (Plugin JAR)

```
Mekanisme Plugin Kartu Temuan Baru
Interface yang Harus Disiapkan:
```

1. Praktikan wajib membuat sebuah interface (contoh: ExperimentCard) dengan method
   yang mendefinisikan kontrak kartu, minimal:
   a. String getCardName() → mengembalikan nama kartu
   b. String getDescription() → mengembalikan deskripsi efek kartu

```
c. void applyEffect(GameState state, Player player) → menerapkan efek kartu ke
state permainan
```

2. Pastikan beberapa kelas ini memiliki method berikut
   public .., enum BANANA ResourceType {

}public (^) interface Player {
Stringint getResourceCount(ResourceType getName(); (^) type);
void addResource(ResourceType type, int amount);
(^) } void removeResource(ResourceType type, int amount);
public List`<Player>` interface GameStategetAllPlayers(); { (^)
(^) } Bank getBank();
public boolean interface hasResource(ResourceType Bank { (^) type, int amount);
(^) } void takeResource(ResourceType type, int amount);
**JAR Loader:**

1. Praktikan membuat komponen JAR Loader yang mampu menerima file .jar secara
   runtime menggunakan URLClassLoader.
2. JAR Loader harus bisa diakses/dipicu saat game sedang berjalan (tidak perlu restart).
3. Asisten akan mengirimkan file .jar-nya pada saat demo.
   **Mekanisme Loading & Penggunaan:**
4. Sistem membaca file .jar dari asisten menggunakan Java Reflection (Class.forName,
   newInstance, dll.)
5. Kelas dalam .jar harus mengimplementasikan interface ExperimentCard yang telah
   disiapkan.
6. Kartu yang berhasil dimuat dimasukkan ke tumpukan deck bersama kartu lainnya
   (dikocok ulang atau ditambahkan ke atas/bawah deck sesuai spesifikasi).
7. Ketika kartu tersebut dibeli dan dimainkan oleh pemain, method applyEffect() dipanggil
   secara polymorphism melalui tipe interface.
   **_Gunakan URLClassLoader untuk memuat .jar secara dinamis. Pastikan class yang diload
   mengimplementasikan interface yang sudah didefinisikan sebelumnya — validasi ini penting
   agar_**^ **_tidak_**^ **_terjadi_**^ **_ClassCastException._**^

### Save & Load Permainan

#### Kebutuhan Save/Load

Sistem Save/Load wajib dapat menyimpan dan memuat state permainan yang mencakup
setidaknya:

1. Konfigurasi peta (posisi setiap terrain hex, token angka, pelabuhan)
2. Posisi Nimon Ungu
3. Data setiap pemain: nama, warna, posisi semua bangunan di papan, kartu di tangan
   (sumber daya + kartu pembangunan), kartu spesial yang dimiliki
4. Poin Prestasi setiap pemain (termasuk yang dari kartu tersembunyi)
5. Giliran saat ini (pemain mana yang sedang aktif)
6. Status timer giliran
7. Sisa kartu di bank sumber daya
8. Sisa deck Kartu Temuan Dr. Neroifa

Untuk tugas besar ini, struktur dari Save/Load dibebaskan ke kelompok kalian. Silahkan sesuaikan
dengan _design_ yang sudah / akan kalian buat.

#### Rekomendasi Format & Struktur

```
Rekomendasi Implementasi Save/Load
Format yang Direkomendasikan: JSON atau Serialisasi Java (.ser)
```

1. JSON lebih mudah dibaca dan di-debug. Disarankan menggunakan library seperti Gson
   atau Jackson.
2. Serialisasi Java lebih mudah diimplementasikan (implements Serializable) namun kurang
   portable.
   **Struktur Minimal File Save (JSON):**
3. "version": versi format save (untuk kompatibilitas masa depan)
4. "timestamp": waktu penyimpanan
5. "mapConfig": konfigurasi lengkap peta
6. "players": array data setiap pemain
7. "currentPlayerIndex": indeks pemain yang giliran sekarang
8. "bank": sisa kartu bank per sumber daya
9. "deck": sisa kartu di deck (bisa direpresentasikan sebagai count saja untuk menjaga
   kerahasiaan)
   8.^ "robberPosition":^ posisi^ Nimon^ Ungu^ saat^ ini^

## Fitur Bonus

#### Generator Peta Baru

```
Mekanisme Plugin Generator Peta
Implementasi Interface:
```

1. Praktikan membuat interface MapGeneratorPlugin dengan method Board
   generateBoard().
2. Secara default, game menggunakan StandardMapGenerator yang menghasilkan peta
   Banana Republic standar.
   **Plugin Eksternal:**
3. Praktikan juga menyediakan .jar seperti DonutIsland.jar atau VolcanoIsland.jar (bebas).
4. .jar ini berisi implementasi MapGeneratorPlugin dengan algoritma penempatan unik.
5. Game membaca .jar di AWAL permainan (sebelum game dimulai, bukan saat berjalan).
6. Board yang dihasilkan plugin menjadi dasar rendering GUI heksagonal.
   **_Plugin Map Generator hanya bisa diload SEBELUM permainan dimulai (saat layar lobby/setup),
   bukan saat permainan sudah berjalan._**

#### Pemain Bot

```
Mekanisme Plugin Bot
Implementasi Interface:
```

1. Praktikan membuat interface PlayerStrategy dengan method Action
   takeTurn(GameState state).
2. Interface ini mendefinisikan kontrak untuk semua strategi bot.
   **Plugin Eksternal:**
3. Praktikan juga menyediakan .jar seperti GreedyBot.jar (selalu tukar ke bank jika bisa)
   atau AggressiveBot.jar (selalu beli Kartu Penjaga) (atau mekanisme lain sesuai
   kreativitas).
4. Saat giliran bot, mesin game memanggil method takeTurn() dari kelas hasil Reflection.
5. Plugin bot hanya bisa diload di AWAL permainan (saat pemilihan pemain di lobby).

```
Plugin^ Bot^ hanya^ bisa^ diload^ SEBELUM^ permainan^ dimulai,^ bukan^ di^ tengah^ permainan.^
```

#### BGM & SFX Engine (Audio Player)

```
Mekanisme Audio Engine
BGM (Background Music): Audio looping terus-menerus selama permainan berlangsung.
```

```
SFX (Sound Effects): Audio pendek yang bisa overlap dengan BGM dan SFX lain (contoh: suara
dadu, suara bangunan, suara Nimon Ungu mencuri).
Pemutaran audio wajib berjalan secara asinkron dan tidak boleh menghambat responsivitas
antarmuka ( non-blocking UI thread ).
```

#### Animasi Background

```
Mekanisme Animasi Background
Implementasi background yang hidup misalnya dengan menambahkan objek bergerak di area
lautan (pinggiran pulau) seperti awan, kapal mini, atau karakter Nimon kecil. Elemen visual
yang digunakan harus diimplementasikan se- lightweight mungkin guna memastikan beban
rendering tidak mengganggu kelancaran UI Thread.
```

## Deliverables

Deliverables dalam Tugas Besar 2 ini mencakup **_source code_** dan **laporan tugas besar**. Keduanya
dikumpulkan bersamaan dengan membuat rilis di Github Classroom sebelum deadline yang telah
ditentukan. Untuk mempermudah, telah disediakan format pembuatan laporan tugas besar
IF2010_TB2_Laporan_XXX dan template code yang telah bisa dijalankan.

## Asistensi & QnA

Asistensi bersifat **optional** namun sangat dianjurkan untuk melakukan asistensi untuk melakukan
_alignment_ terkait arsitektur kelas atau aplikasi yang masih kurang jelas. Asistennya tetap sama
seperti sebelumnya, komunikasikan jadwal dengan asisten terlebih dahulu jika mau melakukan
asistensi. Pertanyaan terkait dengan spesifikasi yang belum jelas atau hal-hal yang berkaitan
dengan masalah teknis pada tugas besar dapat ditanyakan pada borang QnA
QnA - Tugas Besar

#### Penting!

_Mohon hindari bertanya atau meminta klarifikasi saat sudah mendekati deadline, ya. Kami akan
berusaha sebaik mungkin untuk merespons pertanyaan kalian, namun mohon pengertiannya bahwa
asisten tidak selalu bisa standby setiap waktu. Jadi, pastikan kalian tidak bertanya secara dadakan.
Terima kasih dan semangat pengerjaan Tubes-nya!_

## Daftar Referensi

1. Game ini terinspirasi dari board game Catan dan/atau Colonist. Jadi jika masih bingung
   terkait gameplay nya, kalian juga bisa mengunjungi web ini Colonist: Play Settlers of Catan
   Alternative - Free Online Game atau Game Rules. Tentu terdapat beberapa penyesuaian di
   tugas besar ini, jadi perhatikan kembali apa yang ada di spesifikasi ini. Jika dirasa ada yang
   ambigu, segera tanyakan via QnA.
2. Berikut adalah dokumentasi yang dapat digunakan untuk pembuatan map hexagonal How
   To Build a Hex Map Based Application - PragmaticCoding
3. Instalasi IntelliJ IDEA https://www.jetbrains.com/idea/download
4. Jika dibutuhkan, How to Create a JavaFX Maven Project in IntelliJ IDEA from Scratch:
   Step-by-Step Guide
5. Konsep SOLID dan design pattern referensi

### Extras

```
~ Semoga seluruh rangkaian kegiatan perkuliahan OOP memberikan kesan pembelajaran yang
menyenangkan yaa untuk kalian. Selamat menempuh fase kehidupan berikutnya dan sampai jumpa di
lain waktu hehehe 👋
```
