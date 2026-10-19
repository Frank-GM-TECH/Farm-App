# FarmApp - Reparacion minima de compilacion androidTest (HU01)
# Ejecutar desde la raiz del proyecto con:
# powershell -ExecutionPolicy Bypass -File .\Reparar_Pruebas_Android_FarmApp.ps1
# No modifica app/src/main, Firebase ni Gradle.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$tests = Join-Path $root 'app\src\androidTest\java'
$theme = Join-Path $tests 'com\example\farmapp\ui\theme\ThemeModeSelectorTest.kt'
if (-not (Test-Path (Join-Path $root 'settings.gradle.kts')) -or -not (Test-Path $tests)) {
    Write-Host 'ERROR: Guarda este script en la carpeta principal FarmApp (donde esta settings.gradle.kts).' -ForegroundColor Red
    exit 1
}
$backup = Join-Path $root 'app\build\respaldo_reparacion_androidTest'
New-Item -ItemType Directory -Force -Path $backup | Out-Null
$utf8 = New-Object System.Text.UTF8Encoding($false)
$changed = 0
$removed = 0
$themeChanged = $false

Get-ChildItem -Path $tests -Recurse -Filter '*.kt' -File | ForEach-Object {
    $file = $_.FullName
    $text = [System.IO.File]::ReadAllText($file)
    $pattern = '(?m)^[ \t]*import androidx\.compose\.ui\.test\.(?:assertExists|assertDoesNotExist)[ \t]*(?:\r?\n|$)'
    $matches = [regex]::Matches($text, $pattern)
    if ($matches.Count -gt 0) {
        $relative = $file.Substring($tests.Length).TrimStart([char]'\', [char]'/')
        $copyTo = Join-Path $backup $relative
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $copyTo) | Out-Null
        Copy-Item -LiteralPath $file -Destination $copyTo -Force
        $newText = [regex]::Replace($text, $pattern, '')
        [System.IO.File]::WriteAllText($file, $newText, $utf8)
        $removed += $matches.Count
        $changed++
    }
}

if (-not (Test-Path $theme)) {
    Write-Host 'AVISO: No se encontro ThemeModeSelectorTest.kt; ese error podria continuar.' -ForegroundColor Yellow
} else {
    $text = [System.IO.File]::ReadAllText($theme)
    $old = 'SideEffect { backgroundObserved = MaterialTheme.colorScheme.background }'
    if ($text.Contains($old)) {
        $relative = $theme.Substring($tests.Length).TrimStart([char]'\', [char]'/')
        $copyTo = Join-Path $backup $relative
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $copyTo) | Out-Null
        if (-not (Test-Path $copyTo)) { Copy-Item -LiteralPath $theme -Destination $copyTo }
        $newline = if ($text.Contains("`r`n")) { "`r`n" } else { "`n" }
        $new = 'val currentBackground = MaterialTheme.colorScheme.background' + $newline + (' ' * 16) + 'SideEffect { backgroundObserved = currentBackground }'
        $text = $text.Replace($old, $new)
        [System.IO.File]::WriteAllText($theme, $text, $utf8)
        $themeChanged = $true
        $changed++
    } elseif ($text.Contains('SideEffect { backgroundObserved = currentBackground }')) {
        Write-Host 'ThemeModeSelectorTest.kt ya parece estar corregido.' -ForegroundColor DarkGreen
    } else {
        Write-Host 'AVISO: El formato de ThemeModeSelectorTest.kt es distinto; revisa la linea 30 manualmente.' -ForegroundColor Yellow
    }
}

Write-Host "Imports invalidos eliminados: $removed" -ForegroundColor Green
Write-Host "Archivos actualizados: $changed" -ForegroundColor Green
Write-Host "Correccion de SideEffect aplicada: $themeChanged" -ForegroundColor Green
Write-Host "Respaldo: $backup"
Write-Host 'Listo. Ejecuta: .\gradlew.bat :app:connectedDebugAndroidTest' -ForegroundColor Cyan
