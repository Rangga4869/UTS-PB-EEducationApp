# Study Tracker & Battery Monitor - Quick Reference

## Feature Summary

### 🎯 Study Timer Service
- **Type:** Foreground Service
- **Purpose:** Track study time continuously, even when app is minimized
- **Location:** `background/StudyTimerService.kt`
- **Features:**
  - Persistent notification with live timer
  - Start, Pause, Resume, Stop controls
  - Automatic session time saving
  - Broadcasts timer updates every second

### 🔋 Battery Level Receiver
- **Type:** Broadcast Receiver
- **Purpose:** Alert students when battery is low (< 15%)
- **Location:** `background/BatteryLevelReceiver.kt`
- **Features:**
  - Monitors battery level changes
  - High-priority notifications
  - 5-minute cooldown between alerts
  - Only alerts when not charging

### 📱 Study Tracker UI
- **Type:** Activity
- **Purpose:** User interface for controlling the timer
- **Location:** `ui/studytracker/StudyTrackerActivity.kt`
- **Features:**
  - Real-time timer display
  - Total study time statistics
  - Battery status monitor
  - Simple control buttons

## User Flow

```
1. User opens app
   └─> Clicks "Study Tracker" card
       └─> Opens StudyTrackerActivity
           └─> User clicks "Start Study Session"
               └─> StudyTimerService starts
                   ├─> Foreground notification appears
                   ├─> Timer counts up
                   └─> User can minimize app
                       └─> Timer continues running
                           └─> Battery monitoring active
                               └─> Alert if battery < 15%
```

## Implementation Checklist

✅ **StudyTimerService.kt** - Foreground service with timer logic
✅ **BatteryLevelReceiver.kt** - Battery monitoring receiver
✅ **NotificationHelper.kt** - Notification management utility
✅ **StudyTrackerActivity.kt** - UI controller
✅ **activity_study_tracker.xml** - UI layout
✅ **AndroidManifest.xml** - Permissions, service, and receiver registration
✅ **MainActivity.kt** - Added navigation to Study Tracker
✅ **activity_main.xml** - Added Study Tracker card
✅ **Documentation** - Feature documentation created

## Key Broadcasts

| Broadcast Action | Purpose | Extra Data |
|-----------------|---------|------------|
| `STUDY_TIMER_STARTED` | Timer started | None |
| `STUDY_TIMER_STOPPED` | Timer stopped | `elapsed_time` (Long) |
| `STUDY_TIMER_PAUSED` | Timer paused | `elapsed_time` (Long) |
| `STUDY_TIMER_RESUMED` | Timer resumed | None |
| `STUDY_TIMER_TICK` | Timer update (1s) | `elapsed_time` (Long) |
| `BATTERY_LOW_ALERT` | Battery below 15% | `battery_level` (Int) |

## Permissions Required

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## Testing Instructions

### Test Study Timer:
1. Open Study Tracker from main menu
2. Click "Start Study Session"
3. Verify notification appears
4. Minimize app
5. Wait 10 seconds
6. Return to app - verify timer is correct
7. Test Pause/Resume
8. Click Stop and verify total time updates

### Test Battery Monitor:
1. Let battery drain below 15% OR use developer options to simulate
2. Verify notification appears with alert
3. Verify cooldown (should not spam notifications)
4. Plug in charger - verify no alerts while charging

## Project Structure

```
com.kelompok10.eeducation/
├── background/
│   ├── StudyTimerService.kt          (NEW)
│   └── BatteryLevelReceiver.kt       (NEW)
├── ui/
│   ├── main/
│   │   └── MainActivity.kt           (MODIFIED)
│   └── studytracker/
│       └── StudyTrackerActivity.kt   (NEW)
└── utils/
    └── NotificationHelper.kt         (NEW)

res/
└── layout/
    ├── activity_main.xml             (MODIFIED)
    └── activity_study_tracker.xml    (NEW)

AndroidManifest.xml                   (MODIFIED)
```

## How It Works

### Service Lifecycle:
```
User clicks "Start"
    ↓
Intent sent to StudyTimerService
    ↓
Service starts in foreground mode
    ↓
Notification created and displayed
    ↓
Background thread updates timer every second
    ↓
Broadcasts sent to update UI
    ↓
User clicks "Stop"
    ↓
Service saves total time
    ↓
Service stops and removes notification
```

### Battery Monitoring:
```
System broadcasts BATTERY_CHANGED
    ↓
BatteryLevelReceiver receives broadcast
    ↓
Check battery percentage
    ↓
If < 15% AND not charging AND cooldown passed
    ↓
Show notification to user
    ↓
Save alert timestamp
    ↓
Broadcast BATTERY_LOW_ALERT
```

## Tips for Enhancement

1. **Add Pomodoro Timer:**
   - 25-minute study sessions
   - 5-minute break reminders
   
2. **Statistics Dashboard:**
   - Daily/weekly/monthly charts
   - Study patterns analysis
   
3. **Study Goals:**
   - Set daily targets
   - Achievement badges
   
4. **Categories:**
   - Tag study sessions by subject
   - Track time per subject

## Troubleshooting

**Timer doesn't update when app is minimized:**
- Verify foreground service notification is shown
- Check WAKE_LOCK permission

**Battery alerts not showing:**
- Verify receiver is registered in manifest
- Check notification permissions on Android 13+
- Ensure device is not charging

**Service stops unexpectedly:**
- Android may kill background services to save battery
- Foreground service provides best protection
- On some devices, add app to battery optimization whitelist

## References

- [Android Services Guide](https://developer.android.com/guide/components/services)
- [Foreground Services](https://developer.android.com/guide/components/foreground-services)
- [Broadcast Receivers](https://developer.android.com/guide/components/broadcasts)
- [Battery Monitoring](https://developer.android.com/training/monitoring-device-state/battery-monitoring)
