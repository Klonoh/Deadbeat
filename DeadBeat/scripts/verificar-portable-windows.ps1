<#
.SYNOPSIS
  Verifica la distribucion portable de Deadbeat para Windows.
.DESCRIPTION
  jpackage usa jlink con --strip-native-commands, de modo que runtime/bin/java.exe
  puede no existir. La JVM se valida por runtime/release y las bibliotecas
  nativas, ademas de comprobar el contenido del ZIP final.
.PARAMETER ZipPath
  ZIP portable a verificar (por defecto: distribucion/Deadbeat-Windows-Portable.zip).
#>
[CmdletBinding()]
param([string]$ZipPath = '')

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Asegurar([bool]$Condicion, [string]$Mensaje) {
    if (-not $Condicion) { throw $Mensaje }
}

function Obtener-Nombres($Archivo) {
    return @($Archivo.Entries | ForEach-Object { $_.FullName.Replace('\', '/') })
}

try {
    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem

    $root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
    $carpetaApp = Join-Path $root 'distribucion\Deadbeat'
    if ([string]::IsNullOrWhiteSpace($ZipPath)) {
        $ZipPath = Join-Path $root 'distribucion\Deadbeat-Windows-Portable.zip'
    }
    $ZipPath = [System.IO.Path]::GetFullPath($ZipPath.Trim('"'))

    $exe = Join-Path $carpetaApp 'Deadbeat.exe'
    $runtimeRelease = Join-Path $carpetaApp 'runtime\release'
    $javaDll = Join-Path $carpetaApp 'runtime\bin\java.dll'
    $jvmDll = Join-Path $carpetaApp 'runtime\bin\server\jvm.dll'
    $app = Join-Path $carpetaApp 'app'
    $assets = Join-Path $app 'deadbeat-assets.jar'
    $launcherConfig = Join-Path $app 'Deadbeat.cfg'

    foreach ($file in @($exe, $runtimeRelease, $javaDll, $jvmDll, $assets, $launcherConfig)) {
        Asegurar (Test-Path -LiteralPath $file -PathType Leaf) "Falta archivo de la distribucion: $file"
    }
    Asegurar (Test-Path -LiteralPath $ZipPath -PathType Leaf) "No se encontro el ZIP: $ZipPath"
    Asegurar ((Get-Item -LiteralPath $ZipPath).Length -gt 1000000) 'ZIP demasiado pequeno para contener juego y runtime.'

    # jpackage puede omitir java.exe: al ejecutar un lanzador nativo, carga
    # jvm.dll directamente. El archivo release identifica el runtime incluido.
    $releaseContent = [System.IO.File]::ReadAllText($runtimeRelease)
    $match = [regex]::Match($releaseContent, '(?m)^JAVA_VERSION="([^"]+)"\s*$')
    Asegurar $match.Success 'No se pudo determinar JAVA_VERSION en runtime/release.'
    $version = $match.Groups[1].Value
    Asegurar ($version -match '^17(?:[.\+\-]|$)') "Se esperaba Java 17 incluido, detectado: $version"
    Write-Host "Runtime Java ${version}: OK (java.dll y jvm.dll encontrados)" -ForegroundColor Green

    $jars = @(Get-ChildItem -LiteralPath $app -File -Filter '*.jar')
    $lwjgl = @($jars | Where-Object { $_.Name -match '^lwjgl3(?:-|\.jar)' } | Select-Object -First 1)
    $core = @($jars | Where-Object { $_.Name -match '^core(?:-|\.jar)' } | Select-Object -First 1)
    $backend = @($jars | Where-Object { $_.Name -match '^gdx-backend-lwjgl3-' })
    Asegurar ($jars.Count -ge 4) 'Faltan bibliotecas Java en app/.'
    Asegurar ($lwjgl.Count -eq 1) 'Falta JAR del lanzador lwjgl3.'
    Asegurar ($core.Count -eq 1) 'Falta JAR del modulo core.'
    Asegurar ($backend.Count -ge 1) 'Falta gdx-backend-lwjgl3.'

    $z = [System.IO.Compression.ZipFile]::OpenRead($lwjgl[0].FullName)
    try {
        $nombres = Obtener-Nombres $z
        Asegurar ($nombres -contains 'com/deadbeat/lwjgl3/Lwjgl3Launcher.class') 'Falta Lwjgl3Launcher.class.'
    } finally { $z.Dispose() }

    $z = [System.IO.Compression.ZipFile]::OpenRead($core[0].FullName)
    try {
        $nombres = Obtener-Nombres $z
        Asegurar ($nombres -contains 'com/deadbeat/DeadbeatGame.class') 'Falta DeadbeatGame.class.'
    } finally { $z.Dispose() }

    $z = [System.IO.Compression.ZipFile]::OpenRead($assets)
    try {
        $nombres = Obtener-Nombres $z
        foreach ($recurso in @('sprites/player_walk.png', 'music/tracklist.properties', 'world2/surfaces/asphalt.png')) {
            Asegurar ($nombres -contains $recurso) "Falta recurso en deadbeat-assets.jar: $recurso"
        }
    } finally { $z.Dispose() }
    Write-Host 'Lanzador, clases, dependencias y recursos: OK' -ForegroundColor Green

    $z = [System.IO.Compression.ZipFile]::OpenRead($ZipPath)
    try {
        $nombres = Obtener-Nombres $z
        $requeridos = @(
            'Deadbeat/Deadbeat.exe',
            'Deadbeat/runtime/release',
            'Deadbeat/runtime/bin/java.dll',
            'Deadbeat/runtime/bin/server/jvm.dll',
            'Deadbeat/app/Deadbeat.cfg',
            'Deadbeat/app/deadbeat-assets.jar',
            "Deadbeat/app/$($lwjgl[0].Name)",
            "Deadbeat/app/$($core[0].Name)"
        )
        foreach ($requerido in $requeridos) {
            Asegurar ($nombres -contains $requerido) "Falta archivo dentro del ZIP final: $requerido"
        }
    } finally { $z.Dispose() }

    Write-Host "ZIP portable verificado: $ZipPath" -ForegroundColor Green
    Write-Host 'IMPORTANTE: la verificacion estructural no reemplaza abrir Deadbeat.exe y jugar en Windows.'
} catch {
    Write-Host "ERROR DE VERIFICACION: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
