package com.example.farmapp.ui.legacy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.farmapp.data.room.RoomDemoItem
import com.example.farmapp.databinding.RowLegacyItemBinding
import com.example.farmapp.ui.theme.FarmAppTheme

/**
 * C31 — Interoperabilidad Views -> Compose.
 * RecyclerView/ListAdapter/DiffUtil siguen siendo Views; el contenido de cada fila
 * se dibuja con ComposeView y el componente LegacyInventoryComposeRow().
 */
class LegacyInventoryAdapter(
    private val onItemSelected: (Long) -> Unit
) : ListAdapter<RoomDemoItem, LegacyInventoryAdapter.ItemHolder>(ItemDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = RowLegacyItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemHolder(binding, onItemSelected)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ItemHolder) {
        holder.clear()
        super.onViewRecycled(holder)
    }

    class ItemHolder(
        val binding: RowLegacyItemBinding,
        onItemSelected: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // El estado sigue la fila que está actualmente asociada al ViewHolder.
        // Cuando DiffUtil cambia un artículo, Compose lo recompone automáticamente.
        private var currentItem by mutableStateOf<RoomDemoItem?>(null)

        init {
            // Estrategia pensada específicamente para contenedores de reciclaje/pooling.
            binding.legacyItemCompose.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )
            // setContent una vez por holder, no en cada onBindViewHolder.
            binding.legacyItemCompose.setContent {
                FarmAppTheme {
                    currentItem?.let { item ->
                        LegacyInventoryComposeRow(
                            item = item,
                            onClick = { onItemSelected(item.id) }
                        )
                    }
                }
            }
        }

        fun bind(item: RoomDemoItem) { currentItem = item }
        fun clear() { currentItem = null }
    }

    private object ItemDiff : DiffUtil.ItemCallback<RoomDemoItem>() {
        override fun areItemsTheSame(oldItem: RoomDemoItem, newItem: RoomDemoItem): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: RoomDemoItem, newItem: RoomDemoItem): Boolean =
            oldItem == newItem
    }
}
