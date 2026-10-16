package com.example.weather.weather.citypicker

import androidx.recyclerview.widget.DiffUtil

internal object CityListItemDiffCallback : DiffUtil.ItemCallback<CityListItem>() {

    override fun areItemsTheSame(
        oldItem: CityListItem,
        newItem: CityListItem,
    ): Boolean = oldItem.itemKey == newItem.itemKey

    override fun areContentsTheSame(
        oldItem: CityListItem,
        newItem: CityListItem,
    ): Boolean = oldItem == newItem

    override fun getChangePayload(
        oldItem: CityListItem,
        newItem: CityListItem,
    ): Any? {
        if (oldItem !is CityListItem.City || newItem !is CityListItem.City) {
            return null
        }

        val onlySelectionChanged =
            oldItem.isSelected != newItem.isSelected &&
                oldItem.copy(isSelected = newItem.isSelected) == newItem

        return if (onlySelectionChanged) {
            CityListPayload.SelectionChanged
        } else {
            null
        }
    }
}
