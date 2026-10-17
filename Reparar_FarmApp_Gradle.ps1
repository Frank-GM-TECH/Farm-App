# Reparación de FarmApp (HU01 Google) para Gradle 8.13 + Kotlin 2.2.21.
# Guardar este archivo en la carpeta raíz FarmApp y ejecutarlo desde PowerShell.
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
$archivo = Join-Path $PSScriptRoot 'app\build.gradle.kts'
if (-not (Test-Path -LiteralPath $archivo)) {
    throw 'No se encuentra app\build.gradle.kts. Copia este archivo en la carpeta FarmApp (junto a settings.gradle.kts).'
}

$texto = [System.IO.File]::ReadAllText($archivo)
$reemplazos = @(
    @('    kotlinOptions { jvmTarget = "17" }', ""),
    @('val composeBom = platform("androidx.compose:compose-bom:2026.06.00")', "val composeBomVersion = `"2026.06.00`""),
    @('    implementation(composeBom)', '    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))'),
    @('    androidTestImplementation(composeBom)', '    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBomVersion"))')
)
foreach ($p in $reemplazos) {
    if ($texto.Contains($p[0])) {
        $texto = $texto.Replace($p[0], $p[1])
    }
}
$kotlinBlock = @'
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
'@
if (-not $texto.Contains('jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)')) {
    $marcador = 'val composeBomVersion = "2026.06.00"'
    if (-not $texto.Contains($marcador)) { throw 'No se reconoció la estructura del Gradle. No se modificará el proyecto.' }
    $texto = $texto.Replace($marcador, $kotlinBlock + "`r`n" + $marcador)
}
if ($texto.Contains('val composeBom = platform(') -or $texto.Contains('kotlinOptions { jvmTarget')) {
    throw 'No se pudieron corregir todas las instrucciones de Gradle; escribe antes de continuar.'
}
[System.IO.File]::WriteAllText($archivo, $texto, (New-Object System.Text.UTF8Encoding($false)))
Write-Host '[1/2] Corregido app/build.gradle.kts' -ForegroundColor Green

$gradleExe = Join-Path $PSScriptRoot '.bootstrap\gradle-8.13\bin\gradle.bat'
if (-not (Test-Path -LiteralPath $gradleExe)) {
    Write-Warning 'No encontré la copia descargada de Gradle 8.13 en .bootstrap. Envía una captura para continuar.'
    exit 1
}

# Generamos el wrapper en un proyecto vacío; evitamos evaluar FarmApp antes de abrirlo en Android Studio.
$temporal = Join-Path $PSScriptRoot '.bootstrap\wrapper-limpio'
New-Item -ItemType Directory -Force -Path $temporal | Out-Null
[System.IO.File]::WriteAllText((Join-Path $temporal 'settings.gradle.kts'), 'rootProject.name = "FarmAppWrapperHelper"', (New-Object System.Text.UTF8Encoding($false)))
[System.IO.File]::WriteAllText((Join-Path $temporal 'build.gradle.kts'), '', (New-Object System.Text.UTF8Encoding($false)))
Push-Location $temporal
try {
    & $gradleExe wrapper --gradle-version 8.13 --distribution-type bin
    if ($LASTEXITCODE -ne 0) { throw 'Falló la generación del wrapper de Gradle.' }
} finally {
    Pop-Location
}
$destino = Join-Path $PSScriptRoot 'gradle\wrapper'
New-Item -ItemType Directory -Force -Path $destino | Out-Null
Copy-Item -LiteralPath (Join-Path $temporal 'gradlew.bat') -Destination (Join-Path $PSScriptRoot 'gradlew.bat') -Force
Copy-Item -LiteralPath (Join-Path $temporal 'gradlew') -Destination (Join-Path $PSScriptRoot 'gradlew') -Force
Copy-Item -LiteralPath (Join-Path $temporal 'gradle\wrapper\gradle-wrapper.jar') -Destination (Join-Path $destino 'gradle-wrapper.jar') -Force
Copy-Item -LiteralPath (Join-Path $temporal 'gradle\wrapper\gradle-wrapper.properties') -Destination (Join-Path $destino 'gradle-wrapper.properties') -Force
Write-Host '[2/2] Gradle Wrapper generado (gradlew.bat y gradle-wrapper.jar).' -ForegroundColor Green
Write-Host 'LISTO: abre la carpeta FarmApp desde Android Studio > File > Open.' -ForegroundColor Green
