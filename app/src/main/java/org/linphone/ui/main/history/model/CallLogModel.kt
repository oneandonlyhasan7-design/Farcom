/*
 * Copyright (c) 2010-2023 Belledonne Communications SARL.
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
package org.farcom.ui.main.history.model

import androidx.annotation.IntegerRes
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import org.farcom.FarcomApplication.Companion.coreContext
import org.farcom.FarcomApplication.Companion.corePreferences
import org.farcom.R
import org.farcom.core.CallLog
import org.farcom.core.tools.Log
import org.farcom.ui.main.contacts.model.ContactAvatarModel
import org.farcom.utils.AppUtils
import org.farcom.utils.FarcomUtils
import org.farcom.utils.TimestampUtils

class CallLogModel
    @WorkerThread
    constructor(private val callLog: CallLog) {
    companion object {
        private const val TAG = "[Call Log Model]"
    }

    val id = callLog.callId ?: callLog.refKey

    val timestamp = callLog.startDate

    val address = callLog.remoteAddress

    val sipUri = address.asStringUriOnly()

    val displayedAddress: String

    val avatarModel: ContactAvatarModel

    val wasConference: Boolean

    @IntegerRes
    val iconResId: Int

    val dateTime: String

    val friendRefKey: String?

    var friendExists: Boolean = false

    init {
        val date = if (TimestampUtils.isToday(timestamp)) {
            AppUtils.getString(R.string.today)
        } else if (TimestampUtils.isYesterday(timestamp)) {
            AppUtils.getString(R.string.yesterday)
        } else {
            TimestampUtils.toString(timestamp, onlyDate = true, shortDate = true, hideYear = true)
        }
        val time = TimestampUtils.timeToString(timestamp)
        dateTime = "$date | $time"

        wasConference = callLog.wasConference()
        if (wasConference) {
            val conferenceInfo = callLog.conferenceInfo
            if (conferenceInfo != null) {
                avatarModel = coreContext.contactsManager.getContactAvatarModelForConferenceInfo(
                    conferenceInfo
                )
            } else {
                Log.w("$TAG Failed to retrieve conference info attached to call log!")
                val fakeFriend = coreContext.core.createFriend()
                fakeFriend.address = address
                fakeFriend.name = FarcomUtils.getDisplayName(address)
                avatarModel = ContactAvatarModel(fakeFriend)
                avatarModel.forceConferenceIcon.postValue(true)
            }

            friendRefKey = null
            friendExists = false
        } else {
            avatarModel = coreContext.contactsManager.getContactAvatarModelForAddress(address)
            val friend = avatarModel.friend
            friendRefKey = friend.refKey
            friendExists = coreContext.contactsManager.isContactAvailable(friend)
        }
        displayedAddress = if (corePreferences.onlyDisplaySipUriUsername) {
            address.username ?: ""
        } else {
            sipUri
        }

        iconResId = FarcomUtils.getCallIconResId(callLog.status, callLog.dir)
    }

    @UiThread
    fun delete() {
        coreContext.postOnCoreThread { core ->
            core.removeCallLog(callLog)
        }
    }
}
