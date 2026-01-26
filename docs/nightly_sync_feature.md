# 🌙 Nightly Syllabus Sync Feature

## 📖 Overview
Fitur **Nightly Syllabus Sync** secara otomatis mensinkronisasi data materi/kurikulum dari server ke database lokal (Room) setiap tengah malam tanpa mengganggu pengguna dan tanpa menghabiskan kuota data seluler.

## ✨ Features

### 1. **Automatic Background Sync**
- Berjalan otomatis setiap **24 jam**
- Dijadwalkan saat device **idle** (biasanya tengah malam saat user tidur)
- Tidak mengganggu aktivitas user di siang hari

### 2. **Network-Aware**
- **Hanya menggunakan Wi-Fi** (`NETWORK_TYPE_UNMETERED`)
- **Tidak menggunakan data seluler** - menghemat kuota
- Otomatis menunggu sampai Wi-Fi tersedia

### 3. **Battery-Friendly**
- Hanya berjalan saat **battery tidak low**
- Hanya berjalan saat **device idle** (tidak sedang digunakan)
- Menggunakan WorkManager yang battery-efficient

### 4. **Smart Retry Mechanism**
- Jika gagal, akan retry otomatis sampai 3x
- Menggunakan exponential backoff (15 menit interval)
- Tidak akan spam server jika ada masalah

## 🏗️ Architecture

### Components

#### 1. **SyllabusSyncWorker**
`app/src/main/java/.../background/worker/SyllabusSyncWorker.kt`

Worker class yang melakukan sync actual:
- Fetch data dari GitHub Raw/Server
- Parse JSON response
- Clear database lama
- Insert data baru ke Room database

```kotlin
class SyllabusSyncWorker : CoroutineWorker {
    override suspend fun doWork(): Result {
        // 1. Fetch from server
        // 2. Parse JSON
        // 3. Update Room database
        // 4. Return success/retry/failure
    }
}
```

#### 2. **SyncScheduler**
`app/src/main/java/.../utils/SyncScheduler.kt`

Utility class untuk manage scheduling:
- `scheduleNightlySyllabusSync()` - Jadwalkan periodic sync
- `cancelNightlySyllabusSync()` - Cancel sync
- `triggerImmediateSync()` - Trigger manual sync (untuk testing)
- `isSyncScheduled()` - Check status sync

#### 3. **MateriDao**
Database access object dengan method:
- `insertAll(List<Materi>)` - Insert bulk data
- `deleteAll()` - Clear semua data lama

## 📱 User Experience

### Keuntungan untuk User:
1. ✅ **Materi selalu up-to-date** - Setiap pagi data sudah fresh
2. ✅ **Hemat kuota** - Tidak menggunakan data seluler
3. ✅ **Hemat battery** - Hanya berjalan saat idle
4. ✅ **Tidak mengganggu** - Sync di tengah malam
5. ✅ **Otomatis** - Tidak perlu manual refresh

### Skenario Penggunaan:
1. **Malam hari (00:00 - 05:00)**:
   - User tidur
   - HP terhubung Wi-Fi
   - HP idle (tidak digunakan)
   - WorkManager trigger sync
   - Data ter-update tanpa user sadari

2. **Pagi hari**:
   - User buka app
   - Materi sudah terbaru
   - Tidak perlu loading/refresh

## 🔧 Configuration

### Constraints yang Diterapkan:
```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi only
    .setRequiresDeviceIdle(true)                   // Idle only
    .setRequiresBatteryNotLow(true)                // Battery not low
    .build()
```

### Scheduling Parameters:
- **Repeat Interval**: 24 hours
- **Flex Interval**: 4 hours (bisa jalan 4 jam sebelum/sesudah target)
- **Backoff Policy**: Exponential, 15 minutes
- **Max Retry**: 3 attempts

## 🧪 Testing

### Manual Testing:
```kotlin
// Trigger immediate sync (tidak tunggu malam)
SyncScheduler.triggerImmediateSync(context)

// Check if scheduled
SyncScheduler.isSyncScheduled(context) { isScheduled ->
    Log.d(TAG, "Sync scheduled: $isScheduled")
}
```

### Logcat Monitoring:
```
adb logcat | grep "SyllabusSyncWorker"
adb logcat | grep "SyncScheduler"
```

### Expected Logs:
```
SyncScheduler: Nightly syllabus sync scheduled successfully
SyllabusSyncWorker: Starting nightly syllabus sync...
SyllabusSyncWorker: Fetched 10 materi items from server
SyllabusSyncWorker: Syllabus sync completed successfully. 10 items synced.
```

## 📊 WorkManager Inspection

### Check Work Status via ADB:
```bash
adb shell dumpsys jobscheduler | grep "com.kelompok10.eeducation"
```

### View WorkManager Database:
```bash
adb shell
run-as com.kelompok10.eeducation
cd databases
sqlite3 androidx.work.workdb
SELECT * FROM WorkSpec;
```

## 🔐 Permissions

### Required Permissions (already in AndroidManifest.xml):
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

No additional permissions needed - WorkManager handles everything internally.

## 🚀 Deployment Checklist

- [x] Add WorkManager dependency to `build.gradle.kts`
- [x] Create `SyllabusSyncWorker.kt`
- [x] Create `SyncScheduler.kt`
- [x] Add `deleteAll()` to `MateriDao.kt`
- [x] Schedule sync in `MainActivity.onCreate()`
- [x] Configure server URL in worker
- [ ] Update server URL to actual endpoint
- [ ] Test on real device overnight
- [ ] Monitor battery consumption
- [ ] Check logs for successful sync

## 🌐 Server Integration

### Current Configuration:
```kotlin
// In SyllabusSyncWorker.kt
val url = "https://raw.githubusercontent.com/your-username/your-repo/main/materi.json"
```

### TODO: Update URL
Ganti dengan URL actual server/GitHub Raw Anda yang menyediakan JSON dengan format:

```json
[
  {
    "id": 1,
    "judul": "Pengenalan Kotlin",
    "deskripsi": "Dasar-dasar pemrograman Kotlin",
    "kategori": "Programming",
    "isCompleted": false
  },
  ...
]
```

## 📈 Future Improvements

### Potential Enhancements:
1. **Differential Sync** - Hanya sync data yang berubah (bukan clear all)
2. **Last Sync Timestamp** - Tampilkan kapan terakhir sync di UI
3. **Sync Statistics** - Track berapa kali sync berhasil/gagal
4. **Conflict Resolution** - Handle jika user edit local data
5. **Push Notifications** - Notify user jika ada materi baru
6. **Background Fetch** - iOS-style background refresh

## 🐛 Troubleshooting

### Issue: Sync tidak jalan
**Solusi:**
- Check constraint: Apakah device idle + Wi-Fi connected?
- Check battery: Apakah battery low?
- Trigger manual sync untuk testing: `SyncScheduler.triggerImmediateSync()`

### Issue: Sync gagal terus
**Solusi:**
- Check server URL benar
- Check internet connection
- Check JSON format sesuai dengan model `Materi`
- Lihat logcat untuk error detail

### Issue: Battery drain
**Solusi:**
- Pastikan constraint `setRequiresBatteryNotLow(true)` aktif
- Pastikan interval 24 jam, bukan lebih sering
- Monitor dengan Battery Historian

## 📝 Notes

- WorkManager **guaranteed** akan dijalankan, meskipun app di-kill
- Jika semua constraint tidak terpenuhi dalam 7 hari, WorkManager akan paksa run
- Setelah reboot, WorkManager otomatis re-schedule work yang pending
- Xiaomi/MIUI users perlu enable "Autostart" untuk optimal performance

## 🎯 Success Metrics

Fitur ini berhasil jika:
- ✅ Sync berjalan minimal 1x per hari
- ✅ Hanya menggunakan Wi-Fi (0 MB data seluler)
- ✅ Battery consumption < 1% per sync
- ✅ Sync selesai dalam < 30 detik
- ✅ Success rate > 95%
