package io.snaker.app.video.media

import android.os.Parcel
import android.os.Parcelable
import com.google.android.exoplayer2.C

class PlaybackInfo : Parcelable {

    var resumeWindow: Int = 0
    var resumePosition: Long = 0
    var resumeVolume: Float = 0f

    @JvmOverloads constructor(resumeWindow: Int = C.INDEX_UNSET, resumePosition: Long = C.TIME_UNSET, resumeVolume: Float = 0f) {
        this.resumeWindow = resumeWindow
        this.resumePosition = resumePosition
        this.resumeVolume = resumeVolume
    }

    constructor(other: PlaybackInfo) : this(other.resumeWindow, other.resumePosition, other.resumeVolume)

    protected constructor(source: Parcel) {
        resumeWindow = source.readInt()
        resumePosition = source.readLong()
        resumeVolume = source.readFloat()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(resumeWindow)
        dest.writeLong(resumePosition)
        dest.writeFloat(resumeVolume)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun reset() {
        resumeWindow = C.INDEX_UNSET
        resumePosition = C.TIME_UNSET
        resumeVolume = 0f
    }

    override fun toString(): String {
        return "State{window=$resumeWindow, position=$resumePosition, volume=$resumeVolume}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlaybackInfo) return false

        return if (resumeWindow != other.resumeWindow) false
        else resumePosition == other.resumePosition
    }

    override fun hashCode(): Int {
        var result = resumeWindow
        result = 31 * result + (resumePosition xor resumePosition.ushr(32)).toInt()
        result = 31 * result + (resumeVolume.hashCode())
        return result
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<PlaybackInfo> = object : Parcelable.ClassLoaderCreator<PlaybackInfo> {
            override fun createFromParcel(source: Parcel, loader: ClassLoader): PlaybackInfo {
                return PlaybackInfo(source)
            }

            override fun createFromParcel(source: Parcel): PlaybackInfo {
                return PlaybackInfo(source)
            }

            override fun newArray(size: Int): Array<PlaybackInfo?> {
                return arrayOfNulls<PlaybackInfo?>(size)
            }
        }
    }
}
