package com.timothy.searchemulator.model

import android.content.Context
import com.timothy.searchemulator.R
import com.timothy.searchemulator.ui.emulator.algo.SearchAlgo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject

class Description(
    val bigTitle: String,
    val descriptionSets: List<DescriptionUnit>
)

data class DescriptionUnit(
    val title: String?,
    val descriptions: List<String>
)

private const val FAKE_LOADING_TIME = 800L

class AlgoDescriptionRepository @Inject constructor(
    @ApplicationContext val applicationContext: Context
) {
    suspend fun getDescriptions(algo: SearchAlgo): Description {
        delay(FAKE_LOADING_TIME)

        return when (algo) {
            SearchAlgo.SEARCH_BFS -> {
                Description(
                    bigTitle = applicationContext.resources.getString(R.string.algo_bfs_title),
                    descriptionSets = listOf(
                        DescriptionUnit(
                            title = null,
                            descriptions = listOf(applicationContext.resources.getString(R.string.algo_bfs_desc))
                        ),
                        DescriptionUnit(
                            title = applicationContext.resources.getString(R.string.algo_bfs_title_complexity),
                            descriptions = listOf(applicationContext.resources.getString(R.string.algo_bfs_complexity))
                        )
                    )
                )
            }

            SearchAlgo.SEARCH_DFS -> {
                Description(
                    bigTitle = applicationContext.resources.getString(R.string.algo_dfs_title),
                    descriptionSets = listOf(
                        DescriptionUnit(
                            title = null,
                            descriptions = listOf(applicationContext.resources.getString(R.string.algo_dfs_desc))
                        ),
                        DescriptionUnit(
                            title = applicationContext.resources.getString(R.string.algo_dfs_title_complexity),
                            descriptions = listOf(applicationContext.resources.getString(R.string.algo_dfs_complexity))
                        )
                    )
                )
            }
        }
    }
}