package com.Infusory.task.model

/**
 * Built by Kalyani Meshram.
 */
data class ModelAsset(
    val key: String,
    val title: String,
    val assetPath: String
)

val SampleModelAssets = listOf(
    ModelAsset(
        key = "cube",
        title = "Cube",
        assetPath = "models/cube.glb"
    ),
    ModelAsset(
        key = "pyramid",
        title = "Pyramid",
        assetPath = "models/pyramid.glb"
    ),
    ModelAsset(
        key = "diamond",
        title = "Diamond",
        assetPath = "models/diamond.glb"
    ),
    ModelAsset(
        key = "prism",
        title = "Prism",
        assetPath = "models/prism.glb"
    )
)
