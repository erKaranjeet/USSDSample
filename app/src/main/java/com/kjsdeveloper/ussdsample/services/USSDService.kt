package com.kjsdeveloper.ussdsample.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.kjsdeveloper.ussdsample.R


class USSDService : AccessibilityService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("Service::", "Started")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d("USSDService", "onAccessibilityEvent called")

        event?.let {
            Log.d("USSDService", "Event type: ${it.eventType}")
            Log.d("USSDService", "Event text: ${it.text}")

            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                it.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
                it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

                val source: AccessibilityNodeInfo? = it.source
                if (source != null) {
                    val text = getTextFromNode(source)
                    Log.d("USSDService", "Node text: $text")

                    if (text.contains("*#*#12345#*#*")) {
                        performTask(source)
                    }
                } else {
                    Log.d("USSDService", "Source is null")
                }
            }
        }
    }

    private fun getTextFromNode(node: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        if (node.text != null) {
            sb.append(node.text)
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                sb.append(getTextFromNode(child))
            }
        }

        return sb.toString()
    }

    override fun onInterrupt() {
        // Handle service interruption
        Log.d("USSDService", "Service interrupted")
    }

    private fun performTask(source: AccessibilityNodeInfo) {
        // Perform your task here
        Toast.makeText(this, "USSD Code Detected!", Toast.LENGTH_SHORT).show()
        Log.d("USSDService", "USSD Code Detected")

        // Clear the USSD code from the dialer
        clearUSSDCode(source)

        // Intent to open the gallery
        val intent = Intent(Intent.ACTION_VIEW).apply {
            type = "image/*"
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun clearUSSDCode(source: AccessibilityNodeInfo) {
        // Find the text field and clear it
        val dialerTextField = findDialerTextField(source)
        dialerTextField?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, null)
    }

    private fun findDialerTextField(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.className == "android.widget.EditText") {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findDialerTextField(child)
            if (result != null) {
                return result
            }
        }
        return null
    }
}