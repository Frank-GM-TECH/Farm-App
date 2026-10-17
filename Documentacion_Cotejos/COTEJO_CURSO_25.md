# Cotejo 25 — Cómo conservar datos con Room

## Lección oficial

[Cómo conservar datos con Room](https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?hl=es-419), curso *Android Basics with Compose*.

La lección enseña a reemplazar llamadas directas a SQLite por la capa de persistencia Room: `@Entity`, `@ColumnInfo`, `@PrimaryKey`, `@Dao`, `@Insert`, `@Update`, `@Delete`, `@Query`, una clase `RoomDatabase`, repositorio, DI manual y uso de Database Inspector.

## Diagnóstico de la versión 24

FarmApp ya tenía una base educativa creada con `SQLiteOpenHelper` y SQL directamente en el Cotejo 24. **No tenía Room**. Esa base permanece intacta (`farmapp_laboratorio_sql.db`), para que la profesora pueda comparar ambas aproximaciones.

## Qué se añadió

- `data/room/RoomDemoItem.kt`: entidad `articulos_room_demo`, con id generado, nombre, precio en céntimos y cantidad.
- `data/room/RoomDemoDao.kt`: consultas y CRUD mediante Room; observación reactiva con `Flow`.
- `data/room/RoomDemoDatabase.kt`: base de datos Room, versión 1.
- `data/room/RoomDemoRepository.kt`: contrato + implementación DAO (tiene pruebas con repositorios falsos).
- `data/room/RoomDemoForm.kt`: validación de entradas, Kotlin puro.
- `data/room/RoomDemoViewModel.kt`: recolección `Flow`, `StateFlow` de UI, guardado y manejo de errores con corrutinas.
- `ui/room/RoomDemoScreen.kt`: formulario Compose + lista de tarjetas; añade, incrementa existencias y elimina.
- `data/AppContainer.kt`: `roomDemoRepository` y base `Room.databaseBuilder()` con `by lazy` a nivel de `Application`.
- `HomeScreen.kt`, `FarmAppNavHost.kt`, `FarmAppDestinations.kt`: ruta protegida `room_demo` desde Inicio, callback de navegación y botón accesible.
- `res/values/strings.xml`: textos extraídos a recursos.
- `ManualDiViewModelTest.kt`: contenedor falso adaptado para inyectar Room sin usar una base real.

### Cómo distinguir ambas bases de datos

| Tema | C24 SQLite manual | C25 Room |
|---|---|---|
| Base | `farmapp_laboratorio_sql.db` | `farmapp_room_demo.db` |
| Tabla | `productos_demo` | `articulos_room_demo` |
| Acceso | `SQLiteOpenHelper` / `SQLiteDatabase` | `@Dao` / `RoomDatabase` |
| Operaciones | Sentencias SQL explícitas | `@Insert`, `@Update`, `@Delete`, `@Query` |
| UI | Laboratorio de SQL | Formulario y lista Room con `Flow` |
| Propósito | Aprender sintaxis SQL | Aprender Jetpack Room |

**Ambas son bases de aprendizaje y NO contienen datos farmacéuticos reales.** El catálogo local de seis productos, el catálogo remoto, Firebase y los demás cotejos se conservan separados.

## Correspondencia con el resumen del curso

| Concepto | Archivo y evidencia | Estado |
|---|---|---|
| `@Entity` | `RoomDemoItem.kt` | Incorporado |
| `@ColumnInfo` y `@PrimaryKey` | `RoomDemoItem.kt` | Incorporado |
| `@Dao` | `RoomDemoDao.kt` | Incorporado |
| `@Insert`, `@Update`, `@Delete` | `RoomDemoDao.kt` | Incorporado |
| `@Query` | `RoomDemoDao.kt` | Incorporado |
| `RoomDatabase` | `RoomDemoDatabase.kt` | Incorporado |
| Repositorio y DI manual | `RoomDemoRepository.kt`, `AppContainer.kt` | Incorporado |
| Persistencia y actualización de Compose | `RoomDemoScreen.kt`, `RoomDemoViewModel.kt` | Preparados |
| Database Inspector | Ver `GUIA_EJECUTAR_COTEJO_25.md` | Verificación en Android Studio pendiente |

## Pruebas añadidas

- `test/.../RoomDemoFormTest.kt`: 7 pruebas locales de entrada válida y errores/límites.
- `test/.../RoomDemoViewModelTest.kt`: 4 pruebas locales con repositorio falso: guardar, observar cambios, fallar con validación y error simulado, actualizar/eliminar.
- `androidTest/.../RoomDemoDaoTest.kt`: 5 pruebas Room en memoria: insertar, consultar, actualizar, eliminar, ordenar y estado vacío (última conjunta).
- `androidTest/.../RoomDemoScreenTest.kt`: 3 pruebas Compose: formulario, lista y estado vacío.

## Seguridad y limitaciones

- Room usa archivo **distinto** del taller SQL del Cotejo 24. No hay migraciones de C24 ni de datos reales.
- Valores monetarios de práctica almacenados como `Long` en céntimos, no `Double`.
- No se habilitó `fallbackToDestructiveMigration()`, para evitar borrados imprevistos; cuando cambiemos esquemas habrá que implementar migraciones.
- `exportSchema = false` **solo en esta demostración**; en una futura base de producción se activará exportación de esquemas y pruebas de migración.
- No se comprobó compilación completa de Android ni ejecución de Database Inspector aquí. No deben presentarse como aprobadas.

## Cómo exponer

1. Enseñar entidad, DAO y clase Database de Room.
2. Mostrar en `DefaultAppContainer` la creación única de la base por `by lazy` y el repositorio.
3. Enseñar en `RoomDemoViewModel` cómo se observa `Flow` y se expone `StateFlow`.
4. En emulador, entrar a Inicio → Laboratorio Room, guardar un artículo, editar existencias, eliminar y reiniciar la app para comprobar persistencia.
5. Abrir Database Inspector y ver tabla `articulos_room_demo` de `farmapp_room_demo.db`.
6. Comparar con el laboratorio SQL anterior y ejecutar las pruebas de DAO con base en memoria.
