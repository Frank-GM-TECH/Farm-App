package com.example.farmapp.ui.legacy

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.farmapp.FarmAppApplication
import com.example.farmapp.R
import com.example.farmapp.databinding.FragmentLegacyTrackerBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** C31: Fragment, RecyclerView y FAB siguen en Views; cada fila usa ComposeView. */
class LegacyTrackerFragment : Fragment(R.layout.fragment_legacy_tracker) {
    private var _binding: FragmentLegacyTrackerBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLegacyTrackerBinding.bind(view)
        val repository = (requireActivity().application as FarmAppApplication)
            .container.roomDemoRepository
        val adapter = LegacyInventoryAdapter { itemId ->
            findNavController().navigate(
                R.id.action_legacyTracker_to_legacyItemDialog,
                bundleOf(LegacyItemDialogFragment.ARG_ITEM_ID to itemId)
            )
        }
        binding.legacyRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.legacyRecycler.adapter = adapter
        binding.legacyFab.setOnClickListener {
            findNavController().navigate(
                R.id.action_legacyTracker_to_legacyItemDialog,
                bundleOf(LegacyItemDialogFragment.ARG_ITEM_ID to 0L)
            )
        }
        binding.legacyClose.setOnClickListener { requireActivity().finish() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.items.collectLatest { items ->
                    adapter.submitList(items)
                    binding.legacyEmpty.visibility =
                        if (items.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.legacyRecycler.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
