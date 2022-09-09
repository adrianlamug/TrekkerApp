//package com.example.myapplication.other
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import android.widget.TextView
//import androidx.annotation.RequiresApi
//import com.example.myapplication.ui.fragments.MyService
//
//
//
//class AlarmReceiver : BroadcastReceiver() {
//    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//    override fun onReceive(context: Context, intent: Intent) {
//
//
//        when (intent.action) {
//            /*Intent.ACTION_DATE_CHANGED -> {
//            }*/
//            Intent.ACTION_BOOT_COMPLETED -> {
//                val serviceIntent = Intent(context, MyService::class.java)
//                context.startService(serviceIntent)
//            }
//        }
//    }
//}