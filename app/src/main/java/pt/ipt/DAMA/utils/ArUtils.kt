package pt.ipt.DAMA.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
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
import pt.ipt.DAMA.retrofit.MyCookieJar
import pt.ipt.DAMA.ui.views.CelestialActivity

class ArUtils(
    private val context: Context,
    private val arFragment: ArFragment) {

    private var anchor: Anchor? = null
    private val handler = Handler(Looper.getMainLooper())

    init {
        showPopup()
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            anchor = hitResult.createAnchor()
        }
    }
    fun addSphereToPosition(
        name: String,
        x: Float,
        y: Float,
        z: Float
    ) {
        if (anchor == null) {
            handler.postDelayed({
                addSphereToPosition(name,x, y, z)
            }, 1000)
            return
        }
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
                            "Error requesting ARCore installation",
                            Toast.LENGTH_LONG
                        ).show()
                        false
                    }
                }

                ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                    Toast.makeText(
                        context,
                        "ARCore is not supported on this device",
                        Toast.LENGTH_LONG
                    ).show()
                    false
                }

                else -> {
                    Toast.makeText(
                        context,
                        "ARCore availability unknown",
                        Toast.LENGTH_LONG
                    ).show()
                    false
                }
            }
        }
    }

    private fun createInfoPanel(sphereNode: TransformableNode, name: String) {
        ViewRenderable.builder()
            .setView(context, R.layout.info_panel)
            .build()
            .thenAccept { viewRenderable ->
                val infoNode = Node().apply {
                    localPosition = Vector3(0.0f, 0.5f, 0.0f)
                    renderable = viewRenderable
                    val txtName = viewRenderable.view.findViewById<TextView>(R.id.planetName)
                    val btn = viewRenderable.view.findViewById<ImageView>(R.id.planetButton)
                    txtName.text = name
                    if(MyCookieJar(context).isUserLoggedIn()){
                        this.setOnTapListener { _,_ ->
                            Toast.makeText(context, "Button clicked", Toast.LENGTH_SHORT).show()
                            val intent = Intent(context, CelestialActivity::class.java)
                            intent.putExtra("name", name)
                            context.startActivity(intent)
                        }
                        btn.visibility = TextView.VISIBLE
                    }else{
                        this.setOnTapListener { _, _ ->
                            Toast.makeText(context, "Button clicked", Toast.LENGTH_SHORT).show()
                        }
                        btn.visibility = TextView.GONE
                    }

                }
                // Adiciona um listener de toque para mostrar/ocultar o painel de informações
                sphereNode.setOnTapListener{ _, _ ->
                    if (infoNode.parent == null) {
                        sphereNode.addChild(infoNode)
                    } else {
                        sphereNode.removeChild(infoNode)
                    }
                }


                // Listener para atualizar a orientação a cada frame
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

    private fun showPopup() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Aviso")
        builder.setMessage("Toque na tela para posicionar os objetos e mantenha o dispositivo imóvel por alguns segundos para estabilização. ")

        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun removeAllNodes() {
        val scene = arFragment.arSceneView.scene
        val children = scene.children.filterIsInstance<AnchorNode>().toList()
        children.forEach { child ->
            child.anchor?.detach()
            scene.removeChild(child)
        }
    }

}
