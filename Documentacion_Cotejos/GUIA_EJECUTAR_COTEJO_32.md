# Guía para comprobar el Cotejo 32 en Android Studio

1. Integra la carpeta `app/src` con tu proyecto Android Studio `com.example.farmapp`.
2. Asegúrate de habilitar `viewBinding` y agregar `androidx.compose.ui:ui-viewbinding`, con Compose BOM.
3. Sincroniza Gradle y compila el proyecto. Repara primero cualquier error que aparezca: esta entrega no garantiza APK compilado.
4. Configura Firebase y entra a FarmApp con un usuario de prueba.
5. En **Laboratorio Room**, crea un artículo y abre **Ver detalles**.
6. Presiona **Editar con formulario XML en Compose**.
7. Edita nombre, precio y stock en los `EditText` XML. Presiona **Guardar cambios**.
8. Confirma en pantalla y en **Database Inspector** que los datos se modificaron en `articulos_room_demo`.
9. Vuelve a abrir el diálogo: escribe un precio no válido; comprueba que no se guarde. Cancela y confirma que no hay cambios.
10. Abre `ui/interop/LegacyRoomEditDialog.kt`, `res/layout/dialog_legacy_item.xml` y `ui/room/RoomDemoDetailScreen.kt` para explicar ambas tecnologías.
11. Ejecuta tests locales `:app:testDebugUnitTest` y Android instrumentados `:app:connectedDebugAndroidTest`.

**Observación:** Para restauración de estado, el texto se almacena temporalmente mediante `rememberSaveable` por ID. Nunca se guardan credenciales en el diálogo.
