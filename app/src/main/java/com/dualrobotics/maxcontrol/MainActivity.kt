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
class MainActivity:ComponentActivity(){
 private lateinit var bt:BluetoothController;private lateinit var status:TextView;private lateinit var dot:TextView;private val navy=Color.rgb(11,30,61);private val navy2=Color.rgb(16,43,82);private val orange=Color.rgb(255,122,24);private val white=Color.rgb(244,247,251);private val muted=Color.rgb(159,179,204);private var turbo=false
 override fun onCreate(b:Bundle?){super.onCreate(b);if(android.os.Build.VERSION.SDK_INT>=31)ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_SCAN),20);build()}
 @SuppressLint("MissingPermission") private fun build(){bt=BluetoothController(BluetoothAdapter.getDefaultAdapter()).also{it.onStateChanged={ok,n->runOnUiThread{status.text=if(ok)"CONECTADO · $n"else n;status.setTextColor(if(ok) Color.rgb(50,213,131) else Color.rgb(255,92,92));dot.setTextColor(status.currentTextColor)}}};val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(22,24,22,14);setBackgroundColor(navy)};root.addView(header(),LinearLayout.LayoutParams(-1,72));root.addView(card(),LinearLayout.LayoutParams(-1,72));root.addView(label("CONTROL DE MOVIMIENTO"),LinearLayout.LayoutParams(-1,42));val frame=FrameLayout(this).apply{setPadding(0,8,0,8)};val grid=GridLayout(this).apply{columnCount=3;rowCount=3};add(grid,"↑\nADELANTE",'F',1,0);add(grid,"←\nIZQUIERDA",'L',0,1);add(grid,"STOP",'S',1,1);add(grid,"→\nDERECHA",'R',2,1);add(grid,"↓\nATRÁS",'B',1,2);frame.addView(grid,FrameLayout.LayoutParams(-1,-1));root.addView(frame,LinearLayout.LayoutParams(-1,0,1f));root.addView(button("⚡  TURBO  ·  APAGADO",orange).apply{setOnClickListener{turbo=!turbo;text=if(turbo)"⚡  TURBO  ·  ENCENDIDO"else"⚡  TURBO  ·  APAGADO";setBackgroundColor(if(turbo)orange else navy2);bt.send('V')}},LinearLayout.LayoutParams(-1,58));root.addView(label("DIAGNÓSTICO · PRÓXIMAMENTE\nBatería • Temperatura • Motores • Sensores • Jarvis",true),LinearLayout.LayoutParams(-1,70));setContentView(root)}
 private fun header()=LinearLayout(this).apply{gravity=android.view.Gravity.CENTER_VERTICAL;addView(TextView(this@MainActivity).apply{text="◈  MAX CONTROL";textSize=23f;setTextColor(white);typeface=android.graphics.Typeface.DEFAULT_BOLD},LinearLayout.LayoutParams(0,-1,1f));addView(TextView(this@MainActivity).apply{text="DUAL ROBOTICS";textSize=11f;setTextColor(orange)})}
 private fun card()=LinearLayout(this).apply{setPadding(16,8,16,8);setBackgroundColor(navy2);gravity=android.view.Gravity.CENTER_VERTICAL;dot=TextView(this@MainActivity).apply{text="●";textSize=18f;setTextColor(Color.RED)};status=TextView(this@MainActivity).apply{text="DESCONECTADO";textSize=12f;setTextColor(Color.RED);setPadding(8,0,0,0)};addView(dot);addView(status,LinearLayout.LayoutParams(0,-1,1f));addView(button("CONECTAR",orange).apply{setOnClickListener{choose()}},LinearLayout.LayoutParams(125,48))}
 private fun label(s:String,center:Boolean=false)=TextView(this).apply{text=s;textSize=if(center)11f else 12f;setTextColor(muted);gravity=if(center)android.view.Gravity.CENTER else android.view.Gravity.CENTER_VERTICAL;letterSpacing=.12f}
 private fun button(s:String,c:Int)=MaterialButton(this).apply{text=s;textSize=12f;setTextColor(white);setBackgroundColor(navy2);strokeColor=android.content.res.ColorStateList.valueOf(c);strokeWidth=2;cornerRadius=18}
 private fun add(g:GridLayout,s:String,c:Char,col:Int,row:Int){val b=button(s,orange);b.textSize=if(c=='S')15f else 17f;b.setOnTouchListener{_,e->when(e.action){MotionEvent.ACTION_DOWN->{if(c=='S')bt.send('S')else bt.startContinuous(c);true};MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{if(c!='S')bt.stopContinuous();true};else->false}};val p=GridLayout.LayoutParams(GridLayout.spec(row,1,1f),GridLayout.spec(col,1,1f));p.setMargins(5,5,5,5);g.addView(b,p)}
 @SuppressLint("MissingPermission") private fun choose(){val d=bt.pairedDevices();if(d.isEmpty()){Toast.makeText(this,"Empareja primero el HC-05/HC-06 desde Ajustes de Bluetooth",Toast.LENGTH_LONG).show();return};MaterialAlertDialogBuilder(this).setTitle("Seleccionar MAX").setItems(d.map{"${it.name?:"Sin nombre"}\n${it.address}"}.toTypedArray()){_,i->bt.connect(d[i])}.setNegativeButton("Cancelar",null).show()}
 override fun onDestroy(){bt.shutdown();super.onDestroy()}
}