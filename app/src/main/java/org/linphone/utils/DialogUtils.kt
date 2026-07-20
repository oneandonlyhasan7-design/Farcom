/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
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
package org.farcom.utils

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.UiThread
import androidx.appcompat.widget.AppCompatEditText
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import org.farcom.R
import org.farcom.databinding.DialogAssistantAcceptConditionsAndPolicyBinding
import org.farcom.databinding.DialogAssistantCreateAccountConfirmPhoneNumberBinding
import org.farcom.databinding.DialogCallConfirmTransferBinding
import org.farcom.databinding.DialogCancelContactChangesBinding
import org.farcom.databinding.DialogCancelMeetingBinding
import org.farcom.databinding.DialogConfirmTurningOnVfsBinding
import org.farcom.databinding.DialogContactConfirmTrustCallBinding
import org.farcom.databinding.DialogContactTrustProcessBinding
import org.farcom.databinding.DialogDeleteContactBinding
import org.farcom.databinding.DialogKickFromConferenceBinding
import org.farcom.databinding.DialogManageAccountInternationalPrefixHelpBinding
import org.farcom.databinding.DialogMergeCallsIntoConferenceBinding
import org.farcom.databinding.DialogOpenExportFileBinding
import org.farcom.databinding.DialogOpenPlainTextBinding
import org.farcom.databinding.DialogPickNumberOrAddressBinding
import org.farcom.databinding.DialogRemoveAccountBinding
import org.farcom.databinding.DialogRemoveAllCallLogsBinding
import org.farcom.databinding.DialogRemoveCallLogsBinding
import org.farcom.databinding.DialogRemoveConversationHistoryBinding
import org.farcom.databinding.DialogSetOrEditGroupSubjectBindingImpl
import org.farcom.databinding.DialogStartGroupCallFromConversationBinding
import org.farcom.databinding.DialogUpdateAccountPasswordAfterRegisterFailureBinding
import org.farcom.databinding.DialogUpdateAccountPasswordBinding
import org.farcom.databinding.DialogUpdateAvailableBinding
import org.farcom.databinding.DialogZrtpSasValidationBinding
import org.farcom.databinding.DialogZrtpSecurityAlertBinding
import org.farcom.ui.assistant.model.AcceptConditionsAndPolicyDialogModel
import org.farcom.ui.call.model.ZrtpAlertDialogModel
import org.farcom.ui.call.model.ZrtpSasConfirmationDialogModel
import org.farcom.ui.main.contacts.model.ContactTrustDialogModel
import org.farcom.ui.main.contacts.model.NumberOrAddressPickerDialogModel
import org.farcom.ui.main.model.GroupSetOrEditSubjectDialogModel
import androidx.core.graphics.drawable.toDrawable
import org.farcom.databinding.DialogAssistantCreateAccountPhoneNumberValidationNotAvailableBinding
import org.farcom.databinding.DialogDeleteMeetingBinding
import org.farcom.databinding.DialogLeaveGroupConversationBinding
import org.farcom.databinding.DialogManageAccountOutboundProxyHelpBinding
import org.farcom.databinding.DialogRemoveCallLogBinding
import org.farcom.databinding.DialogRemoveConversationBinding
import org.farcom.databinding.DialogRemoveParticipantFromGroupBinding

class DialogUtils {
    companion object {
        @UiThread
        fun getAcceptConditionsAndPrivacyDialog(
            context: Context,
            viewModel: AcceptConditionsAndPolicyDialogModel
        ): Dialog {
            val binding: DialogAssistantAcceptConditionsAndPolicyBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_assistant_accept_conditions_and_policy,
                null,
                false
            )
            binding.viewModel = viewModel
            binding.message.movementMethod = LinkMovementMethod.getInstance()

            return getDialog(context, binding)
        }

        @UiThread
        fun getAccountCreationPhoneNumberConfirmationDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogAssistantCreateAccountConfirmPhoneNumberBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_assistant_create_account_confirm_phone_number,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getAccountCreationPhoneNumberValidationNotAvailableDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogAssistantCreateAccountPhoneNumberValidationNotAvailableBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_assistant_create_account_phone_number_validation_not_available,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getAccountInternationalPrefixHelpDialog(context: Context): Dialog {
            val binding: DialogManageAccountInternationalPrefixHelpBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_manage_account_international_prefix_help,
                null,
                false
            )
            val dialog = getDialog(context, binding)

            binding.setDismissClickListener {
                dialog.dismiss()
            }

            return dialog
        }

        @UiThread
        fun getAccountOutboundProxyHelpDialog(context: Context): Dialog {
            val binding: DialogManageAccountOutboundProxyHelpBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_manage_account_outbound_proxy_help,
                null,
                false
            )
            val dialog = getDialog(context, binding)

            binding.setDismissClickListener {
                dialog.dismiss()
            }

            return dialog
        }

        @UiThread
        fun getConfirmAccountRemovalDialog(
            context: Context,
            viewModel: ConfirmationDialogModel,
            showDeleteAccountLink: Boolean
        ): Dialog {
            val binding: DialogRemoveAccountBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_remove_account,
                null,
                false
            )
            binding.viewModel = viewModel
            binding.message.visibility = if (showDeleteAccountLink) View.VISIBLE else View.GONE

            return getDialog(context, binding)
        }

        @UiThread
        fun getConfirmTurningOnVfsDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogConfirmTurningOnVfsBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_confirm_turning_on_vfs,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getNumberOrAddressPickerDialog(
            context: Context,
            viewModel: NumberOrAddressPickerDialogModel
        ): Dialog {
            val binding: DialogPickNumberOrAddressBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_pick_number_or_address,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getContactTrustCallConfirmationDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogContactConfirmTrustCallBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_contact_confirm_trust_call,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getContactTrustProcessExplanationDialog(
            context: Context,
            viewModel: ContactTrustDialogModel
        ): Dialog {
            val binding: DialogContactTrustProcessBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_contact_trust_process,
                null,
                false
            )
            binding.viewModel = viewModel

            val dialog = getDialog(context, binding)

            binding.setDismissClickListener {
                dialog.dismiss()
            }

            return dialog
        }

        @UiThread
        fun getDeleteContactConfirmationDialog(
            context: Context,
            viewModel: ConfirmationDialogModel,
            contactName: String
        ): Dialog {
            val binding: DialogDeleteContactBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_delete_contact,
                null,
                false
            )
            binding.viewModel = viewModel
            binding.title.text = context.getString(
                R.string.contact_dialog_delete_title,
                contactName
            )

            return getDialog(context, binding)
        }

        @UiThread
        fun getRemoveCallLogsConfirmationDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogRemoveCallLogsBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_remove_call_logs,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getRemoveCallLogConfirmationDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogRemoveCallLogBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_remove_call_log,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getRemoveAllCallLogsConfirmationDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogRemoveAllCallLogsBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_remove_all_call_logs,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getCancelContactChangesConfirmationDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogCancelContactChangesBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_cancel_contact_changes,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getSetOrEditGroupSubjectDialog(
            context: Context,
            lifecycleOwner: LifecycleOwner,
            viewModel: GroupSetOrEditSubjectDialogModel
        ): Dialog {
            val binding: DialogSetOrEditGroupSubjectBindingImpl = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_set_or_edit_group_subject,
                null,
                false
            )
            binding.lifecycleOwner = lifecycleOwner
            binding.viewModel = viewModel
            // For some reason, binding.subject triggers an error on Android Studio...
            binding.root.findViewById<AppCompatEditText>(R.id.subject)?.requestFocus()

            return getDialog(context, binding)
        }

        @UiThread
        fun getConfirmGroupCallDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogStartGroupCallFromConversationBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_start_group_call_from_conversation,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getConfirmRemoveParticipantDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogRemoveParticipantFromGroupBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_remove_participant_from_group,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getLeaveConversationConfirmationDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogLeaveGroupConversationBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_leave_group_conversation,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getDeleteConversationConfirmationDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogRemoveConversationBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_remove_conversation,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getDeleteConversationHistoryConfirmationDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogRemoveConversationHistoryBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_remove_conversation_history,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getOpenOrExportFileDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogOpenExportFileBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_open_export_file,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getOpenAsPlainTextDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogOpenPlainTextBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_open_plain_text,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getUpdateAvailableDialog(
            context: Context,
            viewModel: ConfirmationDialogModel,
            message: String
        ): Dialog {
            val binding: DialogUpdateAvailableBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_update_available,
                null,
                false
            )
            binding.viewModel = viewModel
            binding.message.text = message

            return getDialog(context, binding)
        }

        @UiThread
        fun getAuthRequestedDialog(
            context: Context,
            viewModel: PasswordDialogModel
        ): Dialog {
            val binding: DialogUpdateAccountPasswordAfterRegisterFailureBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_update_account_password_after_register_failure,
                null,
                false
            )
            binding.viewModel = viewModel
            binding.lifecycleOwner = context as LifecycleOwner

            return getDialog(context, binding)
        }

        @UiThread
        fun getUpdatePasswordDialog(
            context: Context,
            viewModel: PasswordDialogModel
        ): Dialog {
            val binding: DialogUpdateAccountPasswordBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_update_account_password,
                null,
                false
            )
            binding.viewModel = viewModel
            binding.lifecycleOwner = context as LifecycleOwner

            return getDialog(context, binding)
        }

        @UiThread
        fun getZrtpSasConfirmationDialog(
            context: Context,
            viewModel: ZrtpSasConfirmationDialogModel
        ): Dialog {
            val binding: DialogZrtpSasValidationBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_zrtp_sas_validation,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getZrtpAlertDialog(
            context: Context,
            viewModel: ZrtpAlertDialogModel
        ): Dialog {
            val binding: DialogZrtpSecurityAlertBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_zrtp_security_alert,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getConfirmMergeCallsDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogMergeCallsIntoConferenceBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_merge_calls_into_conference,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getConfirmCallTransferCallDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogCallConfirmTransferBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_call_confirm_transfer,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getKickConferenceParticipantConfirmationDialog(
            context: Context,
            viewModel: ConfirmationDialogModel,
            displayName: String
        ): Dialog {
            val binding: DialogKickFromConferenceBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_kick_from_conference,
                null,
                false
            )
            binding.viewModel = viewModel
            binding.title.text = context.getString(
                R.string.conference_confirm_removing_participant_dialog_title,
                displayName
            )

            return getDialog(context, binding)
        }

        @UiThread
        fun getCancelMeetingDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogCancelMeetingBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_cancel_meeting,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        fun getDeleteMeetingDialog(
            context: Context,
            viewModel: ConfirmationDialogModel
        ): Dialog {
            val binding: DialogDeleteMeetingBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_delete_meeting,
                null,
                false
            )
            binding.viewModel = viewModel

            return getDialog(context, binding)
        }

        @UiThread
        private fun getDialog(context: Context, binding: ViewDataBinding): Dialog {
            val dialog = Dialog(context, R.style.Theme_FarcomDialog)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(binding.root)

            dialog.window?.apply {
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

                val d: Drawable = context.getColor(R.color.bc_black).toDrawable()
                d.alpha = 153 // 60% opacity
                setBackgroundDrawable(d)
            }

            return dialog
        }
    }
}
