package pt.ipt.DAMA.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import pt.ipt.DAMA.R
import pt.ipt.DAMA.ui.views.EmptyActivity

class ArUtils(
    private val context: Context,
    private val arFragment: ArFragment) {

    private var anchor: Anchor? = null
    private val handler = Handler(Looper.getMainLooper())

    init {
        showPopup() // Show initial guidance popup
        // Create an anchor when user taps on an AR plane
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            anchor = hitResult.createAnchor()
        }
    }

    /**
     * Add a sphere to the AR scene at the specified position
     */
    fun addSphereToPosition(
        name: String,
        x: Float,
        y: Float,
        z: Float
    ) {
        // If anchor is not set, retry after a delay
        if (anchor == null) {
            handler.postDelayed({
                addSphereToPosition(name,x, y, z)
            }, 1000)
            return
        }
        // Create a sphere and add it to the scene
        MaterialFactory.makeOpaqueWithColor(context, Color(ContextCompat.getColor(context, R.color.primaryColor)))
            .thenAccept { material ->
                val sphere = ShapeFactory.makeSphere(0.1f, Vector3(0.0f, 0.0f, 0.0f), material)
                val anchorNode = AnchorNode(anchor).apply {
                    setParent(arFragment.arSceneView.scene)
                }
                Log.i("Info", "Adding sphere to position ($x, $y, $z)")
                TransformableNode(arFragment.transformationSystem).apply {
                    this.localPosition = Vector3(x, y, z)
                    setParent(anchorNode)
                    renderable = sphere
                    select()
                    // Add a listener to show info panel when sphere is tapped
                    setOnTapListener { _, _ ->
                        createInfoPanel(this, name)
                    }
                }
                arFragment.arSceneView.scene.addChild(anchorNode)
            }
            .exceptionally {
                it.printStackTrace()
                null
            }
    }

    /**
     * Check if ARCore is available on the device and request installation if needed
     */
    fun checkARCoreAvailability(activity: Activity): Boolean {
        val availability = ArCoreApk.getInstance().checkAvailability(context)
        if (availability.isTransient) {
            return false
        } else {
            return when (availability) {
                ArCoreApk.Availability.SUPPORTED_INSTALLED -> true
                ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD,
                ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                    try {
                        ArCoreApk.getInstance().requestInstall(activity, true)
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_requesting_arcore_installation),
                            Toast.LENGTH_LONG
                        ).show()
                        false
                    }
                }

                ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.arcore_is_not_supported_on_this_device),
                        Toast.LENGTH_LONG
                    ).show()
                    false
                }

                else -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.arcore_availability_unknown),
                        Toast.LENGTH_LONG
                    ).show()
                    false
                }
            }
        }
    }

    /**
     * Create an informational panel above the sphere when tapped
     */
    private fun createInfoPanel(sphereNode: TransformableNode, name: String) {
        ViewRenderable.builder()
            .setView(context, R.layout.info_panel)
            .build()
            .thenAccept { viewRenderable ->
                val infoNode = Node().apply {
                    localPosition = Vector3(0.0f, 0.5f, 0.0f)
                    renderable = viewRenderable
                    val txtName = viewRenderable.view.findViewById<TextView>(R.id.planetName)
                    txtName.text = name
                    this.setOnTapListener { _,_ ->
                        Log.d("Debug", "Button was clicked")
                        Toast.makeText(context, "Button clicked", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, EmptyActivity::class.java)
                        context.startActivity(intent)
                        TODO("Abrir ecrã de detalhes do planeta")
                    }
                }
                sphereNode.setOnTapListener{ _, _ ->
                    if (infoNode.parent == null) {
                        sphereNode.addChild(infoNode)
                    } else {
                        sphereNode.removeChild(infoNode)
                    }
                }


                // Update node orientation to always face the camera
                arFragment.arSceneView.scene.addOnUpdateListener {
                    val cameraPosition = arFragment.arSceneView.scene.camera.worldPosition
                    val nodePosition = infoNode.worldPosition
                    val direction = Vector3.subtract(cameraPosition, nodePosition)
                    infoNode.worldRotation = Quaternion.lookRotation(direction, Vector3.up())
                }
            }
            .exceptionally {
                it.printStackTrace()
                null
            }
    }

    /**
     * Show an initial popup with guidance for the user
     */
    private fun showPopup() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.aviso))
        builder.setMessage(context.getString(R.string.toque_na_tela_para_posicionar_os_objetos_e_mantenha_o_dispositivo_im_vel_por_alguns_segundos_para_estabiliza_o))

        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /**
     * Remove all nodes from the AR scene
     */
    fun removeAllNodes() {
        val scene = arFragment.arSceneView.scene
        val children = scene.children.filterIsInstance<AnchorNode>().toList()
        children.forEach { child ->
            child.anchor?.detach()
            scene.removeChild(child)
        }
    }

}
