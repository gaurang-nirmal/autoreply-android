package com.psspl.autoreply.utils

object AppConstants {
    const val APP_NAME = "AutoReply"
    const val MAX_FREE_APPS = 2

    /**
     * Base URL for the AutoReply NestJS backend API.
     * Must end with a trailing slash.
     *
     * ── Local development ───────────────────────────────────────────────────
     * Android Emulator → use 10.0.2.2 (maps to the host machine's localhost):
     *     "http://10.0.2.2:3000/"
     *
     * Real device (USB / same Wi-Fi) → use your PC's local IP address:
     *     "http://192.168.x.x:3000/"
     *   Find your IP: Windows → ipconfig → IPv4 Address under your Wi-Fi adapter
     *
     * ── Production ──────────────────────────────────────────────────────────
     *     "https://api.yourapp.com/"
     *
     * ⚠️ http:// (cleartext) is only allowed in debug builds via the
     *    network security config. Switch to https:// for production.
     */
    const val BASE_URL = "http://192.168.10.58:3000/"   // real device → PC on local Wi-Fi
}
