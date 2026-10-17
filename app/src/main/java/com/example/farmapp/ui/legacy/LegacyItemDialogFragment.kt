package com.example.farmapp.ui.legacy

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.farmapp.FarmAppApplication
import com.example.farmapp.R
import com.example.farmapp.data.room.RoomDemoForm
import com.example.farmapp.data.room.RoomDemoItem
import com.example.farmapp.databinding.DialogLegacyItemBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Destination de tipo <dialog> en el navigation graph. Recibe el ID del artículo. */
class LegacyItemDialogFragment : DialogFragment() {
    companion object { const val ARG_ITEM_ID = "itemId" }
    private var _binding: DialogLegacyItemBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val itemId = requireArguments().getLong(ARG_ITEM_ID, 0L)
        val mode = legacyEntryModeFor(itemId)
        _binding = DialogLegacyItemBinding.inflate(layoutInflater)
        val repo = (requireActivity().application as FarmAppApplication)
            .container.roomDemoRepository

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (mode == LegacyEntryMode.NEW) R.string.legacy_dialog_add
                      else R.string.legacy_dialog_edit)
            .setView(binding.root)
            .setNegativeButton(R.string.legacy_cancel, null)
            .setPositiveButton(R.string.legacy_save, null)
            .create()

        dialog.setOnShowListener {
            val positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            if (mode == LegacyEntryMode.EDIT) {
                positive.isEnabled = false
                lifecycleScope.launch {
                    try {
                        val original = repo.observeItem(itemId).first()
                        if (original == null) {
                            binding.legacyFormError.setText(R.string.legacy_missing_item)
                        } else {
                            binding.legacyItemName.setText(original.name)
                            binding.legacyItemPrice.setText(
                                java.lang.String.format(java.util.Locale.US, "%.2f", original.priceCents / 100.0)
                            )
                            binding.legacyItemStock.setText(original.stock.toString())
                            positive.isEnabled = true
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        binding.legacyFormError.setText(R.string.legacy_save_error)
                    }
                }
            }
            positive.setOnClickListener {
                val parsed = RoomDemoForm.validate(
                    binding.legacyItemName.text.toString(),
                    binding.legacyItemPrice.text.toString(),
                    binding.legacyItemStock.text.toString()
                )
                if (parsed == null) {
                    binding.legacyFormError.setText(R.string.legacy_validation_error)
                    return@setOnClickListener
                }
                positive.isEnabled = false
                lifecycleScope.launch {
                    try {
                        val success = if (mode == LegacyEntryMode.NEW) {
                            repo.add(RoomDemoItem(name = parsed.name,
                                priceCents = parsed.priceCents, stock = parsed.stock))
                            true
                        } else {
                            repo.updateRecord(itemId, parsed.name, parsed.priceCents, parsed.stock)
                        }
                        if (success) {
                            dismiss()
                        } else {
                            binding.legacyFormError.setText(R.string.legacy_missing_item)
                            positive.isEnabled = true
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        binding.legacyFormError.setText(R.string.legacy_save_error)
                        positive.isEnabled = true
                    }
                }
            }
        }
        return dialog
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
