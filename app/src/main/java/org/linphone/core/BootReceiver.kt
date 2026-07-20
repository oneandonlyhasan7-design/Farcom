/*
 * Copyright (c) 2010-2024 Belledonne Communications SARL.
 *
 * This file is part of farcom-android
 * (see https://www.farcom.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.farcom.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.farcom.FarcomApplication.Companion.corePreferences
import org.farcom.core.tools.Log

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "[Boot Receiver]"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val keepAlive = corePreferences.keepServiceAlive
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED, ignoreCase = true)) {
            Log.i(
                "$TAG Device boot completed, keep alive service is ${if (keepAlive) "enabled" else "disabled"}"
            )
        } else if (intent.action.equals(Intent.ACTION_MY_PACKAGE_REPLACED, ignoreCase = true)) {
            Log.i(
                "$TAG App has been updated, keep alive service is ${if (keepAlive) "enabled" else "disabled"}"
            )
        }
        // Starting the keep alive service will be done by CoreContext directly
    }
}
