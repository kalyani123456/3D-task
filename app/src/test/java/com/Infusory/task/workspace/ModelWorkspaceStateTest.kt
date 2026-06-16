package com.Infusory.task.workspace

import com.Infusory.task.model.ModelAsset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Built by Kalyani Meshram.
 */
class ModelWorkspaceStateTest {
    private val asset = ModelAsset(
        key = "cube",
        title = "Cube",
        assetPath = "models/cube.glb"
    )

    @Test
    fun addModelCreatesStableIdAndStaggeredFrame() {
        val state = ModelWorkspaceState()
            .addModel(asset, viewportWidth = 1080f, viewportHeight = 1920f)
            .addModel(asset, viewportWidth = 1080f, viewportHeight = 1920f)

        assertEquals(listOf(1L, 2L), state.models.map { it.id })
        assertEquals(ModelFrame(x = 24f, y = 112f, width = 260f, height = 260f), state.models[0].frame)
        assertEquals(ModelFrame(x = 56f, y = 144f, width = 260f, height = 260f), state.models[1].frame)
        assertEquals(3L, state.nextId)
    }

    @Test
    fun addModelPlacesContainerBelowReservedTopArea() {
        val state = ModelWorkspaceState()
            .addModel(
                asset = asset,
                viewportWidth = 1080f,
                viewportHeight = 1920f,
                reservedTop = 240f
            )

        assertEquals(ModelFrame(x = 24f, y = 256f, width = 260f, height = 260f), state.models.single().frame)
    }

    @Test
    fun removeModelDeletesOnlyTheSelectedModel() {
        val state = ModelWorkspaceState()
            .addModel(asset, viewportWidth = 1080f, viewportHeight = 1920f)
            .addModel(asset, viewportWidth = 1080f, viewportHeight = 1920f)
            .removeModel(1L)

        assertEquals(listOf(2L), state.models.map { it.id })
    }

    @Test
    fun normalModeMovesAndResizesContainerWithoutChangingModelTransform() {
        val initial = ModelWorkspaceState()
            .addModel(asset, viewportWidth = 1080f, viewportHeight = 1920f)

        val updated = initial
            .moveContainer(1L, deltaX = 30f, deltaY = 40f, viewportWidth = 1080f, viewportHeight = 1920f)
            .resizeContainer(1L, scale = 1.5f, viewportWidth = 1080f, viewportHeight = 1920f)
            .rotateModel(1L, deltaX = 90f, deltaY = 90f)
            .zoomModel(1L, scale = 1.5f)

        val model = updated.models.single()
        assertFalse(model.isInteracting)
        assertEquals(ModelFrame(x = 54f, y = 152f, width = 390f, height = 390f), model.frame)
        assertEquals(ModelTransform(), model.transform)
    }

    @Test
    fun interactionModeRotatesAndZoomsModelWithoutChangingContainer() {
        val initial = ModelWorkspaceState()
            .addModel(asset, viewportWidth = 1080f, viewportHeight = 1920f)
            .toggleInteraction(1L)

        val updated = initial
            .moveContainer(1L, deltaX = 200f, deltaY = 200f, viewportWidth = 1080f, viewportHeight = 1920f)
            .resizeContainer(1L, scale = 2f, viewportWidth = 1080f, viewportHeight = 1920f)
            .rotateModel(1L, deltaX = 40f, deltaY = -20f)
            .zoomModel(1L, scale = 1.4f)

        val model = updated.models.single()
        assertTrue(model.isInteracting)
        assertEquals(ModelFrame(x = 24f, y = 112f, width = 260f, height = 260f), model.frame)
        assertEquals(ModelTransform(yawDegrees = 20f, pitchDegrees = -10f, zoom = 1.4f), model.transform)
    }

    @Test
    fun resizeAndZoomStayInsidePerformanceFriendlyBounds() {
        val initial = ModelWorkspaceState()
            .addModel(asset, viewportWidth = 320f, viewportHeight = 480f)
            .toggleInteraction(1L)

        val updated = initial
            .resizeContainer(1L, scale = 20f, viewportWidth = 320f, viewportHeight = 480f)
            .zoomModel(1L, scale = 20f)

        val model = updated.models.single()
        assertEquals(ModelFrame(x = 24f, y = 112f, width = 260f, height = 260f), model.frame)
        assertEquals(2.5f, model.transform.zoom)
    }
}
