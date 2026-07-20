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
package org.farcom.ui.main.chat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import org.farcom.R
import org.farcom.core.tools.Log
import org.farcom.databinding.StartChatFragmentBinding
import org.farcom.ui.GenericActivity
import org.farcom.ui.main.chat.viewmodel.StartConversationViewModel
import org.farcom.ui.main.fragment.GenericAddressPickerFragment
import org.farcom.ui.main.model.GroupSetOrEditSubjectDialogModel
import org.farcom.utils.DialogUtils
import org.farcom.utils.Event
import org.farcom.utils.hideKeyboard

@UiThread
class StartConversationFragment : GenericAddressPickerFragment() {
    companion object {
        private const val TAG = "[Start Conversation Fragment]"
    }

    private lateinit var binding: StartChatFragmentBinding

    override lateinit var viewModel: StartConversationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StartChatFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[StartConversationViewModel::class.java]

        postponeEnterTransition()
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        observeToastEvents(viewModel)

        binding.setBackClickListener {
            goBack()
        }

        binding.setAskForGroupConversationSubjectClickListener {
            showGroupConversationSubjectDialog()
        }

        setupRecyclerView(binding.contactsList)

        viewModel.modelsList.observe(
            viewLifecycleOwner
        ) {
            Log.i("$TAG Contacts & suggestions list is ready with [${it.size}] items")
            adapter.submitList(it)

            attachAdapter()

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        viewModel.chatRoomCreatedEvent.observe(viewLifecycleOwner) {
            it.consume { conversationId ->
                Log.i(
                    "$TAG Conversation [$conversationId] has been created, navigating to it"
                )
                sharedViewModel.showConversationEvent.value = Event(conversationId)
                goBack()
            }
        }

        viewModel.defaultAccountChangedEvent.observe(viewLifecycleOwner) {
            it.consume {
                viewModel.updateGroupChatButtonVisibility()
            }
        }
    }

    private fun showGroupConversationSubjectDialog() {
        val model = GroupSetOrEditSubjectDialogModel("", isGroupConversation = true)

        val dialog = DialogUtils.getSetOrEditGroupSubjectDialog(
            requireContext(),
            viewLifecycleOwner,
            model
        )

        model.dismissEvent.observe(viewLifecycleOwner) {
            it.consume {
                Log.i("$TAG Set conversation subject cancelled")
                dialog.dismiss()
            }
        }

        model.confirmEvent.observe(viewLifecycleOwner) {
            it.consume { newSubject ->
                if (newSubject.isNotEmpty()) {
                    Log.i(
                        "$TAG Conversation subject has been set to [$newSubject]"
                    )
                    viewModel.subject.value = newSubject
                    viewModel.createGroupChatRoom()

                    dialog.currentFocus?.hideKeyboard()
                    dialog.dismiss()
                } else {
                    val message = getString(R.string.conversation_invalid_empty_subject_toast)
                    val icon = R.drawable.warning_circle
                    (requireActivity() as GenericActivity).showRedToast(message, icon)
                }
            }
        }

        Log.i("$TAG Showing dialog to set conversation subject")
        dialog.show()
    }
}
