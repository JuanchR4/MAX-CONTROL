package com.dualrobotics.maxcontrol.bluetooth
import android.annotation.SuppressLint
import android.bluetooth.*
import kotlinx.coroutines.*
import java.io.IOException
import java.util.UUID
class BluetoothController(private val adapter: BluetoothAdapter?) {
 companion object { val SPP_UUID:UUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") }
 var onStateChanged:(Boolean,String)->Unit={_,_->}; private var socket:BluetoothSocket?=null; private var repeatJob:Job?=null; private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
 @SuppressLint("MissingPermission") fun pairedDevices()=adapter?.bondedDevices?.toList()?.sortedBy{it.name?:it.address}?:emptyList()
 @SuppressLint("MissingPermission") fun connect(device:BluetoothDevice){scope.launch{close();try{val s=device.createRfcommSocketToServiceRecord(SPP_UUID);s.connect();socket=s;withContext(Dispatchers.Main){onStateChanged(true,device.name?:device.address)}}catch(_:IOException){close();withContext(Dispatchers.Main){onStateChanged(false,"No se pudo conectar")}}}}
 fun send(c:Char)=scope.launch{sendNow(c)}
 fun startContinuous(c:Char){repeatJob?.cancel();repeatJob=scope.launch{while(isActive){sendNow(c);delay(50)}}}
 fun stopContinuous(){repeatJob?.cancel();repeatJob=null;send('S')}
 fun disconnect(){repeatJob?.cancel();close();onStateChanged(false,"Desconectado")}
 private fun sendNow(c:Char){try{socket?.outputStream?.write(c.code);socket?.outputStream?.flush()}catch(_:IOException){close()}}
 private fun close(){try{socket?.close()}catch(_:IOException){};socket=null};fun shutdown(){disconnect();scope.cancel()}
}