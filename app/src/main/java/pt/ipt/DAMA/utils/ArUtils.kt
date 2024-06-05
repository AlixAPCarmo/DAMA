package pt.ipt.DAMA.utils

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
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

class ArUtils(
    private val context: Context,
    private val arFragment: ArFragment) {

    //private var session: Session? = null
    private var anchor: Anchor? = null
    private val handler = Handler(Looper.getMainLooper())

    init {
        //initializeSession()
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
            Log.d("Debug", "Session or anchor is null, delaying sphere creation")
            handler.postDelayed({
                addSphereToPosition(name,x, y, z)
            }, 1000)
            return
        }
        MaterialFactory.makeOpaqueWithColor(context, Color(android.graphics.Color.RED))
            .thenAccept { material ->
                val sphere = ShapeFactory.makeSphere(0.1f, Vector3(0.0f, 0.0f, 0.0f), material)
                val anchorNode = AnchorNode(anchor).apply {
                    setParent(arFragment.arSceneView.scene)
                }
                Log.d("Debug", "Adding sphere to position ($x, $y, $z)")
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

    private fun initializeSession() {
        Log.d("Debug", "Initializing AR session")
        if (arFragment.arSceneView == null) {
            // ArSceneView ainda não está pronto; adia a inicialização
            Log.d("Debug", "ArSceneView is null, delaying initialization")
            handler.postDelayed({ initializeSession() }, 2000)
            return
        }
        try {
            val arSession = Session(context).apply {
                val config = Config(this).apply {
                    updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                }
                configure(config)
            }
            arFragment.arSceneView.setupSession(arSession)
            //this.session = arSession
        } catch (e: UnavailableException) {
            Log.e("ARActivity", "ARCore not available: ${e.localizedMessage}")
            Toast.makeText(
                context,
                "ARCore not available: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
            handler.postDelayed({initializeSession()}, 5000)
        }
    }

    private fun createInfoPanel(sphereNode: TransformableNode, name: String) {
        ViewRenderable.builder()
            .setView(context, R.layout.info_panel)
            .build()
            .thenAccept { viewRenderable ->
                val infoNode = Node().apply {
                    renderable = viewRenderable
                    val txtName = viewRenderable.view.findViewById<TextView>(R.id.planetName)
                    val btn = viewRenderable.view.findViewById<TextView>(R.id.planetButton)
                    txtName.text = name
                    btn.setOnClickListener {
                        Toast.makeText(context, "Button clicked", Toast.LENGTH_SHORT).show()
                        TODO("Abrir ecrã de detalhes do planeta")
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

    fun removeAllNodes() {
        val scene = arFragment.arSceneView.scene
        // Cria uma cópia da lista de filhos para evitar modificações concorrentes
        val children = ArrayList(scene.children)
        for (child in children) {
            if (child is AnchorNode) {
                // Se o node for um AnchorNode, remove o âncora associado primeiro
                child.anchor?.detach()
            }
            // Remove o node da cena
            scene.removeChild(child)
        }
    }

}
