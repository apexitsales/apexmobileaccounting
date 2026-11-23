package com.apexit.accounting

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context

object ThermalPrinter {
    fun hasBluetooth(context: Context): Boolean =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter?.isEnabled == true

    fun escposText(title: String, number: String, customer: String, total: Double): ByteArray {
        val esc = 0x1B.toByte()
        val gs = 0x1D.toByte()
        return buildList {
            add(esc); add(0x40.toByte())
            add(esc); add(0x61.toByte()); add(0x01.toByte())
            addAll(title.toByteArray().toList()); add(0x0A.toByte())
            addAll(number.toByteArray().toList()); add(0x0A.toByte())
            add(esc); add(0x61.toByte()); add(0x00.toByte())
            addAll("Customer: $customer\n".toByteArray().toList())
            addAll("TOTAL: %.2f\n".format(total).toByteArray().toList())
            add(0x0A.toByte()); add(0x0A.toByte()); add(0x0A.toByte())
            add(gs); add(0x56.toByte()); add(0x00.toByte())
        }.toByteArray()
    }
}
