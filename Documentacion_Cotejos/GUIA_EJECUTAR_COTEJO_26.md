# GUÍA — Cotejo 26 en Android Studio

1. Integra los archivos `app/src` de esta versión sobre tu proyecto Compose (respaldando primero la versión anterior) y **conserva las dependencias Gradle Room/KSP del C25**.
2. Sincroniza Gradle, compila y ejecuta FarmApp en un emulador Android.
3. Inicia sesión: **Inicio → Laboratorio Room local (demo)**.
4. Guarda `Gasas demo`, `6.50` y `2` en la base Room.
5. Pulsa **Ver detalles**. Se abre `room_demo/{itemId}`. Verifica ID, precio y stock.
6. Pulsa **Vender 1 unidad**. El stock baja de 2 a 1; al volver a vender baja a 0. Al llegar a cero, el botón queda deshabilitado.
7. Pulsa **Editar artículo**. Modifica nombre, precio y stock, y pulsa **Guardar cambios**. Regresa con Atrás y comprueba los datos actualizados.
8. Elimina un artículo y verifica que desaparezca tanto del detalle como del inventario.
9. Reinicia FarmApp y comprueba la persistencia local (la sesión Firebase puede requerir configuración).
10. Abre **View > Tool Windows > App Inspection > Database Inspector**; revisa `farmapp_room_demo.db`, tabla `articulos_room_demo`.
11. Ejecuta `RoomDemoInventoryRulesTest`, `RoomDemoDetailViewModelTest`, `RoomDemoInventoryDaoTest` y `RoomDemoDetailsScreenTest`.

### Terminal de Windows

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

### Criterios de aceptación

- Inventario se actualiza al insertar, vender, editar y eliminar.
- Nunca puede quedar stock negativo tras pulsar Vender.
- Edición no acepta datos inválidos.
- La pantalla de detalle recupera el artículo por ID, y muestra ausencia cuando se elimina.
- Se conserva la base del C25 y el laboratorio SQL del C24.

No afirmar ejecución exitosa hasta obtener un BUILD SUCCESSFUL en Android Studio y las pruebas verdes.
