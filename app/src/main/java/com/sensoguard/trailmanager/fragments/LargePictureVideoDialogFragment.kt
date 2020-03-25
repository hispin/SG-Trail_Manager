package com.sensoguard.trailmanager.fragments

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.halilibo.bvpkotlin.BetterVideoPlayer
import com.sensoguard.trailmanager.R
import com.sensoguard.trailmanager.classes.TouchImageView
import com.sensoguard.trailmanager.global.ACTION_PICTURE_KEY
import com.sensoguard.trailmanager.global.ACTION_TYPE_KEY
import com.sensoguard.trailmanager.global.ACTION_VIDEO_KEY
import com.sensoguard.trailmanager.global.IMAGE_PATH_KEY
import kotlinx.android.synthetic.main.activity_my_screens.*
import java.io.File


class LargePictureVideoDialogFragment : DialogFragment() {

    private var imgPath: String? = null
    private var actionType: Int? = null
    private var ibClose: AppCompatImageButton? = null
    private var ivMyVideo: BetterVideoPlayer? = null
    private var ivMyCaptureImage: TouchImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(
            R.layout.fragment_large_picture_video,
            container,
            false
        )


        initViews(view)


        val bundle = arguments
        actionType = bundle?.getInt(ACTION_TYPE_KEY, -1)
        imgPath = bundle?.getString(IMAGE_PATH_KEY, null)
        if (actionType == ACTION_PICTURE_KEY) {
            showPicture(imgPath)
        } else if (actionType == ACTION_VIDEO_KEY) {
            imgPath?.let { showVideo(it) }
        }

        // Inflate the layout for this fragment
        return view
    }

    private fun showVideo(imgPath: String) {
        ivMyVideo?.visibility = View.VISIBLE
        ivMyCaptureImage?.visibility = View.GONE
        //val imgFile = File(imgPath)
        val imgFile = File(context?.filesDir, imgPath)
        ivMyVideo?.setSource(Uri.fromFile(imgFile))
    }

    private fun showPicture(path: String?) {
        ivMyVideo?.visibility = View.GONE
        ivMyCaptureImage?.visibility = View.VISIBLE

        if (path == null)
            return

        //from external storage
        val imgFile = File(path)

        //from internal storage
        //val imgFile = File(context?.filesDir, path)

        if (imgFile.exists()) {
            //Picasso.get().load(File(imgFile.absolutePath)).into(ivMyCaptureImage)
            showPicture(imgFile, ivMyCaptureImage)
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setLayoutLandscape()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setLayoutPortrait()
        }
    }

    private fun setLayoutPortrait() {
    }

    private fun setLayoutLandscape() {
    }

    //show the picture by glide
    private fun showPicture(imgFile: File, ivMyCaptureImage: AppCompatImageView?) {
        ivMyCaptureImage?.let {
            Glide.with(activity!!).load(File(imgFile.absolutePath)).listener(object :
                RequestListener<Drawable> {

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {

                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Toast.makeText(context, "error loading image", Toast.LENGTH_LONG).show()
                    return false
                }

            }).into(it)
        }
    }


    private fun initViews(view: View?) {
        ibClose = view?.findViewById(R.id.ibClose)
        ibClose?.setOnClickListener {
            dismiss()
        }

        ivMyVideo = view?.findViewById(R.id.ivMyVideo)
        ivMyCaptureImage = view?.findViewById(R.id.ivMyCaptureImage)
    }

    private fun disableOrientation() {
        toolbar.visibility = View.VISIBLE
        //tab_layout.visibility=View.VISIBLE
        if (activity == null) {
            return
        }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setLayoutPortrait()
    }

    private fun enableOrientation() {
        toolbar.visibility = View.GONE
//        activity.findViewById<>(R.id.layout_table)
//        tab_layout.visibility=View.GONE
//        val layout =
//            view!!.findViewById(layout_table) as TableLayout // change id here
//
//        layout.visibility = View.GONE
        if (activity == null) {
            return
        }
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onStop() {
        disableOrientation()
        super.onStop()
    }

    override fun onStart() {
        enableOrientation()

        //configuration the fragment dialog as full screen
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }

        super.onStart()
    }
}