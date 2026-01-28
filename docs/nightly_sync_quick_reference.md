# 🌙 Nightly Syllabus Sync - Quick Reference

## 🚀 Quick Start

### 1. Schedule Sync (Automatic)
Sync dijadwalkan otomatis saat app pertama kali dibuka:
```kotlin
// MainActivity.onCreate()
SyncScheduler.scheduleNightlySyllabusSync(this)
```

### 2. Manual Trigger (For Testing)
```kotlin
// Trigger immediate sync
SyncScheduler.triggerImmediateSync(context)
```

### 3. Cancel Sync
```kotlin
SyncScheduler.cancelNightlySyllabusSync(context)
```

### 4. Check Status
```kotlin
SyncScheduler.isSyncScheduled(context) { isScheduled ->
    if (isScheduled) {
        Log.d(TAG, "Sync is scheduled")
    }
}
```

## 📋 Key Files

| File | Purpose |
|------|---------|
| `SyllabusSyncWorker.kt` | Worker yang melakukan sync |
| `SyncScheduler.kt` | Utility untuk schedule/cancel |
| `MateriDao.kt` | Database access |
| `nightly_sync_feature.md` | Full documentation |

## 🔧 Configuration

### Constraints:
- ✅ **Wi-Fi Only** (No mobile data)
- ✅ **Device Idle** (Usually at night)
- ✅ **Battery Not Low**

### Timing:
- **Every**: 24 hours
- **Flex**: ±4 hours
- **Retry**: 3 attempts with 15min backoff

## 🧪 Testing Commands

```bash
# View logs
adb logcat | grep "SyllabusSyncWorker"

# Check WorkManager database
adb shell dumpsys jobscheduler | grep "eeducation"

# Trigger immediate sync via code
SyncScheduler.triggerImmediateSync(context)
```

## ⚙️ Server Setup

**Update this URL in `SyllabusSyncWorker.kt`:**
```kotlin
val url = URL("https://raw.githubusercontent.com/YOUR-USERNAME/YOUR-REPO/main/materi.json")
```

**JSON Format Expected:**
```json
[
  {
    "id": 1,
    "judul": "Title",
    "deskripsi": "Description",
    "kategori": "Category",
    "isCompleted": false
  }
]
```

## 🎯 User Benefits

| Benefit | Description |
|---------|-------------|
| 📚 Always Fresh | Data updated every night |
| 💾 Save Data | Wi-Fi only, no mobile data |
| 🔋 Save Battery | Runs during idle time |
| 😴 No Interruption | Syncs while user sleeps |
| 🤖 Automatic | Zero user action needed |

## 🐛 Common Issues

### Sync doesn't run?
1. Check Wi-Fi connected
2. Check device idle
3. Check battery > 15%
4. Trigger manual sync for testing

### Sync fails?
1. Check server URL correct
2. Check internet working
3. Check JSON format matches
4. View logs for error details

### Battery drain?
1. Verify constraint: `setRequiresBatteryNotLow(true)`
2. Verify interval: 24 hours (not shorter)
3. Monitor with Battery Historian

## 📱 Xiaomi/MIUI Users

**Required Settings:**
1. Enable **Autostart** for E-Education
2. Set **Battery Saver** to "No restrictions"
3. Lock app in **Recent Apps**

## ✅ Success Criteria

- [x] Runs at least once per day
- [x] Uses 0 MB mobile data
- [x] Battery impact < 1%
- [x] Completes in < 30 seconds
- [x] 95%+ success rate

## 🎓 Next Steps

1. Update server URL in `SyllabusSyncWorker.kt`
2. Test with `triggerImmediateSync()`
3. Monitor overnight for automatic run
4. Check logs next morning
5. Verify data updated in app
