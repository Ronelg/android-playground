package com.worldturtlemedia.playground.photos.googlephotos.ui.filter

import android.view.View
import androidx.annotation.DrawableRes
import com.worldturtlemedia.playground.common.ktx.string
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.CategoryFilterItemBinding
import com.worldturtlemedia.playground.photos.databinding.CategoryFilterItemBinding.bind
import com.worldturtlemedia.playground.photos.googlephotos.model.filter.CategoryFilter
import com.xwray.groupie.viewbinding.BindableItem

data class CategoryFilterItem(
    val category: CategoryFilter,
    private val isChecked: Boolean
) : BindableItem<CategoryFilterItemBinding>(category.stringRes.toLong()) {

    override fun getLayout(): Int = R.layout.category_filter_item

    override fun initializeViewBinding(view: View): CategoryFilterItemBinding = bind(view)

    override fun bind(viewBinding: CategoryFilterItemBinding, position: Int) {
        with(viewBinding) {
            @DrawableRes val iconRes: Int =
                if (isChecked) R.drawable.ic_check_box_active
                else R.drawable.ic_check_box_outline

            imgChecked.setImageResource(iconRes)

            txtTitle.text = root.context.string(category.stringRes)
        }
    }
}

fun createCategoryFilterItems(selected: List<CategoryFilter> = emptyList()): List<CategoryFilterItem> {
    return CategoryFilter.asList().map { category ->
        CategoryFilterItem(category, selected.contains(category))
    }
}
