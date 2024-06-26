package com.wallet.modules.market.topnftcollections

import com.wallet.core.managers.MarketKitWrapper
import com.wallet.modules.market.SortingField
import com.wallet.modules.market.TimeDuration
import com.wallet.modules.market.sortedByDescendingNullLast
import com.wallet.modules.market.sortedByNullLast
import com.wallet.modules.nft.NftCollectionItem
import com.wallet.modules.nft.nftCollectionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TopNftCollectionsRepository(
    private val marketKit: MarketKitWrapper
) {
    private var itemsCache: List<NftCollectionItem>? = null


    suspend fun get(
        sortingField: SortingField,
        timeDuration: TimeDuration,
        forceRefresh: Boolean,
        limit: Int? = null,
    ) = withContext(Dispatchers.IO) {
        val currentCache = itemsCache

        val items = if (forceRefresh || currentCache == null) {
            marketKit.nftCollections().map { it.nftCollectionItem }
        } else {
            currentCache
        }

        itemsCache = items

         items.sort(sortingField, timeDuration).let { sortedList ->
            limit?.let { sortedList.take(it) } ?: sortedList
        }
    }

    private fun List<NftCollectionItem>.sort(sortingField: SortingField, timeDuration: TimeDuration) =
        when (sortingField) {
            SortingField.HighestCap,
            SortingField.LowestCap -> this
            SortingField.HighestVolume -> sortedByDescendingNullLast { it.volume(timeDuration)?.value }
            SortingField.LowestVolume -> sortedByNullLast { it.volume(timeDuration)?.value }
            SortingField.TopGainers -> sortedByDescendingNullLast { it.volumeDiff(timeDuration) }
            SortingField.TopLosers -> sortedByNullLast { it.volumeDiff(timeDuration) }
        }

    private fun NftCollectionItem.volume(timeDuration: TimeDuration) =
        when (timeDuration) {
            TimeDuration.OneDay -> oneDayVolume
            TimeDuration.SevenDay -> sevenDayVolume
            TimeDuration.ThirtyDay -> thirtyDayVolume
            TimeDuration.ThreeMonths -> null
        }

    private fun NftCollectionItem.volumeDiff(timeDuration: TimeDuration) =
        when (timeDuration) {
            TimeDuration.OneDay -> oneDayVolumeDiff
            TimeDuration.SevenDay -> sevenDayVolumeDiff
            TimeDuration.ThirtyDay -> thirtyDayVolumeDiff
            TimeDuration.ThreeMonths -> null
        }

}
