package org.mateoUR.apprecordatorios

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment

// ============================================================
// MainActivity
// Punto de entrada de la app.
// Solicita permisos en tiempo de ejecucion, inicia el
// Foreground Service y aloja el NavHostFragment que gestiona
// la navegacion entre pantallas.
// Equivalente a ReminderApp.build() + on_start() de Python.
// ============================================================

class MainActivity : AppCompatActivity() {

    // Launcher para solicitar multiples permisos a la vez
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (perm, granted) ->
            println("[PERMISO] $perm -> ${if (granted) "CONCEDIDO" else "DENEGADO"}")
        }
        startReminderService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // NavHostFragment declarado en activity_main.xml
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        requestRequiredPermissions()
    }

    private fun requestRequiredPermissions() {
        val needed = mutableListOf<String>()

        // POST_NOTIFICATIONS solo en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // SCHEDULE_EXACT_ALARM en Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)
                != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
            }
        }

        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        } else {
            startReminderService()
        }
    }

    private fun startReminderService() {
        val intent = Intent(this, ReminderService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
