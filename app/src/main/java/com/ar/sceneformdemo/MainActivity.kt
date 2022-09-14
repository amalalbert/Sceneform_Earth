package com.ar.sceneformdemo

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.ar.helpers.CameraPermissionHelper
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.function.BiFunction


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    val TAG = "CustomMaterialSample"
//    private var points: ArrayList<Vector> = ArrayList()
    private var radius: Float? = null
//    lateinit var polyline: Polyline
    private var color: Int? = null
    private var alpha: Float? = null
    private val MIN_OPENGL_VERSION = 3.0
    private var hasFinishedLoading = false
    private var arFragment: ArFragment? = null
    private var andyRenderable: ModelRenderable? = null
    private var layout_2d: ViewRenderable? = null
    private var earth: ModelRenderable? = null
    private val sliderSettings: SliderSettings = SliderSettings()
    private val degreesPerSecond = 90.0f
    val orbitanimator = createAnimator()
    private var pressTime = -1L;
    private var releaseTime = 1L;
    private var duration = -1L;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(com.ar.sceneformdemo.R.layout.activity_ux)
//        val b = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        arFragment =
            supportFragmentManager.findFragmentById(com.ar.sceneformdemo.R.id.ux_fragment) as ArFragment?
        val ControlsStage: CompletableFuture<ViewRenderable> =
            ViewRenderable.builder().setView(this,com.ar.sceneformdemo.R.layout.alert_layout).build()
        val Earthstage: CompletableFuture<ModelRenderable> =
            ModelRenderable.builder().setSource(this, Uri.parse("Earth.sfb")).build()
// Code to insert object probably happens here
            CompletableFuture.allOf(ControlsStage,Earthstage).handle(BiFunction<Void, Throwable, Any?> { notUsed: Void?, throwable: Throwable? ->
                // When you build a Renderable, Sceneform loads its resources in the background while
                // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                // before calling get().
                if (throwable != null) {
                    val toast =
                        Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }
                try {
                    earth = Earthstage.get()
                    layout_2d = ControlsStage.get()
                    // Everything finished loading successfully.
                    hasFinishedLoading = true
                } catch (ex: InterruptedException) {
                    val toast =
                        Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                } catch (ex: ExecutionException) {
                    val toast =
                        Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }
                null
            })
        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
            .setSource(this, com.ar.sceneformdemo.R.raw.andy)
            .build()
            .thenAccept { renderable: ModelRenderable ->
                andyRenderable = renderable
            }
            .exceptionally { throwable: Throwable? ->
                val toast =
                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                null
            }
//        val anchor: Anchor?
//        val anchorNode = AnchorNode()
//        val anchorNode = AnchorNode(anchor)
//        anchorNode.setParent(arFragment!!.arSceneView.scene)
//        val sliderControls = TransformableNode(arFragment!!.transformationSystem)
//        sliderControls.setParent(anchorNode)
//        sliderControls.renderable = layout_2d
//        sliderControls.localPosition = Vector3(0.0f, 0.25f, 0.0f)
        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            if (andyRenderable == null) {
                Log.d("devhell", "onCreate: $andyRenderable")
                return@setOnTapArPlaneListener
            }
            // Create the Anchor.
            val anchor: Anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment!!.arSceneView.scene)
            val sliderControls = TransformableNode(arFragment!!.transformationSystem)
            sliderControls.setParent(anchorNode)
            sliderControls.renderable = layout_2d
            sliderControls.localPosition = Vector3(0.0f, 0.35f, 0.0f)
            sliderControls.select()

//             Create the transformable andy and add it to the anchor.
            val andy =
                TransformableNode(arFragment!!.transformationSystem)
            andy.setParent(anchorNode)
            andy.renderable = earth
            andy.select()

            // Toggle the solar controls on and off by tapping the sun.
            andy.setOnTapListener { hitTestResult: HitTestResult?, motionEvent: MotionEvent? ->
                sliderControls.isEnabled = (!sliderControls.isEnabled)
                if (motionEvent != null) {
                    if(motionEvent.action == MotionEvent.ACTION_DOWN){
                        pressTime = System.currentTimeMillis();
                        if(releaseTime != -1L) duration = pressTime - releaseTime;
                    } else if(motionEvent.action == MotionEvent.ACTION_UP){
                        releaseTime = System.currentTimeMillis();
                        duration = System.currentTimeMillis() - pressTime;
                    }
                }
            }

            if (orbitanimator != null) {
                orbitanimator.target = andy
                orbitanimator.duration = getAnimationDuration()
                orbitanimator.start()
            }

//            val orbitDegreesPerSecond = 0f
//            // Orbit is a rotating node with no renderable positioned at the sun.
//            // The planet is positioned relative to the orbit so that it appears to rotate around the sun.
//            // This is done instead of making the sun rotate so each planet can orbit at its own speed.
//            val orbit = RotatingNode(sliderSettings, true, false, 0f)
//            orbit.setDegreesPerSecond(orbitDegreesPerSecond)
//            orbit.setParent(andy)

            val solarControlsView: View? = layout_2d?.view

            val rotationSpeedBar = solarControlsView?.findViewById<SeekBar>(R.id.rotationSpeedBar)
            if (rotationSpeedBar != null) {
                rotationSpeedBar.progress = (sliderSettings.getRotationSpeedMultiplier() * 10.0f).toInt()
            }
            rotationSpeedBar?.setOnSeekBarChangeListener(
                object : OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        val ratio = progress.toFloat() / rotationSpeedBar.max.toFloat()
                        sliderSettings.setRotationSpeedMultiplier(ratio * 10.0f)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        orbitanimator?.duration = getAnimationDuration()
                    }
                })
        }
    }



    override fun onResume() {
        super.onResume()
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }
    }
    private fun createAnimator(): ObjectAnimator? {
        // Node's setLocalRotation method accepts Quaternions as parameters.
        // First, set up orientations that will animate a circle.
        val orientation1 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 0f)
        val orientation2 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 120f)
        val orientation3 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 240f)
        val orientation4 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 360f)
        val orbitAnimation = ObjectAnimator()
        orbitAnimation.setObjectValues(orientation1, orientation2, orientation3, orientation4)

        // Next, give it the localRotation property.
        orbitAnimation.setPropertyName("localRotation")

        // Use Sceneform's QuaternionEvaluator.
        orbitAnimation.setEvaluator(QuaternionEvaluator())

        //  Allow orbitAnimation to repeat forever
        orbitAnimation.repeatCount = ObjectAnimator.INFINITE
        orbitAnimation.repeatMode = ObjectAnimator.RESTART
        orbitAnimation.interpolator = LinearInterpolator()
        orbitAnimation.setAutoCancel(true)
        return orbitAnimation
    }
    private fun getAnimationDuration(): Long {
        return (1000 * 360 / (degreesPerSecond * getSpeedMultiplier())).toLong()
    }
    private fun getSpeedMultiplier(): Float {
        return sliderSettings.getRotationSpeedMultiplier()
    }
//    private fun createLine() {
//        this.radius?.let { radius ->
//            this.color?.let { color ->
//                this.alpha?.let { alpha ->
//                    polyline = Polyline(radius)
//                    polyline.appendPoint(points[0])
//                    val material = Material()
//                    material.diffuseColor = color
//                    material.transparencyMode = Material.TransparencyMode.A_ONE
//                    material.lightingModel = Material.LightingModel.PHONG
//                    material.blendMode = Material.BlendMode.ALPHA
//                    material.cullMode = Material.CullMode.BACK
//                    polyline.materials = Arrays.asList(material)
//                    val sphereNode = Node()
//                    sphereNode.geometry = polyline
//                    sphereNode.opacity = alpha
//                    this.addChildNode(sphereNode)
//                }
//            }
//        }
//    }
}