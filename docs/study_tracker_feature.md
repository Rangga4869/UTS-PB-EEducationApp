# Study Tracker Service & Battery Monitor Feature

## Overview
The Study Tracker feature helps students track their study sessions with a foreground service that continues running even when the app is minimized. Additionally, it includes a battery monitor that alerts users when battery drops below 15%, encouraging them to take breaks.

## Components

### 1. StudyTimerService (Foreground Service)
**Location:** `app/src/main/java/com/kelompok10/eeducation/background/StudyTimerService.kt`

**Features:**
- ✅ Runs as a foreground service with persistent notification
- ✅ Continues tracking time when app is minimized
- ✅ Supports Start, Pause, Resume, and Stop actions
- ✅ Saves total study time across sessions
- ✅ Broadcasts timer events for UI updates
- ✅ Updates notification every second with current time

**Key Actions:**
- `ACTION_START_TIMER` - Start a new study session
- `ACTION_STOP_TIMER` - Stop and save the current session
- `ACTION_PAUSE_TIMER` - Pause the timer
- `ACTION_RESUME_TIMER` - Resume from pause

**Broadcasts:**
- `STUDY_TIMER_STARTED` - Timer started
- `STUDY_TIMER_STOPPED` - Timer stopped (includes elapsed time)
- `STUDY_TIMER_PAUSED` - Timer paused (includes elapsed time)
- `STUDY_TIMER_RESUMED` - Timer resumed
- `STUDY_TIMER_TICK` - Sent every second (includes current elapsed time)

### 2. BatteryLevelReceiver (Broadcast Receiver)
**Location:** `app/src/main/java/com/kelompok10/eeducation/background/BatteryLevelReceiver.kt`

**Features:**
- ✅ Monitors battery level changes
- ✅ Alerts when battery drops below 15%
- ✅ Only alerts when device is not charging
- ✅ 5-minute cooldown between alerts
- ✅ Shows high-priority notification with vibration
- ✅ Encourages students to take breaks

**Triggers:**
- `ACTION_BATTERY_CHANGED` - Battery level changed
- `ACTION_BATTERY_LOW` - System battery low event

**Broadcasts:**
- `BATTERY_LOW_ALERT` - Battery below 15% (includes battery level)

### 3. NotificationHelper (Utility)
**Location:** `app/src/main/java/com/kelompok10/eeducation/utils/NotificationHelper.kt`

**Features:**
- ✅ Centralized notification management
- ✅ Creates notification channels for Android O+
- ✅ Provides utility methods for showing/canceling notifications

**Channels:**
- `StudyTimerChannel` - Low importance, ongoing service notification
- `BatteryAlertChannel` - High importance, alert notifications
- `GeneralChannel` - Default importance, general notifications

### 4. StudyTrackerActivity (UI)
**Location:** `app/src/main/java/com/kelompok10/eeducation/ui/studytracker/StudyTrackerActivity.kt`

**Features:**
- ✅ Clean, intuitive UI for timer control
- ✅ Real-time timer display (HH:MM:SS format)
- ✅ Shows total accumulated study time
- ✅ Battery status display
- ✅ Start, Pause, Resume, Stop controls
- ✅ Receives and displays timer updates via broadcasts

**Layout:** `app/src/main/res/layout/activity_study_tracker.xml`

## Permissions

The following permissions have been added to `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

## How to Use

### For Students:

1. **Access Study Tracker:**
   - Open the app
   - Tap on the "⏱️ Study Tracker" card from the main menu

2. **Start a Study Session:**
   - Tap "Start Study Session" button
   - A persistent notification appears showing the timer
   - The app can be minimized - timer continues running

3. **Pause/Resume:**
   - Tap "Pause" to temporarily stop the timer
   - Tap "Resume" to continue from where you paused

4. **Stop Session:**
   - Tap "Stop" to end and save the current study session
   - Total study time is automatically updated

5. **Battery Monitoring:**
   - Battery monitoring is automatic
   - When battery drops below 15%, you'll receive an alert
   - This reminds you to take a break and charge your device

### Integration with MainActivity

The Study Tracker is accessible from the main menu:
- Added new "Study Tracker" card in the main activity
- Click handler navigates to `StudyTrackerActivity`

## Technical Details

### Service Lifecycle:
1. Service starts when user taps "Start"
2. Foreground notification keeps service alive
3. Service persists through app minimization
4. Service stops when user taps "Stop" or system kills it

### Data Persistence:
- Timer state saved in SharedPreferences
- Total study time accumulated across sessions
- Preferences file: `StudyTimerPrefs`

### Battery Monitoring:
- Receiver registered in manifest with intent filters
- Cooldown prevents notification spam
- Alert preferences: `BatteryAlertPrefs`

## Testing

### Test the Study Timer:
1. Start a study session
2. Minimize the app
3. Check notification tray - timer should be running
4. Return to app - time should be synchronized
5. Test pause/resume functionality
6. Stop session and verify total time is saved

### Test Battery Monitor:
1. Wait for battery to drop below 15% (or simulate in development)
2. Verify notification appears
3. Verify 5-minute cooldown works
4. Check that charging device prevents alerts

## Future Enhancements

Potential improvements:
- [ ] Add statistics and charts for study patterns
- [ ] Set study goals and daily targets
- [ ] Integration with calendar for scheduled study sessions
- [ ] Export study time reports
- [ ] Customizable low battery threshold
- [ ] Study session categories (e.g., Math, Science)
- [ ] Break reminders (Pomodoro technique)

## Architecture

```
MainActivity
    └── StudyTrackerActivity
            ├── StudyTimerService (Foreground Service)
            │       ├── Notification updates
            │       └── Broadcasts timer events
            └── BatteryLevelReceiver (System Receiver)
                    └── Monitors battery & sends alerts

NotificationHelper (Utility)
    └── Manages all notification channels
```

## Files Created/Modified

### New Files:
1. `StudyTimerService.kt` - Foreground service implementation
2. `BatteryLevelReceiver.kt` - Battery monitoring receiver
3. `NotificationHelper.kt` - Notification management utility
4. `StudyTrackerActivity.kt` - UI for timer control
5. `activity_study_tracker.xml` - Activity layout
6. `study_tracker_feature.md` - This documentation

### Modified Files:
1. `AndroidManifest.xml` - Added service, receiver, permissions
2. `MainActivity.kt` - Added Study Tracker navigation
3. `activity_main.xml` - Added Study Tracker card

## Notes

- The foreground service requires a persistent notification on Android O+
- Battery monitoring works passively - no polling required
- All broadcasts are local to the app for security
- Timer continues running even if device sleeps (with WAKE_LOCK)
- Notification permissions must be granted on Android 13+

## Support

For issues or questions about this feature, refer to:
- [Android Services Documentation](https://developer.android.com/guide/components/services)
- [Broadcast Receivers Documentation](https://developer.android.com/guide/components/broadcasts)
- [Foreground Services Best Practices](https://developer.android.com/guide/components/foreground-services)
