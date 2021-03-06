package com.test.myapplication

import android.content.Context
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.IncapableCause
import com.zhihu.matisse.internal.entity.Item
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils
import java.util.*

/**
 *
 * @ProjectName:    zbplant
 * @Package:        com.kw.lib_common.bean
 * @Description:     java类作用描述
 * @Author:         kw
 * @CreateDate:     2020/6/24 16:29
 * @UpdateDate:     2020/6/24 16:29
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */
class GifSizeFilter(p0: Int, p1: Int, p2: Int) : Filter() {
    private var mMinWidth = p0
    private var mMinHeight = p1
    private var mMaxSize = p2
    override fun filter(context: Context?, item: Item?): IncapableCause? {
        if (!needFiltering(context, item)) return null

        val size =
            PhotoMetadataUtils.getBitmapBound(context!!.contentResolver, item!!.contentUri)
        return if (size.x < mMinWidth || size.y < mMinHeight || item.size > mMaxSize) {
            IncapableCause(
                IncapableCause.DIALOG,
                context.getString(
                    R.string.common_error_gif,
                    mMinWidth,
                    PhotoMetadataUtils.getSizeInMB(mMaxSize.toLong()).toString()
                )
            )
        } else null
    }

    override fun constraintTypes(): HashSet<MimeType?> {
        return object : HashSet<MimeType?>() {
            init {
                add(MimeType.GIF)
            }
        }
    }
}