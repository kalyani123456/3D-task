package com.Infusory.task.workspace

import com.Infusory.task.model.ModelAsset
import kotlin.math.max
import kotlin.math.min

/**
 * Built by Kalyani Meshram.
 */
data class ModelFrame(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

/**
 * Built by Kalyani Meshram.
 */
data class ModelTransform(
    val yawDegrees: Float = 0f,
    val pitchDegrees: Float = 0f,
    val zoom: Float = 1f
)

/**
 * Built by Kalyani Meshram.
 */
data class PlacedModel(
    val id: Long,
    val asset: ModelAsset,
    val frame: ModelFrame,
    val transform: ModelTransform = ModelTransform(),
    val isInteracting: Boolean = false
)

/**
 * Built by Kalyani Meshram.
 */
data class ModelWorkspaceState(
    val models: List<PlacedModel> = emptyList(),
    val nextId: Long = 1L
) {
    fun addModel(
        asset: ModelAsset,
        viewportWidth: Float,
        viewportHeight: Float,
        reservedTop: Float = 0f
    ): ModelWorkspaceState {
        val spawnIndex = models.size % SpawnSlots
        val startX = SpawnStartX + SpawnStep * spawnIndex
        val topSafeStart = max(SpawnStartY, reservedTop + SpawnGap)
        val startY = topSafeStart + SpawnStep * spawnIndex
        val frame = ModelFrame(
            x = startX.coerceIn(0f, max(0f, viewportWidth - DefaultSize)),
            y = startY.coerceIn(0f, max(0f, viewportHeight - DefaultSize)),
            width = DefaultSize.coerceAtMost(viewportWidth),
            height = DefaultSize.coerceAtMost(viewportHeight)
        )

        return copy(
            models = models + PlacedModel(
                id = nextId,
                asset = asset,
                frame = frame
            ),
            nextId = nextId + 1L
        )
    }

    fun removeModel(id: Long): ModelWorkspaceState = copy(
        models = models.filterNot { it.id == id }
    )

    fun toggleInteraction(id: Long): ModelWorkspaceState = updateModel(id) { model ->
        model.copy(isInteracting = !model.isInteracting)
    }

    fun moveContainer(
        id: Long,
        deltaX: Float,
        deltaY: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): ModelWorkspaceState = updateModel(id) { model ->
        if (model.isInteracting) {
            model
        } else {
            model.copy(frame = model.frame.moveBy(deltaX, deltaY, viewportWidth, viewportHeight))
        }
    }

    fun resizeContainer(
        id: Long,
        scale: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): ModelWorkspaceState = updateModel(id) { model ->
        if (model.isInteracting) {
            model
        } else {
            model.copy(frame = model.frame.resizeBy(scale, viewportWidth, viewportHeight))
        }
    }

    fun rotateModel(id: Long, deltaX: Float, deltaY: Float): ModelWorkspaceState =
        updateModel(id) { model ->
            if (!model.isInteracting) {
                model
            } else {
                val nextTransform = model.transform.copy(
                    yawDegrees = model.transform.yawDegrees + deltaX * RotationSensitivity,
                    pitchDegrees = (model.transform.pitchDegrees + deltaY * RotationSensitivity)
                        .coerceIn(MinPitchDegrees, MaxPitchDegrees)
                )
                model.copy(transform = nextTransform)
            }
        }

    fun zoomModel(id: Long, scale: Float): ModelWorkspaceState = updateModel(id) { model ->
        if (!model.isInteracting) {
            model
        } else {
            model.copy(
                transform = model.transform.copy(
                    zoom = (model.transform.zoom * scale).coerceIn(MinZoom, MaxZoom)
                )
            )
        }
    }

    private fun updateModel(id: Long, update: (PlacedModel) -> PlacedModel): ModelWorkspaceState =
        copy(models = models.map { model -> if (model.id == id) update(model) else model })

    companion object {
        const val DefaultSize = 260f
        const val MinSize = 160f
        const val MaxSize = 420f
        const val MinZoom = 0.6f
        const val MaxZoom = 2.5f

        private const val SpawnStartX = 24f
        private const val SpawnStartY = 112f
        private const val SpawnStep = 32f
        private const val SpawnGap = 16f
        private const val SpawnSlots = 8
        private const val RotationSensitivity = 0.5f
        private const val MinPitchDegrees = -80f
        private const val MaxPitchDegrees = 80f
    }
}

private fun ModelFrame.moveBy(
    deltaX: Float,
    deltaY: Float,
    viewportWidth: Float,
    viewportHeight: Float
): ModelFrame = copy(
    x = (x + deltaX).coerceIn(0f, max(0f, viewportWidth - width)),
    y = (y + deltaY).coerceIn(0f, max(0f, viewportHeight - height))
)

private fun ModelFrame.resizeBy(
    scale: Float,
    viewportWidth: Float,
    viewportHeight: Float
): ModelFrame {
    val maxSizeForViewport = min(ModelWorkspaceState.MaxSize, min(viewportWidth, viewportHeight))
    val nextSize = (width * scale)
        .coerceIn(ModelWorkspaceState.MinSize, max(ModelWorkspaceState.MinSize, maxSizeForViewport))

    return copy(
        width = nextSize,
        height = nextSize,
        x = x.coerceIn(0f, max(0f, viewportWidth - nextSize)),
        y = y.coerceIn(0f, max(0f, viewportHeight - nextSize))
    )
}
