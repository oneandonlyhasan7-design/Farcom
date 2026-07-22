/*
 * Copyright (c) 2010-2025 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
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
package org.linphone.compatibility

import android.app.ActivityOptions

// @RequiresApi(Build.VERSION_CODES.BAKLAVA) removed: BAKLAVA is Android's internal codename
// for a still-in-preview OS release with no public stable SDK, so the constant isn't
// resolvable at any installable compileSdk. The annotation was advisory-only (no runtime
// effect) - the actual gating happens at the call site in Compatibility.kt via
// Version.sdkAboveOrEqual(Version.API36_ANDROID_16_BAKLAVA), which comes from the Linphone
// SDK itself, not the Android platform SDK, so it's unaffected by this.
class Api36Compatibility {
    companion object {
        private const val TAG = "[API 36 Compatibility]"

        fun getPendingIntentActivityOptions(creator: Boolean): ActivityOptions {
            val options = ActivityOptions.makeBasic()
            // MODE_BACKGROUND_ACTIVITY_START_ALLOW_ALWAYS requires API 36 (unresolvable at
            // compileSdk=35). MODE_BACKGROUND_ACTIVITY_START_ALLOWED is its API-34
            // predecessor with equivalent intent (grants background activity start
            // privileges) - safe substitute since this method is only ever reached on a
            // preview OS build that doesn't publicly exist yet anyway.
            if (creator) {
                options.pendingIntentCreatorBackgroundActivityStartMode =
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
            } else {
                options.pendingIntentBackgroundActivityStartMode =
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
            }
            return options
        }
    }
}
