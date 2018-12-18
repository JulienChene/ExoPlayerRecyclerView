package io.snaker.app.videorecyclerview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import io.snaker.app.video.helper.LoggingInterceptor
import kotlinx.android.synthetic.main.main_fragment.*
import okhttp3.OkHttpClient

class MainFragment : Fragment() {

    companion object {
        private const val IMAGE_URL = "https://www.google.com/url?sa=i&source=images&cd=&ved=2ahUKEwjWlcCBuarfAhXtnuAKHQj5BisQjRx6BAgBEAU&url=http%3A%2F%2Fwww.pbs.org%2Fprogram%2Fbig-pacific%2F&psig=AOvVaw2qP2-E9xptEuxDRSxCYaMP&ust=1545259135417728"
        private const val VIDEO_URL = "https://scontent.cdninstagram.com/vp/c8c1efaf14ea1d42a30017a68f87117f/5C1B1C4F/t50.2886-16/33561327_217477685649488_7146335967157157888_n.mp4"

        val recyclerContent = listOf(Media(false, VIDEO_URL),
            Media(false, VIDEO_URL),
            Media(false, VIDEO_URL),
            Media(false, VIDEO_URL),
            Media(false, VIDEO_URL))

//        val recyclerContent = listOf(Media(true, IMAGE_URL),
//            Media(true, IMAGE_URL),
//            Media(true, IMAGE_URL),
//            Media(false, VIDEO_URL),
//            Media(true, IMAGE_URL),
//            Media(true, IMAGE_URL),
//            Media(true, IMAGE_URL))
    }

    val okHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(LoggingInterceptor(LoggingInterceptor.Level.BODY_AND_HEADERS))
        .build()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = VideoAdapter(recyclerContent, Glide.with(this), okHttpClient)
        recycler_view.adapter = adapter
    }
}
