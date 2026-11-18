# FarmApp: prueba de navegación y chip SQL, después de ejecutar 104 instrumentadas.
# Este script modifica ÚNICAMENTE 2 archivos Kotlin de app/src/androidTest.
# No modifica el código de producción, Gradle ni Firebase.
$ErrorActionPreference = 'Stop'

$project = (Get-Location).Path
$testsRoot = Join-Path $project 'app\src\androidTest\java\com\example\farmapp\ui'
$navigation = Join-Path $testsRoot 'navigation\FarmAppNavigationTest.kt'
$sql = Join-Path $testsRoot 'sql\SqlLabScreenTest.kt'

if (-not (Test-Path (Join-Path $project 'gradlew.bat'))) {
    throw 'Abre PowerShell desde la carpeta raiz de FarmApp (donde esta gradlew.bat).'
}
foreach ($f in @($navigation, $sql)) {
    if (-not (Test-Path $f)) { throw "Falta el archivo de prueba: $f" }
}

$backup = Join-Path $env:TEMP ('FarmApp_2Tests_Backup_' + (Get-Date -Format 'yyyyMMdd_HHmmss'))
New-Item -ItemType Directory -Path $backup -Force | Out-Null
Copy-Item $navigation (Join-Path $backup 'FarmAppNavigationTest.kt')
Copy-Item $sql (Join-Path $backup 'SqlLabScreenTest.kt')

$utf8 = New-Object System.Text.UTF8Encoding($false)

# 1) Login: el boton puede quedar por debajo del viewport del Samsung.
# Se modifica SOLO la asercion del test de cerrar sesion.
$n = [System.IO.File]::ReadAllText($navigation)
$methodToken = '@Test fun cerrarSesionLimpiaNavegacionProtegida()'
$methodPos = $n.IndexOf($methodToken)
if ($methodPos -lt 0) { throw 'No se encontro cerrarSesionLimpiaNavegacionProtegida; no se altero ese archivo.' }
$head = $n.Substring(0, $methodPos)
$tail = $n.Substring($methodPos)
$oldNav = 'rule.onNodeWithTag("auth_submit_button").assertIsDisplayed()'
$newNav = 'rule.onNodeWithTag("auth_submit_button").performScrollTo().assertIsDisplayed()'
if (-not $tail.Contains($newNav)) {
    if (-not $tail.Contains($oldNav)) { throw 'No se encontro la asercion esperada del test de navegacion.' }
    $tail = $tail.Replace($oldNav, $newNav)
}
$n = $head + $tail
if (-not $n.Contains('import androidx.compose.ui.test.performScrollTo' + "`n") -and
    -not $n.Contains('import androidx.compose.ui.test.performScrollTo' + "`r`n")) {
    $n = $n.Replace('import androidx.compose.ui.test.performClick', "import androidx.compose.ui.test.performScrollTo`nimport androidx.compose.ui.test.performClick")
}
[System.IO.File]::WriteAllText($navigation, $n, $utf8)
Write-Host '[1/2] Navigation: el boton se desplaza a la zona visible antes de comprobarse.' -ForegroundColor Green

# 2) SQL: los FilterChip estan dentro de un Row con horizontalScroll.
# Un gesto sobre el chip puede caer fuera de la zona visible del dispositivo.
$s = [System.IO.File]::ReadAllText($sql)
$oldSql = 'composeRule.onNodeWithTag("sql_query_NAME_LIKE").performClick()'
$newSql = 'composeRule.onNodeWithTag("sql_query_NAME_LIKE").performScrollTo().performClick()'
if (-not $s.Contains($newSql)) {
    if (-not $s.Contains($oldSql)) { throw 'No se encontro el click esperado del test SQL.' }
    $s = $s.Replace($oldSql, $newSql)
}
if (-not $s.Contains('import androidx.compose.ui.test.performScrollTo' + "`n") -and
    -not $s.Contains('import androidx.compose.ui.test.performScrollTo' + "`r`n")) {
    $s = $s.Replace('import androidx.compose.ui.test.performClick', "import androidx.compose.ui.test.performScrollTo`nimport androidx.compose.ui.test.performClick")
}
[System.IO.File]::WriteAllText($sql, $s, $utf8)
Write-Host '[2/2] SQL: el chip se desplaza a la zona visible antes del click.' -ForegroundColor Green

Write-Host "Respaldo externo al repositorio: $backup" -ForegroundColor Cyan
Write-Host 'Ahora ejecuta: .\gradlew.bat :app:connectedDebugAndroidTest' -ForegroundColor Cyan
Write-Host 'Esta correccion requiere ejecucion en Android: aun no se ha validado en un dispositivo.' -ForegroundColor Yellow
