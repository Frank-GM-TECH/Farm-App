# Ejecutar una vez, desde PowerShell, dentro de la carpeta FarmApp.
# Descarga Gradle exclusivamente desde services.gradle.org y genera el wrapper oficial.
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
$version = "8.13"
$base = Join-Path $PSScriptRoot ".bootstrap"
$zip = Join-Path $base "gradle-$version-bin.zip"
$folder = Join-Path $base "gradle-$version"
New-Item -ItemType Directory -Force $base | Out-Null
if (!(Test-Path (Join-Path $folder "bin/gradle.bat"))) {
    $uri = "https://services.gradle.org/distributions/gradle-$version-bin.zip"
    Write-Host "Descargando Gradle $version desde la web oficial..."
    Invoke-WebRequest -Uri $uri -OutFile $zip
    Expand-Archive -Path $zip -DestinationPath $base -Force
}
Write-Host "Generando el Gradle Wrapper oficial..."
& (Join-Path $folder "bin/gradle.bat") wrapper --gradle-version $version --distribution-type bin
if ($LASTEXITCODE -ne 0) { throw "Falló la creación del wrapper; verifica SDK Android e Internet." }
Write-Host "Listo. Ahora abre esta carpeta desde Android Studio y sincroniza Gradle."
