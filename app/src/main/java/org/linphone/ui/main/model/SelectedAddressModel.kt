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
package org.farcom.ui.main.model

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import org.farcom.core.Address
import org.farcom.ui.main.contacts.model.ContactAvatarModel

class SelectedAddressModel
    @WorkerThread
    constructor(
    val address: Address,
    val avatarModel: ContactAvatarModel?,
    private val onRemovedFromSelection: ((model: SelectedAddressModel) -> Unit)? = null
) {
    @UiThread
    fun removeFromSelection() {
        onRemovedFromSelection?.invoke(this)
    }
}
