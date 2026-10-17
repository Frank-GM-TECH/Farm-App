# COTEJO 24 — Cómo usar SQL para leer y escribir en una base de datos

## Correspondencia con el curso oficial

La captura del codelab recoge `SELECT`, `WHERE`, `LIKE`, `DISTINCT`, `GROUP BY`, agregaciones, `ORDER BY`, `LIMIT`, `INSERT`, `UPDATE` y `DELETE`. Este cotejo prepara la siguiente sección del curso, que utiliza **Room**, pero Room NO se adelanta ni se declara implementado aquí.

## Diagnóstico de la versión 23

- No existía una base de datos SQL local de FarmApp. Los seis productos del catálogo se almacenaban en memoria.
- `Retrofit` solo provee datos remotos externos; no sustituye a SQLite.
- Para mantener la secuencia del curso, se añadió un laboratorio SQL independiente con `SQLiteOpenHelper` (herramienta Android estándar), sin conectar la base de práctica a compras, Firebase ni inventario real.

## Archivos y ubicación

- `app/src/main/java/com/example/farmapp/data/sql/SqlLabModels.kt`: modelos, consultas y contrato de repositorio.
- `app/src/main/java/com/example/farmapp/data/sql/SqlLabSqliteRepository.kt`: definición de tabla, carga de 6 filas, SQL, CRUD y transacciones.
- `app/src/main/java/com/example/farmapp/data/sql/SqlLabViewModel.kt`: `StateFlow`, corrutinas, estados y fábrica con inyección manual.
- `app/src/main/java/com/example/farmapp/ui/sql/SqlLabScreen.kt`: Compose, consultas seleccionables, resultados y operaciones CRUD.
- `app/src/main/java/com/example/farmapp/data/AppContainer.kt`: entrega `sqlLabRepository` con `by lazy`.
- `app/src/main/java/com/example/farmapp/FarmAppApplication.kt`: entrega contexto de aplicación al contenedor.
- `app/src/main/java/com/example/farmapp/ui/navigation/FarmAppDestinations.kt` y `FarmAppNavHost.kt`: ruta `sql_lab` protegida por autenticación.
- `app/src/main/java/com/example/farmapp/ui/home/HomeScreen.kt`: enlace al laboratorio.

## Datos y consultas verificables

Tabla: `productos_demo(sku TEXT PRIMARY KEY, nombre TEXT, categoria TEXT, stock INTEGER CHECK(stock>=0))`.

Los 6 registros semilla son ficticios y suman 34 unidades.

| Requisito | Ejemplo ejecutado |
|---|---|
| SELECT | `SELECT sku, nombre, categoria, stock FROM productos_demo` |
| WHERE | `WHERE stock BETWEEN ? AND ?` con argumentos `1`, `5` |
| LIKE | `WHERE nombre LIKE ?` con `%gel%` |
| DISTINCT | `SELECT DISTINCT categoria` |
| GROUP BY | `COUNT(*)`, `SUM(stock)` por categoría |
| ORDER BY | `ORDER BY nombre COLLATE NOCASE ASC` |
| LIMIT | `LIMIT 20` |
| INSERT | Añade solo SKU `FA-SQL-EXTRA` con `ContentValues` |
| UPDATE | Establece `stock=11` solo si coincide `sku=?` |
| DELETE | Borra solamente `sku=?` |

**Seguridad y robustez:** parámetros vinculados en `rawQuery` y CRUD para evitar concatenar valores; acceso al disco en `Dispatchers.IO`; una transacción restaura la semilla sin dejar una base parcial. El botón de reiniciar afecta EXCLUSIVAMENTE a la base de laboratorio SQL.

## Pasos para demostrar en clase

1. Abrir FarmApp, iniciar sesión y tocar «Laboratorio SQL local (demo)».
2. Pulsar **SELECT + ORDER BY + LIMIT** y observar los seis registros.
3. Pulsar **WHERE**, **LIKE**, **DISTINCT** y **GROUP BY** y leer la SQL junto a los resultados.
4. Pulsar **INSERT**. Se agrega `FA-SQL-EXTRA` (si está activa la consulta SELECT). Volver a pulsarlo comprueba la unicidad del SKU.
5. Pulsar **UPDATE**, después **DELETE**. Mostrar las diferencias y restaurar.
6. Cerrar y abrir la app para demostrar persistencia de SQLite. Restablecer ejemplos si es necesario.
7. Explicar por qué la persistencia de producción se migrará a Room cuando corresponda en el curso.

## Pruebas

- `app/src/test/java/com/example/farmapp/data/sql/SqlLabQueryTest.kt`: pruebas JVM del texto de las consultas y parámetros.
- `app/src/androidTest/java/com/example/farmapp/data/sql/SqlLabRepositoryInstrumentedTest.kt`: base SQLite real en un contexto aislado (select, where, like, distinct, group by, insert, update, delete, restricciones, reset y persistencia).
- `app/src/androidTest/java/com/example/farmapp/ui/sql/SqlLabScreenTest.kt`: pruebas de interacción Compose sin abrir base real.
- Revisión aislada con Python SQLite de consultas/semilla disponible en `VALIDACION_24.txt`.

**No se ha compilado toda la app con Gradle ni ejecutado las pruebas de emulador.** Paquete de fuentes, no proyecto Gradle independiente.
