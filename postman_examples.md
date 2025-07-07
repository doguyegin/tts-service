# Postman Test Örnekleri

## 1. Temel API İsteği

### Method: POST
### URL: `http://localhost:8080/api/audio/speak`

### Headers:
```
Content-Type: application/json
```

### Body (raw JSON):
```json
{
  "text": "merhaba dünya"
}
```

### Beklenen Yanıt:
- HTTP Status: 200 OK
- Content-Type: audio/wav
- Dosya indirme (attachment)

---

## 2. Boş Metin Testi

### Method: POST
### URL: `http://localhost:8080/api/audio/speak`

### Headers:
```
Content-Type: application/json
```

### Body (raw JSON):
```json
{
  "text": ""
}
```

### Beklenen Yanıt:
- HTTP Status: 400 Bad Request

---

## 3. Null Metin Testi

### Method: POST
### URL: `http://localhost:8080/api/audio/speak`

### Headers:
```
Content-Type: application/json
```

### Body (raw JSON):
```json
{
  "text": null
}
```

### Beklenen Yanıt:
- HTTP Status: 400 Bad Request

---

## 4. Uzun Metin Testi

### Method: POST
### URL: `http://localhost:8080/api/audio/speak`

### Headers:
```
Content-Type: application/json
```

### Body (raw JSON):
```json
{
  "text": "merhaba dünya nasılsın bugün hava çok güzel"
}
```

### Beklenen Yanıt:
- HTTP Status: 200 OK (eğer tüm kelimeler için ses dosyası varsa)
- HTTP Status: 404 Not Found (eğer hiç ses dosyası bulunamazsa)

---

## 5. Özel Karakterler Testi

### Method: POST
### URL: `http://localhost:8080/api/audio/speak`

### Headers:
```
Content-Type: application/json
```

### Body (raw JSON):
```json
{
  "text": "merhaba! dünya? nasılsın..."
}
```

---

## Postman Environment Variables

### Base URL:
```
{{base_url}} = http://localhost:8080
```

### Tam URL:
```
{{base_url}}/api/audio/speak
```

---

## Test Scripts (Postman Tests)

### Başarılı İstek Testi:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Content-Type is audio/wav", function () {
    pm.response.to.have.header("Content-Type");
    pm.expect(pm.response.headers.get("Content-Type")).to.include("audio/wav");
});

pm.test("Response has attachment header", function () {
    pm.response.to.have.header("Content-Disposition");
    pm.expect(pm.response.headers.get("Content-Disposition")).to.include("attachment");
});
```

### Hata Testi:
```javascript
pm.test("Status code is 400 for invalid input", function () {
    pm.response.to.have.status(400);
});
```

---

## Log Kontrolü

Test sonrası log dosyalarını kontrol etmek için:

```bash
# API istekleri
tail -f logs/api-requests.log

# Hatalar
tail -f logs/errors.log

# Genel uygulama logları
tail -f logs/application.log
``` 