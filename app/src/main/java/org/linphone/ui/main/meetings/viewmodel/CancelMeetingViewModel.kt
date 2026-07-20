/*
 * Copyright (c) 2010-2025 Belledonne Communications SARL.
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
package org.farcom.ui.main.meetings.viewmodel

import androidx.annotation.UiThread
import androidx.lifecycle.MutableLiveData
import org.farcom.FarcomApplication.Companion.coreContext
import org.farcom.FarcomApplication.Companion.corePreferences
import org.farcom.core.Address
import org.farcom.core.ConferenceInfo
import org.farcom.core.ConferenceScheduler
import org.farcom.core.ConferenceSchedulerListenerStub
import org.farcom.core.tools.Log
import org.farcom.ui.GenericViewModel
import org.farcom.utils.Event
import org.farcom.utils.FarcomUtils

open class CancelMeetingViewModel
    @UiThread
    constructor() : GenericViewModel() {
    companion object {
        private const val TAG = "[Cancel Meeting ViewModel]"
    }

    val operationInProgress = MutableLiveData<Boolean>()

    val conferenceCancelledEvent: MutableLiveData<Event<Boolean>> by lazy {
        MutableLiveData()
    }

    private var sendNotificationForCancelledConference: Boolean = false

    private val conferenceSchedulerListener = object : ConferenceSchedulerListenerStub() {
        override fun onStateChanged(
            conferenceScheduler: ConferenceScheduler,
            state: ConferenceScheduler.State?
        ) {
            Log.i("$TAG Conference scheduler state is $state")
            if (state == ConferenceScheduler.State.Ready) {
                Log.i(
                    "$TAG Conference ${conferenceScheduler.info?.subject}  has been cancelled"
                )
                if (sendNotificationForCancelledConference) {
                    Log.i("$TAG Sending cancelled meeting ICS to participants")
                    val params = FarcomUtils.getChatRoomParamsToCancelMeeting()
                    if (params != null && !corePreferences.disableChat) {
                        conferenceScheduler.sendInvitations(params)
                    } else {
                        Log.e("$TAG Failed to get chat room params to send cancelled meeting ICS!")
                        operationInProgress.postValue(false)
                    }
                } else {
                    operationInProgress.postValue(false)
                    conferenceCancelledEvent.postValue(Event(true))
                }
            } else if (state == ConferenceScheduler.State.Error) {
                operationInProgress.postValue(false)
                // TODO FIXME: show error to user
            }
        }

        override fun onInvitationsSent(
            conferenceScheduler: ConferenceScheduler,
            failedInvitations: Array<out Address>?
        ) {
            if (failedInvitations?.isNotEmpty() == true) {
                // TODO FIXME: show error to user
                for (address in failedInvitations) {
                    Log.e(
                        "$TAG Conference cancelled ICS wasn't sent to participant ${address.asStringUriOnly()}"
                    )
                }
            } else {
                Log.i(
                    "$TAG Conference cancelled ICS successfully sent to all participants"
                )
            }
            conferenceScheduler.removeListener(this)

            operationInProgress.postValue(false)
            conferenceCancelledEvent.postValue(Event(true))
        }
    }

    init {
        operationInProgress.value = false
    }

    @UiThread
    fun cancelMeeting(conferenceInfo: ConferenceInfo, sendNotification: Boolean) {
        coreContext.postOnCoreThread { core ->
            Log.w("$TAG Cancelling conference info [${conferenceInfo.uri?.asStringUriOnly()}]")
            sendNotificationForCancelledConference = sendNotification
            operationInProgress.postValue(true)

            val conferenceScheduler = FarcomUtils.createConferenceScheduler(
                FarcomUtils.getDefaultAccount()
            )
            conferenceScheduler.addListener(conferenceSchedulerListener)
            conferenceScheduler.cancelConference(conferenceInfo)
        }
    }
}
