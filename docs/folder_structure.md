app/
├── src/
│   ├── main/
│   │   ├── assets/
│   │   │   └── materi_awal.json         <-- (Data "hardcode" dalam bentuk JSON)
│   │   ├── java/com/kelompok10/eeducation/
│   │   │   ├── data/
│   │   │   │   ├── local/
│   │   │   │   │   ├── AppDatabase.kt   <-- (Konfigurasi Room)
│   │   │   │   │   ├── MateriDao.kt     <-- (Query Database)
│   │   │   │   │   └── MateriEntity.kt  <-- (Model Table Room)
│   │   │   │   ├── pref/
│   │   │   │   │   └── UserPreferences.kt <-- (Shared Preferences)
│   │   │   │   └── repository/
│   │   │   │       └── MateriRepository.kt <-- (Sumber data tunggal)
│   │   │   ├── ui/
│   │   │   │   ├── adapter/
│   │   │   │   │   └── MateriAdapter.kt
│   │   │   │   ├── materi/
│   │   │   │   │   ├── MateriActivity.kt
│   │   │   │   │   └── MateriDetailActivity.kt
│   │   │   │   └── viewmodel/
│   │   │   │       └── MateriViewModel.kt
│   │   │   ├── background/
│   │   │   │   ├── service/
│   │   │   │   │   └── UpdateMateriService.kt <-- (Service)
│   │   │   │   ├── receiver/
│   │   │   │   │   └── AlarmReceiver.kt      <-- (Broadcast Receiver)
│   │   │   │   ├── scheduler/
│   │   │   │   │   └── SyncJobService.kt     <-- (JobScheduler)
│   │   │   │   └── worker/
│   │   │   │       └── DownloadWorker.kt     <-- (Modern Background Task)
│   │   │   └── utils/
│   │   │       ├── JsonHelper.kt             <-- (Parsing JSON dari Assets)
│   │   │       └── NetworkUtils.kt           <-- (Internet Connection Check)
│   │   ├── res/
│   │   │   ├── layout/                       <-- (File XML UI)
│   │   │   ├── xml/
│   │   │   │   └── network_security_config.xml
│   │   │   └── values/
│   │   │       └── strings.xml
│   │   └── AndroidManifest.xml               <-- (Registrasi Service/Receiver)
└── build.gradle (Module: app)