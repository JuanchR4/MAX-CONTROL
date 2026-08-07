package com.dualrobotics.maxcontrol

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.dualrobotics.maxcontrol.bluetooth.BluetoothController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : ComponentActivity() {
    private lateinit var bt: BluetoothController
    private lateinit var status: TextView
    private lateinit var dot: TextView
    private val navy = Color.rgb(11,30,61)
    private val navy2 = Color.rgb(16,43,82)
    private val orange = Color.rgb(255,122,24)
    private val white = Color.rgb(244,247,251)
    private val muted = Color.rgb(159,179,204)
    private var turbo = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 31) ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), 20)
        buildUi()
    }

    @SuppressLint("MissingPermission") private fun buildUi() {
        bt = BluetoothController(BluetoothAdapter.getDefaultAdapter()).also { controller ->
            controller.onStateChanged = { connected, name -> runOnUiThread {
                status.text = if (connected) "CONECTADO · $name" else name
                status.setTextColor(if (connected) Color.rgb(50,213,131) else Color.rgb(255,92,92))
                dot.setTextColor(status.currentTextColor)
            } }
        }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(14,20,14,12); setBackgroundColor(navy) }
        root.addView(header(), LinearLayout.LayoutParams(-1,64))
        root.addView(connectionCard(), LinearLayout.LayoutParams(-1,62))
        root.addView(label("CONTROL DE MOVIMIENTO"), LinearLayout.LayoutParams(-1,42))
        root.addView(controls(), LinearLayout.LayoutParams(-1,0,1f))
        root.addView(MaterialButton(this).apply {
            text = "⚡  TURBO  ·  APAGADO"; textSize = 12f; setTextColor(white); setBackgroundColor(navy2); strokeColor = android.content.res.ColorStateList.valueOf(orange); strokeWidth = 2; cornerRadius = 18
            setOnClickListener { turbo = !turbo; text = if (turbo) "⚡  TURBO  ·  ENCENDIDO" else "⚡  TURBO  ·  APAGADO"; setBackgroundColor(if (turbo) orange else navy2); bt.send('V') }
        }, LinearLayout.LayoutParams(-1,56))
        root.addView(label("DIAGNÓSTICO · PRÓXIMAMENTE\nBatería • Temperatura • Motores • Sensores • Jarvis", true), LinearLayout.LayoutParams(-1,62))
        setContentView(root)
    }

    private fun controls(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(0,8,0,8)
        addView(row(controlButton("↑\nADELANTE", 'F'), null, null), LinearLayout.LayoutParams(-1,0,1f))
        addView(row(controlButton("←\nIZQUIERDA", 'L'), controlButton("STOP", 'S'), controlButton("→\nDERECHA", 'R')), LinearLayout.LayoutParams(-1,0,1f))
        addView(row(controlButton("↓\nATRÁS", 'B'), null, null), LinearLayout.LayoutParams(-1,0,1f))
    }

    private fun row(left: View, center: View?, right: View?): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        addView(Space(this@MainActivity), LinearLayout.LayoutParams(0,-1,1f))
        addView(left, LinearLayout.LayoutParams(0,-1,1f).apply { setMargins(4,4,4,4) })
        if (center != null) addView(center, LinearLayout.LayoutParams(0,-1,1f).apply { setMargins(4,4,4,4) }) else addView(Space(this@MainActivity), LinearLayout.LayoutParams(0,-1,1f))
        if (right != null) addView(right, LinearLayout.LayoutParams(0,-1,1f).apply { setMargins(4,4,4,4) }) else addView(Space(this@MainActivity), LinearLayout.LayoutParams(0,-1,1f))
        addView(Space(this@MainActivity), LinearLayout.LayoutParams(0,-1,1f))
    }

    private fun controlButton(title: String, command: Char): MaterialButton = MaterialButton(this).apply {
        text = title; textSize = if (command == 'S') 14f else 16f; setTextColor(white); setBackgroundColor(navy2); strokeColor = android.content.res.ColorStateList.valueOf(if (command == 'S') Color.rgb(255,92,92) else orange); strokeWidth = 2; cornerRadius = 18
        setOnTouchListener { _, event -> when (event.action) { MotionEvent.ACTION_DOWN -> { if (command == 'S') bt.send('S') else bt.startContinuous(command); true }; MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { if (command != 'S') bt.stopContinuous(); true }; else -> false } }
    }

    private fun header() = LinearLayout(this).apply { gravity = android.view.Gravity.CENTER_VERTICAL; addView(TextView(this@MainActivity).apply { text = "◈  MAX CONTROL"; textSize = 23f; setTextColor(white); typeface = android.graphics.Typeface.DEFAULT_BOLD }, LinearLayout.LayoutParams(0,-1,1f)); addView(TextView(this@MainActivity).apply { text = "DUAL ROBOTICS"; textSize = 11f; setTextColor(orange) }) }
    private fun connectionCard() = LinearLayout(this).apply { setPadding(12,6,12,6); setBackgroundColor(navy2); gravity = android.view.Gravity.CENTER_VERTICAL; dot = TextView(this@MainActivity).apply { text = "●"; textSize = 18f; setTextColor(Color.RED) }; status = TextView(this@MainActivity).apply { text = "DESCONECTADO"; textSize = 12f; setTextColor(Color.RED); setPadding(8,0,0,0) }; addView(dot); addView(status, LinearLayout.LayoutParams(0,-1,1f)); addView(MaterialButton(this@MainActivity).apply { text = "CONECTAR"; textSize = 11f; setTextColor(white); setBackgroundColor(navy2); strokeColor = android.content.res.ColorStateList.valueOf(orange); strokeWidth = 2; cornerRadius = 18; setOnClickListener { chooseDevice() } }, LinearLayout.LayoutParams(120,48)) }
    private fun label(textValue: String, centered: Boolean = false) = TextView(this).apply { text = textValue; textSize = if (centered) 11f else 12f; setTextColor(muted); gravity = if (centered) android.view.Gravity.CENTER else android.view.Gravity.CENTER_VERTICAL; letterSpacing = .12f }
    @SuppressLint("MissingPermission") private fun chooseDevice() { val devices = bt.pairedDevices(); if (devices.isEmpty()) { Toast.makeText(this,"Empareja primero el HC-05/HC-06 desde Ajustes de Bluetooth",Toast.LENGTH_LONG).show(); return }; MaterialAlertDialogBuilder(this).setTitle("Seleccionar MAX").setItems(devices.map { "${it.name ?: "Sin nombre"}\n${it.address}" }.toTypedArray()) { _, which -> bt.connect(devices[which]) }.setNegativeButton("Cancelar",null).show() }
    override fun onDestroy() { bt.shutdown(); super.onDestroy() }
}