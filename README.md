# 3D Model Viewer

Single-activity Android app for loading several bundled `.glb` models, placing them on one screen, and switching each model between container controls and 3D interaction controls.

Built by Kalyani Meshram.

## Requirements Covered

- One `MainActivity`; no fragments or second screens.
- Four bundled `.glb` assets in `app/src/main/assets/models`.
- Multiple models can be added and kept on screen at the same time.
- Each model has its own draggable, pinch-resizable container.
- Each model has always-visible `3D/Move` and `Close` buttons.
- Normal mode is limited to container movement/resizing.
- 3D mode is limited to model rotation/zoom.
- The gesture mode rules are covered by unit tests in `ModelWorkspaceStateTest`.

## 3D Library Choice

The app uses SceneView `4.18.0`, which is a Jetpack Compose wrapper around Google Filament. I picked it because it loads GLB directly from Android assets, owns the Filament lifecycle from Compose, exposes a compact `SceneView` composable, and has a performance preset for constrained devices.

## Performance Decisions

- The bundled sample models are intentionally tiny low-poly GLB files to keep APK size, GPU buffers, and load time small.
- `RenderQuality.Performance` is used for each SceneView.
- One Filament `Engine` and `ModelLoader` are remembered at the app level and passed into model containers.
- Gesture and mode state is stored in small immutable Kotlin data classes with no Android dependencies.
- Work that changes per gesture is limited to scalar frame/transform updates.
- The UI avoids extra screens, fragments, network loading, and heavyweight runtime asset parsing.

## Trade-Offs

Each model container uses its own Texture-backed SceneView so it can be clipped and layered cleanly inside Compose. That is simple and reliable for the client brief, but a more advanced production version could render all models in one Filament scene and implement custom viewport masking for even lower overhead.

The included GLB assets are simple generated shapes rather than detailed art models. This makes the performance behavior predictable on low-end devices and keeps the submission self-contained.

## Known Limitations

- Model containers are clamped inside the visible screen so their controls remain reachable.
- The app uses bundled models only; external file picking is intentionally out of scope because the task asks for bundled GLB samples.
- Release signing is not configured in this repository yet. Add your client signing config before producing the final signed APK.

## Build

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```
