package com.Infusory.task.ui.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.Infusory.task.model.ModelAsset
import com.Infusory.task.model.SampleModelAssets
import com.Infusory.task.workspace.ModelTransform
import com.Infusory.task.workspace.ModelWorkspaceState
import com.Infusory.task.workspace.PlacedModel
import com.google.android.filament.Engine
import io.github.sceneview.RenderQuality
import io.github.sceneview.SceneView
import io.github.sceneview.SurfaceType
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import kotlin.math.roundToInt

/**
 * Built by Kalyani Meshram.
 */
@Composable
fun ModelViewerApp(modifier: Modifier = Modifier) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    var workspace by remember { mutableStateOf(ModelWorkspaceState()) }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = modifier
            .background(StageBackground)
    ) {
        ModelPickerDock(
            assets = SampleModelAssets,
            onAddModel = { asset ->
                workspace = workspace.addModel(
                    asset = asset,
                    viewportWidth = viewportSize.safeWidth(),
                    viewportHeight = viewportSize.safeHeight()
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .onSizeChanged { viewportSize = it }
        ) {
            StageGrid(modifier = Modifier.fillMaxSize())

            workspace.models.forEach { model ->
                key(model.id) {
                    ModelContainer(
                        model = model,
                        engine = engine,
                        modelLoader = modelLoader,
                        onMove = { delta ->
                            workspace = workspace.moveContainer(
                                id = model.id,
                                deltaX = delta.x,
                                deltaY = delta.y,
                                viewportWidth = viewportSize.safeWidth(),
                                viewportHeight = viewportSize.safeHeight()
                            )
                        },
                        onResize = { scale ->
                            workspace = workspace.resizeContainer(
                                id = model.id,
                                scale = scale,
                                viewportWidth = viewportSize.safeWidth(),
                                viewportHeight = viewportSize.safeHeight()
                            )
                        },
                        onRotate = { delta ->
                            workspace = workspace.rotateModel(
                                id = model.id,
                                deltaX = delta.x,
                                deltaY = delta.y
                            )
                        },
                        onZoom = { scale ->
                            workspace = workspace.zoomModel(id = model.id, scale = scale)
                        },
                        onToggleInteraction = {
                            workspace = workspace.toggleInteraction(model.id)
                        },
                        onClose = {
                            workspace = workspace.removeModel(model.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelContainer(
    model: PlacedModel,
    engine: Engine,
    modelLoader: ModelLoader,
    onMove: (Offset) -> Unit,
    onResize: (Float) -> Unit,
    onRotate: (Offset) -> Unit,
    onZoom: (Float) -> Unit,
    onToggleInteraction: () -> Unit,
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    val shape = RoundedCornerShape(8.dp)
    val frame = model.frame
    val borderColor = if (model.isInteracting) ActiveBorder else RestingBorder

    Box(
        modifier = Modifier
            .offset { IntOffset(frame.x.roundToInt(), frame.y.roundToInt()) }
            .size(
                width = with(density) { frame.width.toDp() },
                height = with(density) { frame.height.toDp() }
            )
            .clip(shape)
            .background(ContainerBackground)
            .border(width = 1.dp, color = borderColor, shape = shape)
    ) {
        ModelScene(
            asset = model.asset,
            transform = model.transform,
            engine = engine,
            modelLoader = modelLoader,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(model.id, model.isInteracting) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (model.isInteracting) {
                            if (pan != Offset.Zero) onRotate(pan)
                            if (zoom != 1f) onZoom(zoom)
                        } else {
                            if (pan != Offset.Zero) onMove(pan)
                            if (zoom != 1f) onResize(zoom)
                        }
                    }
                }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            FilledTonalButton(
                onClick = onToggleInteraction,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (model.isInteracting) ActiveButton else ControlButton,
                    contentColor = Color.White
                ),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = if (model.isInteracting) "Move" else "3D",
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            FilledTonalButton(
                onClick = onClose,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = CloseButton,
                    contentColor = Color.White
                ),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = "Close",
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun ModelScene(
    asset: ModelAsset,
    transform: ModelTransform,
    engine: Engine,
    modelLoader: ModelLoader,
    modifier: Modifier = Modifier
) {
    SceneView(
        modifier = modifier,
        surfaceType = SurfaceType.TextureSurface,
        engine = engine,
        modelLoader = modelLoader,
        renderQuality = RenderQuality.Performance,
        cameraManipulator = null,
        autoFitContent = true
    ) {
        rememberModelInstance(modelLoader, asset.assetPath)?.let { instance ->
            ModelNode(
                modelInstance = instance,
                autoAnimate = true,
                rotation = Rotation(
                    x = transform.pitchDegrees,
                    y = transform.yawDegrees,
                    z = 0f
                ),
                scale = Scale(transform.zoom)
            )
        }
    }
}

@Composable
private fun ModelPickerDock(
    assets: List<ModelAsset>,
    onAddModel: (ModelAsset) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = DockBackground,
        tonalElevation = 4.dp,
        shadowElevation = 10.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Add",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(2.dp))
            assets.forEach { asset ->
                Button(
                    onClick = { onAddModel(asset) },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AssetButton,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(
                        text = asset.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun StageGrid(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.radialGradient(
                colors = listOf(Color(0xFF243126), Color(0xFF121416), Color(0xFF08090A)),
                radius = 1100f
            )
        )
    )
}

private fun IntSize.safeWidth(): Float = width.takeIf { it > 0 }?.toFloat() ?: 1080f

private fun IntSize.safeHeight(): Float = height.takeIf { it > 0 }?.toFloat() ?: 1920f

private val StageBackground = Color(0xFF08090A)
private val ContainerBackground = Color(0xFF111417)
private val DockBackground = Color(0xEE171A1E)
private val RestingBorder = Color(0x99E7D8B4)
private val ActiveBorder = Color(0xFF55D6C2)
private val ControlButton = Color(0xCC26323A)
private val ActiveButton = Color(0xCC126F63)
private val CloseButton = Color(0xCC8F3535)
private val AssetButton = Color(0xFF62513B)
