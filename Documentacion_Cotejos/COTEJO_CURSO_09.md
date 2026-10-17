# COTEJO CURSO 09 — Ícono de la app

## Sección del curso cotejada

- Uso de directorios `mipmap` para íconos de la app.
- Soporte por densidades: `mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`.
- Recursos con calificadores como `anydpi-v26`.
- Gráficos vectoriales en XML.
- Íconos adaptables (API 26+).
- Flujo equivalente al de **Image Asset Studio**.

## Resultado de la revisión

En la versión 08 de FarmApp **no existía aún un conjunto completo de íconos launcher**. Solo había un `drawable` (`ic_farmapp_logo.xml`) usado dentro de la interfaz.

## Cambios incorporados

### 1) Recursos `mipmap`

Se añadieron estos directorios y archivos:

- `app/src/main/res/mipmap-mdpi/ic_launcher.png`
- `app/src/main/res/mipmap-hdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xhdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xxhdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`
- Sus variantes redondas `ic_launcher_round.png`

### 2) Ícono adaptable

Se añadieron:

- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- `app/src/main/res/drawable/ic_launcher_background.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`

Esto permite que Android use el ícono adaptable en API 26 en adelante y recurra a mapas de bits en densidades anteriores.

### 3) Manifest

Se añadió `app/src/main/AndroidManifest.xml` con las referencias:

```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round" />
```

## Concordancia con el curso

| Punto del resumen | Estado en FarmApp |
|---|---|
| Archivos de íconos en `mipmap` | **Incluido** |
| Versiones por densidad (`mdpi` a `xxxhdpi`) | **Incluido** |
| Calificadores de recurso (`v26`) | **Incluido** |
| Elementos de diseño vectorial | **Incluido** |
| Íconos adaptables | **Incluido** |
| Uso equivalente a Image Asset Studio | **Incluido** |

## Cómo mostrarlo a la profesora

1. Abre `res/mipmap-anydpi-v26` y enseña `ic_launcher.xml`.
2. Abre `res/drawable/ic_launcher_foreground.xml` para mostrar el gráfico vectorial en XML.
3. Abre cualquiera de las carpetas `mipmap-*` para mostrar las versiones rasterizadas por densidad.
4. Abre `AndroidManifest.xml` y señala `android:icon` y `android:roundIcon`.

## Observación

El diseño del ícono se creó con un símbolo farmacéutico coherente con FarmApp. En Android Studio también podrías regenerarlo manualmente con **Image Asset Studio** usando el mismo concepto visual.
