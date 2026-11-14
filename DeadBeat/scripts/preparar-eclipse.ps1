<#
.SYNOPSIS
  Prepara Deadbeat para Eclipse con JDK 11, sin modificar el codigo del juego.
.DESCRIPTION
  Usa Gradle 7.6.4, genera .project/.classpath/.settings, compila el lanzador
  utilizando unicamente las tareas de compilacion del proyecto.
.PARAMETER JdkHome
  Carpeta del JDK 11 que contiene bin\java.exe, bin\javac.exe y el archivo release.
  Para cumplir literalmente la rubrica se requiere Oracle JDK 11.
.PARAMETER ExigirOracle
  Detiene la preparacion cuando el JDK 11 no es Oracle Corporation.
#>
[CmdletBinding()]
param(
    [string]$JdkHome = $env:JAVA_HOME,
    [switch]$ExigirOracle
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Ejecutar([string]$Descripcion, [string[]]$Argumentos) {
    Write-Host "`n==> $Descripcion" -ForegroundColor Cyan
    & $script:Gradlew @Argumentos
    if ($LASTEXITCODE -ne 0) {
        throw "Fallo $Descripcion (codigo de salida $LASTEXITCODE)."
    }
}

try {
    if ($env:OS -ne 'Windows_NT') {
        throw 'Este asistente .ps1 esta preparado para Windows y Eclipse.'
    }
    $root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
    $script:Gradlew = Join-Path $root 'gradlew.bat'
    if (-not (Test-Path -LiteralPath $script:Gradlew -PathType Leaf)) {
        throw 'Falta gradlew.bat: copia el parche sobre la carpeta DeadBeat completa.'
    }
    if ([string]::IsNullOrWhiteSpace($JdkHome)) {
        throw 'Indica Oracle JDK 11 mediante JAVA_HOME o -JdkHome "C:\ruta\jdk-11".'
    }
    $JdkHome = [IO.Path]::GetFullPath($JdkHome.Trim('"'))
    $javaExe = Join-Path $JdkHome 'bin\java.exe'
    $javacExe = Join-Path $JdkHome 'bin\javac.exe'
    foreach ($file in @($javaExe, $javacExe)) {
        if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
            throw "No se encontro $file. -JdkHome debe apuntar a la raiz de un JDK completo."
        }
    }

    $version = (& $javaExe --version | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or
        $version -notmatch '(?m)^(?:java|openjdk)\s+11(?:[.\+\-\s]|$)') {
        throw "Para la edicion academica debes seleccionar JDK 11. Detectado: $version"
    }
    Write-Host "Java detectado: $version" -ForegroundColor Green

    $releaseFile = Join-Path $JdkHome 'release'
    $vendor = 'No identificado'
    if (Test-Path -LiteralPath $releaseFile -PathType Leaf) {
        $releaseInfo = [IO.File]::ReadAllText($releaseFile)
        $matched = [regex]::Match($releaseInfo, '(?m)^IMPLEMENTOR="([^"]+)"')
        if ($matched.Success) { $vendor = $matched.Groups[1].Value }
    }
    $esOracle = $vendor -eq 'Oracle Corporation'
    Write-Host "Distribucion JDK: $vendor"
    if (-not $esOracle) {
        if ($ExigirOracle) {
            throw 'La rubrica exige Oracle JDK 11. Instala Oracle JDK 11 y repite con -ExigirOracle.'
        }
        Write-Warning 'Este JDK puede servir para desarrollo, pero para cumplimiento literal verifica con Oracle JDK 11 (-ExigirOracle).'
    }

    $wrapperProperties = Join-Path $root 'gradle\wrapper\gradle-wrapper.properties'
    if (-not (Test-Path -LiteralPath $wrapperProperties -PathType Leaf) -or
        ([IO.File]::ReadAllText($wrapperProperties) -notmatch 'gradle-7\.6\.4-bin\.zip')) {
        throw 'No se detecto el Gradle Wrapper 7.6.4 de la Fase 1. Aplica primero el Paso 1.'
    }
    $gradleFile = [IO.File]::ReadAllText((Join-Path $root 'build.gradle'))
    if ($gradleFile -notmatch "id\s+'eclipse'") {
        throw 'Falta el build.gradle actualizado del parche: no esta habilitado el plugin eclipse.'
    }

    # La seleccion del JDK no cambia las variables globales del usuario.
    $env:JAVA_HOME = $JdkHome
    $env:Path = (Join-Path $JdkHome 'bin') + ';' + $env:Path

    Push-Location $root
    try {
        Ejecutar 'Confirmar Gradle 7.6.4 y Java 11' @('--version')
        Ejecutar 'Generar proyectos de Eclipse y compilar desktop' @('eclipse', ':lwjgl3:classes', '--no-daemon')
        foreach ($relative in @(
            '.project',
            'core\.project', 'core\.classpath', 'core\.settings\org.eclipse.jdt.core.prefs',
            'lwjgl3\.project', 'lwjgl3\.classpath', 'lwjgl3\.settings\org.eclipse.jdt.core.prefs',
            'Deadbeat-Eclipse.launch'
        )) {
            $path = Join-Path $root $relative
            if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
                throw "Gradle termino pero falta archivo de Eclipse: $relative"
            }
        }

        # Validar los nombres usados en la configuracion de lanzamiento.
        [xml]$coreProject = Get-Content -LiteralPath (Join-Path $root 'core\.project') -Raw
        [xml]$desktopProject = Get-Content -LiteralPath (Join-Path $root 'lwjgl3\.project') -Raw
        if ($coreProject.projectDescription.name -ne 'Deadbeat-core' -or
            $desktopProject.projectDescription.name -ne 'Deadbeat-lwjgl3') {
            throw 'Los nombres generados de Eclipse no coinciden con Deadbeat-core y Deadbeat-lwjgl3.'
        }

        Write-Host "`nLISTO: Eclipse puede importar Deadbeat, Deadbeat-core y Deadbeat-lwjgl3." -ForegroundColor Green
        Write-Host 'Abre Eclipse -> File -> Import -> General -> Existing Projects into Workspace.'
        Write-Host "Selecciona esta carpeta: $root"
        Write-Host 'En Eclipse, configura JDK 11 y ejecuta Deadbeat-Eclipse.launch.'
        if (-not $esOracle) {
            Write-Warning 'VALIDACION ACADEMICA PENDIENTE: comprobar nuevamente con Oracle JDK 11.'
        }
        Write-Host 'El empaquetador Windows (JDK 17) no ha sido modificado.'
    }
    finally { Pop-Location }
}
catch {
    Write-Host "`nERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
