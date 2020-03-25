package com.worldturtlemedia.playground.photos.googlephotos.model.filter

import androidx.annotation.StringRes
import com.google.photos.library.v1.proto.ContentCategory
import com.google.photos.library.v1.proto.ContentFilter
import com.google.photos.library.v1.proto.Filters
import com.worldturtlemedia.playground.photos.R

/**
 * You can only send a MAXIMUM of 10 categories for each request, so for simplicity I have only
 * included 10 categories that I think would be most popular.
 *
 * Full list:
 * https://developers.google.com/photos/library/guides/apply-filters#including-categories
 * ANIMALS 	FASHION 	LANDMARKS 	RECEIPTS 	WEDDINGS
 * ARTS 	FLOWERS 	LANDSCAPES 	SCREENSHOTS 	WHITEBOARDS
 * BIRTHDAYS 	FOOD 	NIGHT 	SELFIES
 * CITYSCAPES 	GARDENS 	PEOPLE 	SPORT
 * CRAFTS 	HOLIDAYS 	PERFORMANCES 	TRAVEL
 * DOCUMENTS 	HOUSES 	PETS 	UTILITY
 */
sealed class CategoryFilter(
    private val value: ContentCategory
) : Filter {

    object Animals : CategoryFilter(ContentCategory.ANIMALS)
    object Birthdays : CategoryFilter(ContentCategory.BIRTHDAYS)
    object CityScapes : CategoryFilter(ContentCategory.CITYSCAPES)
    object Fashion : CategoryFilter(ContentCategory.FASHION)
    object Food : CategoryFilter(ContentCategory.FOOD)
    object Landmarks : CategoryFilter(ContentCategory.LANDMARKS)
    object People : CategoryFilter(ContentCategory.PEOPLE)
    object Sport : CategoryFilter(ContentCategory.SPORT)
    object Travel : CategoryFilter(ContentCategory.TRAVEL)
    object Weddings : CategoryFilter(ContentCategory.WEDDINGS)

    override fun build(builder: Filters.Builder): Filters.Builder {
        val contentFilter = ContentFilter.newBuilder()
            .addIncludedContentCategories(this.value)
            .build()

        return builder.mergeContentFilter(contentFilter)
    }

    @get:StringRes
    val stringRes: Int
        get() = when (this) {
            Animals -> R.string.category_animals
            Birthdays -> R.string.category_birthdays
            CityScapes -> R.string.category_cityScapes
            Fashion -> R.string.category_fashion
            Food -> R.string.category_food
            Landmarks -> R.string.category_landmarks
            People -> R.string.category_people
            Sport -> R.string.category_sport
            Travel -> R.string.category_travel
            Weddings -> R.string.category_weddings
        }

    companion object {
        fun asList() = listOf(
            Animals,
            Birthdays,
            CityScapes,
            Fashion,
            Food,
            Landmarks,
            People,
            Sport,
            Travel,
            Weddings
        )
    }
}