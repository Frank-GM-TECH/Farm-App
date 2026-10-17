# GUÍA — Ejecutar y verificar el Cotejo 09 en Android Studio

1. Abre tu proyecto FarmApp en Android Studio.
2. Copia o fusiona la carpeta `app/src/main/res` de esta entrega con la de tu proyecto.
3. Si ya tienes un `AndroidManifest.xml`, integra las líneas:
   - `android:icon="@mipmap/ic_launcher"`
   - `android:roundIcon="@mipmap/ic_launcher_round"`
4. Sincroniza Gradle.
5. Ejecuta la app en un emulador o dispositivo.
6. Vuelve al launcher del dispositivo y verifica que el ícono de FarmApp cambió.
7. En **Project > Android > app > res**, revisa las carpetas `mipmap-*` y `mipmap-anydpi-v26`.
8. En **Resource Manager**, comprueba que Android Studio reconoce el launcher icon.

## Qué explicar

- `mipmap` se usa para íconos launcher.
- `drawable` se usa para imágenes comunes dentro de la interfaz.
- `anydpi-v26` contiene el ícono adaptable para Android 8.0+.
- Las carpetas por densidad brindan compatibilidad visual en distintas pantallas.
