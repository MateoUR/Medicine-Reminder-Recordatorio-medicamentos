# App Recordatorios - Kotlin

Traduccion completa de la app Kivy/Python a Kotlin nativo para Android.

## Estructura del proyecto

```
AppRecordatorios/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/org/mateoUR/apprecordatorios/
│       │   ├── MainActivity.kt          <- punto de entrada + permisos
│       │   ├── DataManager.kt           <- lectura/escritura JSON
│       │   ├── NotificationHelper.kt    <- canal y envio de notificaciones
│       │   ├── AlarmScheduler.kt        <- alarmas exactas con AlarmManager
│       │   ├── AlarmReceiver.kt         <- recibe alarmas, muestra notificacion
│       │   ├── BootReceiver.kt          <- reprograma alarmas al encender
│       │   ├── ReminderService.kt       <- Foreground Service segundo plano
│       │   ├── SoundManager.kt          <- sonidos de boton y notificacion
│       │   ├── MenuFragment.kt
│       │   ├── MedicationFragment.kt
│       │   ├── AppointmentFragment.kt
│       │   ├── DeleteMedicationFragment.kt
│       │   ├── DeleteAppointmentFragment.kt
│       │   └── HelpFragment.kt
│       └── res/
│           ├── layout/                  <- XMLs de cada pantalla
│           ├── values/colors.xml
│           ├── values/themes.xml
│           ├── xml/nav_graph.xml        <- navegacion entre pantallas
│           ├── drawable/                <- imagenes de fondo + ic_notification.png
│           └── raw/                     <- sonidos MP3
├── build.gradle
└── settings.gradle
```

## Pasos para abrir en Android Studio

1. Abrir Android Studio
2. File -> Open -> seleccionar la carpeta AppRecordatorios
3. Esperar que Gradle sincronice

## Archivos que debes agregar manualmente (no incluidos por derechos de imagen/audio)

Copiar en res/drawable/:
- bg_menu.png
- bg_medications.png
- bg_appointments.png
- ic_launcher.png        (icono de la app, 512x512)
- ic_notification.png    (icono blanco 24x24 para la notificacion)

Copiar en res/raw/:
- sound_button.mp3
- sound_notification.mp3

## Equivalencias Python -> Kotlin

| Python (Kivy)                  | Kotlin                          |
|--------------------------------|---------------------------------|
| DataMixin                      | DataManager.kt                  |
| send_notification()            | NotificationHelper.kt           |
| schedule_alarm()               | AlarmScheduler.kt               |
| BootReceiver.java              | BootReceiver.kt                 |
| service.py                     | ReminderService.kt + AlarmReceiver.kt |
| play_sound()                   | SoundManager.kt                 |
| MenuScreen                     | MenuFragment.kt                 |
| MedicationReminderScreen       | MedicationFragment.kt           |
| MedicalAppointmentsScreen      | AppointmentFragment.kt          |
| DeleteRemindersScreen          | DeleteMedicationFragment.kt     |
| DeleteAppointmentsScreen       | DeleteAppointmentFragment.kt    |
| HelpScreen                     | HelpFragment.kt                 |
| ScreenManager                  | NavHostFragment + nav_graph.xml |
