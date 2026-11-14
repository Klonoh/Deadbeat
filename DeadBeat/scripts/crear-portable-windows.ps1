<#
.SYNOPSIS
  Construye Deadbeat para Windows como una carpeta portable con Java integrado.
.DESCRIPTION
  Usa el Gradle Wrapper real del repositorio (proyecto lwjgl3), crea un JAR
  de recursos en el classpath y genera Deadbeat.exe + runtime con jpackage.
  No requiere instalar Java en los equipos donde se va a jugar.
.PARAMETER JdkHome
  Carpeta raiz de Temurin JDK 17, con bin\java.exe y bin\jpackage.exe.
#>
[CmdletBinding()]
param(
    [string]$JdkHome = $env:JAVA_HOME
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Die([string]$Message) {
    throw $Message
}
function Run-External([string]$Title, [string]$Exe, [string[]]$Arguments) {
    Write-Host "`n==> $Title" -ForegroundColor Cyan
    & $Exe @Arguments
    if ($LASTEXITCODE -ne 0) {
        Die "Fallo '$Title' (codigo de salida $LASTEXITCODE)."
    }
}

try {
    if ($env:OS -ne 'Windows_NT') {
        Die 'Este paquete de Windows debe construirse en Windows (o en GitHub Actions con windows-latest).'
    }

    $root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
    $gradlew = Join-Path $root 'gradlew.bat'
    $assets = Join-Path $root 'assets'
    if (-not (Test-Path -LiteralPath $gradlew -PathType Leaf)) {
        Die "No se encontro el Gradle Wrapper en $gradlew. Mantenga la estructura original del repositorio."
    }
    if (-not (Test-Path -LiteralPath (Join-Path $assets 'sprites\player_walk.png') -PathType Leaf)) {
        Die "Falta assets\sprites\player_walk.png. Use el repositorio completo, no solo los archivos Java."
    }
    if (-not (Test-Path -LiteralPath (Join-Path $assets 'music\tracklist.properties') -PathType Leaf)) {
        Die 'Falta assets\music\tracklist.properties.'
    }

    # Sin -JdkHome, detectamos jpackage disponible en PATH.
    if ([string]::IsNullOrWhiteSpace($JdkHome)) {
        $detected = Get-Command 'jpackage.exe' -ErrorAction SilentlyContinue
        if ($null -ne $detected) {
            $JdkHome = Split-Path -Parent (Split-Path -Parent $detected.Source)
        }
    }
    if ([string]::IsNullOrWhiteSpace($JdkHome)) {
        Die 'No se encontro JDK. Instale Temurin JDK 17 y configure JAVA_HOME, o use -JdkHome "C:\ruta\al\jdk-17".'
    }

    $JdkHome = [System.IO.Path]::GetFullPath($JdkHome.Trim('"'))
    $binDir = Join-Path $JdkHome 'bin'
    $javaExe = Join-Path $binDir 'java.exe'
    $jarExe = Join-Path $binDir 'jar.exe'
    $jpackageExe = Join-Path $binDir 'jpackage.exe'
    foreach ($executable in @($javaExe, $jarExe, $jpackageExe)) {
        if (-not (Test-Path -LiteralPath $executable -PathType Leaf)) {
            Die "Falta $executable. Debe indicar la carpeta de un JDK completo, no un JRE."
        }
    }

    # Java 17 escribe "--version" en stdout. Evita el tratamiento especial de
    # stderr en Windows PowerShell 5.1 cuando ErrorActionPreference es Stop.
    $versionOutput = (& $javaExe --version | Out-String)
    if ($LASTEXITCODE -ne 0) { Die 'No se pudo ejecutar Java.' }
    if ($versionOutput -notmatch '(?m)^(?:openjdk|java)\s+17(?:[.\s+\-]|$)') {
        Die "Este empaquetador utiliza JDK 17 para asegurar compatibilidad con Gradle 7.6.4. JDK detectado: $versionOutput"
    }
    Write-Host "JDK seleccionado: $JdkHome" -ForegroundColor Green
    Write-Host $versionOutput.Trim()

    # Fuerza el uso del JDK 17 elegido durante toda esta sesion,
    # incluso si en el equipo hay Java 11 en PATH.
    $env:JAVA_HOME = $JdkHome
    $env:PATH = "$binDir;$env:PATH"

    Push-Location $root
    try {
        Run-External 'Compilar y preparar dependencias de escritorio' $gradlew @(':lwjgl3:installDist', '--no-daemon')

        # El plugin application de Gradle prepara los JAR del juego y de LWJGL3.
        $installedLibs = Join-Path $root 'lwjgl3\build\install\lwjgl3\lib'
        if (-not (Test-Path -LiteralPath $installedLibs -PathType Container)) {
            Die "No se encontro $installedLibs. Compruebe que lwjgl3 aplica el plugin application."
        }

        $working = Join-Path $root 'lwjgl3\build\deadbeat-portable'
        $inputDir = Join-Path $working 'input'
        if (Test-Path -LiteralPath $working) { Remove-Item -LiteralPath $working -Recurse -Force }
        New-Item -ItemType Directory -Force -Path $inputDir | Out-Null

        $libraries = @(Get-ChildItem -LiteralPath $installedLibs -File -Filter '*.jar')
        if ($libraries.Count -lt 3) {
            Die "Se esperaban varios JAR en $installedLibs, pero solo hay $($libraries.Count)."
        }
        foreach ($lib in $libraries) {
            Copy-Item -LiteralPath $lib.FullName -Destination $inputDir
        }

        $launcher = @($libraries | Where-Object { $_.Name -match '^lwjgl3(?:-|\.jar)' }) | Select-Object -First 1
        if ($null -eq $launcher) {
            Die 'No se encontro el JAR del modulo lwjgl3 en la distribucion de Gradle.'
        }

        # En desktop, Gdx.files.internal() busca en el disco y luego en classpath.
        # Por eso los PNG/MP3 van en un JAR independiente, sin cambiar el codigo.
        $assetsJar = Join-Path $inputDir 'deadbeat-assets.jar'
        Run-External 'Empaquetar sprites, mapas y musica' $jarExe @('--create', '--file', $assetsJar, '--no-compress', '-C', $assets, '.')
        $entries = @(& $jarExe --list --file $assetsJar)
        foreach ($required in @('sprites/player_walk.png', 'music/tracklist.properties')) {
            if ($entries -notcontains $required) {
                Die "No se incluyo el recurso obligatorio $required en deadbeat-assets.jar."
            }
        }

        $output = Join-Path $root 'distribucion'
        $appImage = Join-Path $output 'Deadbeat'
        $zipFile = Join-Path $output 'Deadbeat-Windows-Portable.zip'
        if (-not (Test-Path -LiteralPath $output)) { New-Item -ItemType Directory -Path $output | Out-Null }
        if (Test-Path -LiteralPath $appImage) { Remove-Item -LiteralPath $appImage -Recurse -Force }
        if (Test-Path -LiteralPath $zipFile) { Remove-Item -LiteralPath $zipFile -Force }

        Run-External 'Generar Deadbeat.exe y Java integrado con jpackage' $jpackageExe @(
            '--type', 'app-image',
            '--name', 'Deadbeat',
            '--input', $inputDir,
            '--main-jar', $launcher.Name,
            '--main-class', 'com.deadbeat.lwjgl3.Lwjgl3Launcher',
            '--dest', $output,
            '--java-options', '-Dfile.encoding=UTF-8',
            '--java-options', '-Xmx1024m'
        )

        $exe = Join-Path $appImage 'Deadbeat.exe'
        $runtime = Join-Path $appImage 'runtime'
        $bundledAssets = Join-Path $appImage 'app\deadbeat-assets.jar'
        foreach ($required in @($exe, $runtime, $bundledAssets)) {
            if (-not (Test-Path -LiteralPath $required)) {
                Die "El empaquetado termino, pero falta $required."
            }
        }

        Write-Host "`n==> Comprimir la distribucion portable" -ForegroundColor Cyan
        Compress-Archive -LiteralPath $appImage -DestinationPath $zipFile -CompressionLevel Optimal -Force
        if (-not (Test-Path -LiteralPath $zipFile -PathType Leaf)) {
            Die "No se pudo crear $zipFile."
        }

        Write-Host "`nLISTO" -ForegroundColor Green
        Write-Host "Carpeta lista para jugar: $appImage"
        Write-Host "ZIP listo para compartir: $zipFile"
        Write-Host 'En otro PC: extraer el ZIP y abrir Deadbeat\Deadbeat.exe. No hay que instalar Java.'
        Write-Host 'Aviso: antes de distribuir publicamente la banda sonora compruebe sus derechos de uso.'
    }
    finally {
        Pop-Location
    }
}
catch {
    Write-Host "`nERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
