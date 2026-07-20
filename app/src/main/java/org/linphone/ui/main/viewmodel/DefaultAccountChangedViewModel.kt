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
package org.farcom.ui.main.viewmodel

import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import org.farcom.FarcomApplication.Companion.coreContext
import org.farcom.core.Account
import org.farcom.core.Core
import org.farcom.core.CoreListenerStub
import org.farcom.core.GlobalState
import org.farcom.core.tools.Log
import org.farcom.ui.GenericViewModel
import org.farcom.utils.Event

open class DefaultAccountChangedViewModel : GenericViewModel() {
    companion object {
        private const val TAG = "[Default Account Changed ViewModel]"
    }

    val defaultAccountChangedEvent: MutableLiveData<Event<Boolean>> by lazy {
        MutableLiveData()
    }

    private val coreListener = object : CoreListenerStub() {
        @WorkerThread
        override fun onDefaultAccountChanged(core: Core, account: Account?) {
            defaultAccountChangedEvent.postValue(Event(true))
        }

        @WorkerThread
        override fun onGlobalStateChanged(core: Core, state: GlobalState?, message: String) {
            if (core.globalState == GlobalState.On) {
                Log.i("$TAG Global state is [${core.globalState}], reload default account")
                defaultAccountChangedEvent.postValue(Event(true))
            }
        }
    }

    init {
        coreContext.postOnCoreThread { core ->
            core.addListener(coreListener)
        }
    }

    override fun onCleared() {
        coreContext.postOnCoreThread { core ->
            core.removeListener(coreListener)
        }

        super.onCleared()
    }
}
