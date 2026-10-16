package com.example.weather.weather.citypicker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.ItemCityBinding
import com.example.weather.databinding.ItemCitySectionBinding
import com.example.weather.databinding.ItemCityStatusBinding
import com.example.weather.databinding.ItemCurrentLocationBinding

internal interface CityAdapterListener {

    fun onCurrentLocationClick()

    fun onCityClick(cityId: String)

    fun onDeleteCityClick(cityId: String)

    fun onRetryClick(section: CityListSection)
}

internal class CityAdapter(
    private val listener: CityAdapterListener,
) : ListAdapter<CityListItem, RecyclerView.ViewHolder>(CityListItemDiffCallback) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        CityListItem.CurrentLocation -> VIEW_TYPE_CURRENT_LOCATION
        is CityListItem.Section -> VIEW_TYPE_SECTION
        is CityListItem.City -> VIEW_TYPE_CITY
        is CityListItem.Status -> VIEW_TYPE_STATUS
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val viewHolder = when (viewType) {
            VIEW_TYPE_CURRENT_LOCATION -> CurrentLocationViewHolder(
                ItemCurrentLocationBinding.inflate(
                    inflater,
                    parent,
                    false,
                ), listener = listener
            )

            VIEW_TYPE_SECTION -> SectionViewHolder(
                ItemCitySectionBinding.inflate(
                    inflater,
                    parent,
                    false,
                )
            )


            VIEW_TYPE_CITY -> CityViewHolder(
                ItemCityBinding.inflate(
                    inflater,
                    parent,
                    false,
                ), listener = listener
            )


            VIEW_TYPE_STATUS -> StatusViewHolder(
                ItemCityStatusBinding.inflate(
                    inflater,
                    parent,
                    false,
                ), listener = listener
            )

            else -> {
                throw IllegalArgumentException("Unknown view type: $viewType")
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        val item = getItem(position)

        when (holder) {
            is CurrentLocationViewHolder if item === CityListItem.CurrentLocation -> {
                holder.bind()
            }

            is SectionViewHolder if item is CityListItem.Section -> {
                holder.bind(item)
            }

            is CityViewHolder if item is CityListItem.City -> {
                holder.bind(item)
            }

            is StatusViewHolder if item is CityListItem.Status -> {
                holder.bind(item)
            }

            else -> {
                throw IllegalStateException(
                    "Cannot bind ${item.javaClass.simpleName} " +
                            "to ${holder.javaClass.simpleName}",
                )
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any?>
    ) {
        val item = getItem(position)

        if (
            holder is CityViewHolder &&
            item is CityListItem.City &&
            payloads.any { it === CityListPayload.SelectionChanged }
        ) {
            holder.bindSelection(item.isSelected)
        }  else {
            super.onBindViewHolder(holder, position, payloads)
        }

    }

    private companion object {
        const val VIEW_TYPE_CURRENT_LOCATION = 0
        const val VIEW_TYPE_SECTION = 1
        const val VIEW_TYPE_CITY = 2
        const val VIEW_TYPE_STATUS = 3
    }

    private class CityViewHolder(
        private val binding: ItemCityBinding,
        private val listener: CityAdapterListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CityListItem.City) {
            binding.cityNameTextView.text = item.name
            binding.countryTextView.text = item.countryCode
            binding.deleteImageButton.isVisible = item.canDelete
            bindSelection(item.isSelected)

            binding.root.setOnClickListener {
                listener.onCityClick(item.cityId)
            }

            if (item.canDelete) {
                binding.deleteImageButton.setOnClickListener {
                    listener.onDeleteCityClick(item.cityId)
                }
            } else {
                binding.deleteImageButton.setOnClickListener(null)
            }
        }

        fun bindSelection(isSelected: Boolean) {
            binding.selectedImageView.isVisible = isSelected
        }
    }

    private class SectionViewHolder(
        private val binding: ItemCitySectionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CityListItem.Section) {
            binding.sectionTitleTextView.setText(item.titleResId)
        }
    }

    private class CurrentLocationViewHolder(
        private val binding: ItemCurrentLocationBinding,
        private val listener: CityAdapterListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.root.setOnClickListener {
                listener.onCurrentLocationClick()
            }
        }
    }

    private class StatusViewHolder(
        private val binding: ItemCityStatusBinding,
        private val listener: CityAdapterListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CityListItem.Status) {
            when (item) {
                is CityListItem.Status.Loading -> bindLoading()
                is CityListItem.Status.Empty -> bindEmpty(item)
                is CityListItem.Status.Error -> bindError(item)
            }
        }

        private fun bindLoading() {
            binding.progressBar.isVisible = true
            binding.messageTextView.isVisible = false
            binding.retryButton.isVisible = false
            binding.retryButton.setOnClickListener(null)
        }

        private fun bindEmpty(item: CityListItem.Status.Empty) {
            binding.progressBar.isVisible = false
            binding.messageTextView.isVisible = true
            binding.messageTextView.setText(item.messageResId)
            binding.retryButton.isVisible = false
            binding.retryButton.setOnClickListener(null)
        }

        private fun bindError(item: CityListItem.Status.Error) {
            binding.progressBar.isVisible = false
            binding.messageTextView.isVisible = true
            binding.messageTextView.setText(item.messageResId)
            binding.retryButton.isVisible = true
            binding.retryButton.setOnClickListener {
                listener.onRetryClick(item.section)
            }
        }
    }
}
