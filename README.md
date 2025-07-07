# 🎧 Spring Boot + PostgreSQL Tabanlı Metinden Sese (TTS) Eşleştirme ve Oynatma Servisi

Bu proje, gelen metin ifadelerini parçalara ayırarak PostgreSQL veritabanındaki hazır ses dosyalarıyla eşleştirir ve bunları sırayla birleştirerek sesli çıktı üretir.

---

## 🎯 Proje Amacı

Gelen metin ifadelerini analiz ederek, PostgreSQL'de kayıtlı olan `.wav` uzantılı ses dosyalarıyla eşleştirip, bu dosyaları sırayla birleştirerek oynatılabilir bir ses çıktısı üretmek.

---

## 🏗️ Genel Mimarî

```

[HTTP REST API]
│
▼
[Text Parse Service]
│
▼
[Audio Matching Service]
│
▼
[Audio Stitching Service]
│
▼
[Audio Streaming API]

```

---

## 📁 Proje Klasör Yapısı

```

src/
├── main/
│   ├── java/
│   │   └── com.example.audioengine/
│   │       ├── controller/
│   │       │   └── AudioController.java
│   │       ├── service/
│   │       │   ├── TextParserService.java
│   │       │   ├── AudioMatcherService.java
│   │       │   ├── AudioMergeService.java
│   │       │   └── AudioPlaybackService.java
│   │       ├── model/
│   │       │   └── AudioFile.java
│   │       ├── repository/
│   │       │   └── AudioFileRepository.java
│   │       └── config/
│   │           └── AppConfig.java
│   └── resources/
│       ├── application.yml
│       └── audio/  ← Ses dosyalarının fiziksel klasörü
└── test/
└── ...

````

---

## 🧩 Entity Örneği – `AudioFile.java`

```java
@Entity
@Table(name = "audio_files")
public class AudioFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword; // örn: "merhaba"

    private String filename; // örn: "merhaba.wav"
}
````

---

## 🛠️ Servis Örnekleri

### `TextParserService.java`

```java
@Service
public class TextParserService {
    public List<String> parseText(String input) {
        return Arrays.asList(input.toLowerCase().split("\\s+"));
    }
}
```

### `AudioMatcherService.java`

```java
@Service
public class AudioMatcherService {
    @Autowired private AudioFileRepository audioFileRepo;

    public List<File> matchWordsToAudio(List<String> words) {
        List<File> matchedFiles = new ArrayList<>();
        for (String word : words) {
            audioFileRepo.findByKeyword(word).ifPresent(audio -> {
                matchedFiles.add(new File("audio/" + audio.getFilename()));
            });
        }
        return matchedFiles;
    }
}
```

### `AudioMergeService.java`

```java
@Service
public class AudioMergeService {
    public File mergeAudioFiles(List<File> audioFiles) throws IOException {
        File output = new File("output/merged.wav");
        List<String> cmd = new ArrayList<>();
        cmd.add("ffmpeg");
        for (File file : audioFiles) {
            cmd.add("-i");
            cmd.add(file.getAbsolutePath());
        }
        cmd.add("-filter_complex");
        cmd.add("[0:0][1:0][2:0]concat=n=" + audioFiles.size() + ":v=0:a=1[out]");
        cmd.add("-map");
        cmd.add("[out]");
        cmd.add(output.getAbsolutePath());

        new ProcessBuilder(cmd).start();
        return output;
    }
}
```

### `AudioController.java`

```java
@RestController
@RequestMapping("/api/audio")
public class AudioController {
    @Autowired private TextParserService parserService;
    @Autowired private AudioMatcherService matcherService;
    @Autowired private AudioMergeService mergeService;

    @PostMapping("/speak")
    public ResponseEntity<Resource> speak(@RequestBody Map<String, String> payload) throws IOException {
        String input = payload.get("text");
        List<String> words = parserService.parseText(input);
        List<File> audioFiles = matcherService.matchWordsToAudio(words);
        File merged = mergeService.mergeAudioFiles(audioFiles);

        InputStreamResource resource = new InputStreamResource(new FileInputStream(merged));
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=output.wav")
            .contentType(MediaType.parseMediaType("audio/wav"))
            .body(resource);
    }
}
```

---

## ⚙️ `application.yml` Örneği

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/audio_db
    username: postgres
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

audio:
  path: audio/
  output-path: output/
```

---

## 🧪 Test Senaryoları

1. `"merhaba dünya"` girişi yapıldığında, `merhaba.wav` ve `dünya.wav` dosyaları birleştirilir ve çıktı olarak döner.
2. `"bilinmeyen kelime"` girilirse, sadece eşleşen ses dosyaları varsa oynatılır, eşleşmeyenler atlanır.
3. Boş ya da geçersiz input girilirse 400 Bad Request hatası döner.
4. `ffmpeg` çalışamazsa hata loglanır ve 500 hatası döner.

---

## 🛡️ Ekstra Özellikler (Opsiyonel)

* JWT veya API key ile erişim kontrolü
* Redis cache ile eşleşen seslerin hızlı getirilmesi
* Ses dosyalarının lokal yerine S3 veya CDN'den alınması
* NLP entegrasyonu ile imla düzeltme (örn: `slm` → `selam`)
* Basit bir web panel üzerinden ses dosyası yükleme ve eşleme arayüzü

---

## 🚀 Genişleme Olasılıkları

* Docker ile containerize edilerek taşınabilir hale getirilebilir.
* WebSocket ile canlı ses yayını yapılabilir.
* Queue yapısı (örneğin RabbitMQ) ile yüksek trafikte performans iyileştirilebilir.

``` 