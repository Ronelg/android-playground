package com.worldturtlemedia.playground.photos.googlephotos.ui.debug

import android.annotation.SuppressLint
import androidx.lifecycle.lifecycleScope
import com.worldturtlemedia.playground.common.base.ui.BaseFragment
import com.worldturtlemedia.playground.common.base.ui.viewbinding.viewBinding
import com.worldturtlemedia.playground.common.ktx.onClick
import com.worldturtlemedia.playground.photos.R
import com.worldturtlemedia.playground.photos.databinding.DebugFragmentBinding
import com.worldturtlemedia.playground.photos.googlephotos.data.AlbumsRepository
import com.worldturtlemedia.playground.photos.googlephotos.data.ApiResult
import com.worldturtlemedia.playground.photos.googlephotos.model.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("SetTextI18n")
class DebugFragment : BaseFragment<DebugFragmentBinding>(R.layout.debug_fragment) {

    override val binding: DebugFragmentBinding by viewBinding { DebugFragmentBinding.bind(it) }

    private val albumsRepository: AlbumsRepository by lazy { AlbumsRepository.instance }

    override fun setupViews() {
        withBinding {
            btnStart.onClick { loadAlbums() }
        }
    }

    private fun loadAlbums() = lifecycleScope.launch(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        albumsRepository.debugFetchAllAlbums().collect { result ->
            withContext(Dispatchers.Main) {
                binding.btnStart.text = if (result is ApiResult.Loading) "Loading..." else "Load"

                when (result) {
                    is ApiResult.Fail -> binding.txtFailure.text = result.error.toString()
                    is ApiResult.Success -> {
                        val elapsed = (System.currentTimeMillis() - start) / 1000
                        displayAlbumResults(result.result, elapsed)
                    }
                }
            }
        }
    }

    private fun displayAlbumResults(albums: List<Album>, elapsed: Long) {
        val totalAlbums = albums.size
        val totalAlbumItems = albums.fold(0L) { count, album -> count + album.itemCount }

        val albumsString = albums.joinToString("\n") { "${it.title}: ${it.itemCount} items" }

        withBinding {
            txtFailure.text = ""
            txtAlbums.text = albumsString
            txtAlbumTotal.text = "Albums: $totalAlbums, Items: $totalAlbumItems"
            txtLoadTime.text = "Took $elapsed seconds to complete"
        }
    }
}