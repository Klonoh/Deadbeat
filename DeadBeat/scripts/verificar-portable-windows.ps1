<#
.SYNOPSIS
  Valida el ZIP portable de Deadbeat despues de construirlo.
.DESCRIPTION
  Comprueba el lanzador .exe, runtime Java 17, bibliotecas LWJGL3/LibGDX,
  clase principal y recursos dentro del JAR; verifica ademas que los archivos
  necesarios esten presentes en el ZIP final, no solo en la carpeta temporal.
  No arranca la ventana del juego: ese test es manual en una PC Windows.
.PARAMETER ZipPath
  Ruta opcional del ZIP. Por defecto: DeadBeat/distribucion/Deadbeat-Windows-Portable.zip
#>
[CmdletBinding()]
param([string]$ZipPath = '')

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Asegurar([bool]$Condicion, [string]$Mensaje) {
    if (-not $Condicion) { throw $Mensaje }
}

function Abrir-Zip([string]$Ruta) {
    return [System.IO.Compression.ZipFile]::OpenRead($Ruta)
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
    $java = Join-Path $carpetaApp 'runtime\bin\java.exe'
    $release = Join-Path $carpetaApp 'runtime\release'
    $app = Join-Path $carpetaApp 'app'
    $assets = Join-Path $app 'deadbeat-assets.jar'
    foreach ($requerido in @($exe, $java, $release, $assets)) {
        Asegurar (Test-Path -LiteralPath $requerido -PathType Leaf) "Falta archivo del portable: $requerido"
    }
    Asegurar (Test-Path -LiteralPath $ZipPath -PathType Leaf) "Falta ZIP final: $ZipPath"
    Asegurar ((Get-Item -LiteralPath $ZipPath).Length -gt 1000000) 'El ZIP es demasiado pequeno para un juego con runtime Java.'

    $version = (& $java --version | Out-String)
    Asegurar ($LASTEXITCODE -eq 0) 'No se pudo ejecutar el Java incluido en la distribucion.'
    Asegurar ($version -match '(?m)^(?:openjdk|java)\s+17(?:[.\s+\-]|$)') "El runtime integrado no es Java 17: $version"
    Write-Host "Runtime incluido OK: $($version.Trim())" -ForegroundColor Green

    $jars = @(Get-ChildItem -LiteralPath $app -Filter '*.jar' -File)
    $lwjgl = @($jars | Where-Object { $_.Name -match '^lwjgl3(?:-|\.jar)' } | Select-Object -First 1)
    $core = @($jars | Where-Object { $_.Name -match '^core(?:-|\.jar)' } | Select-Object -First 1)
    $backend = @($jars | Where-Object { $_.Name -match '^gdx-backend-lwjgl3-' })
    Asegurar ($jars.Count -ge 4) 'Faltan bibliotecas en la carpeta app.'
    Asegurar ($lwjgl.Count -eq 1) 'No esta el JAR del lanzador lwjgl3.'
    Asegurar ($core.Count -eq 1) 'No esta el JAR del modulo core.'
    Asegurar ($backend.Count -ge 1) 'Falta la biblioteca gdx-backend-lwjgl3.'

    $z = Abrir-Zip $lwjgl[0].FullName
    try {
        $nombres = Obtener-Nombres $z
        Asegurar ($nombres -contains 'com/deadbeat/lwjgl3/Lwjgl3Launcher.class') 'El JAR del lanzador no contiene Lwjgl3Launcher.class.'
    } finally { $z.Dispose() }
    $z = Abrir-Zip $core[0].FullName
    try {
        $nombres = Obtener-Nombres $z
        Asegurar ($nombres -contains 'com/deadbeat/DeadbeatGame.class') 'El JAR core no contiene DeadbeatGame.class.'
    } finally { $z.Dispose() }

    $z = Abrir-Zip $assets
    try {
        $nombres = Obtener-Nombres $z
        foreach ($requerido in @('sprites/player_walk.png', 'music/tracklist.properties', 'world2/surfaces/asphalt.png')) {
            Asegurar ($nombres -contains $requerido) "Recurso faltante en deadbeat-assets.jar: $requerido"
        }
    } finally { $z.Dispose() }
    Write-Host 'Clases, bibliotecas y recursos del juego: OK' -ForegroundColor Green

    $z = Abrir-Zip $ZipPath
    try {
        $nombres = Obtener-Nombres $z
        $requeridos = @(
            'Deadbeat/Deadbeat.exe',
            'Deadbeat/runtime/bin/java.exe',
            'Deadbeat/runtime/release',
            'Deadbeat/app/deadbeat-assets.jar',
            "Deadbeat/app/$($lwjgl[0].Name)",
            "Deadbeat/app/$($core[0].Name)"
        )
        foreach ($requerido in $requeridos) {
            Asegurar ($nombres -contains $requerido) "El ZIP final no contiene $requerido"
        }
    } finally { $z.Dispose() }

    $bytes = (Get-Item -LiteralPath $ZipPath).Length
    Write-Host "ZIP verificado: $ZipPath ($bytes bytes)" -ForegroundColor Green
    Write-Host 'La comprobacion estructural fue exitosa. Falta probar abrir Deadbeat.exe en otra PC.'
} catch {
    Write-Host "ERROR DE VERIFICACION: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
