<#
.SYNOPSIS
  Actualiza los 4 archivos del Gradle Wrapper a 7.6.4 y valida JDK 11.
.DESCRIPTION
  Ejecutar DESPUES de copiar el archivo gradle-wrapper.properties de este parche.
  Requiere conexion a Internet la primera vez para descargar Gradle 7.6.4.
.PARAMETER JdkHome
  Ruta de la carpeta raiz del JDK 11 (Oracle para cumplimiento literal de la rubrica).
  Puede omitirse si JAVA_HOME ya apunta al JDK 11.
.PARAMETER SoloWrapper
  Regenera el Wrapper y verifica su integridad, sin ejecutar :core:test.
#>
[CmdletBinding()]
param(
    [string]$JdkHome = $env:JAVA_HOME,
    [switch]$SoloWrapper
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Run-Gradle([string]$Description, [string[]]$Arguments) {
    Write-Host "`n==> $Description" -ForegroundColor Cyan
    & $script:Gradlew @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Fallo: $Description (codigo $LASTEXITCODE)."
    }
}

try {
    if ($env:OS -ne 'Windows_NT') {
        throw 'Este asistente esta preparado para Windows. En macOS/Linux usa ./gradlew.'
    }
    $root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
    $script:Gradlew = Join-Path $root 'gradlew.bat'
    $properties = Join-Path $root 'gradle\wrapper\gradle-wrapper.properties'
    $wrapperJar = Join-Path $root 'gradle\wrapper\gradle-wrapper.jar'
    foreach ($file in @($script:Gradlew, $properties, $wrapperJar)) {
        if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
            throw "Falta $file. Extrae el parche sobre el repositorio completo."
        }
    }

    $config = [System.IO.File]::ReadAllText($properties)
    if ($config -notmatch 'gradle-7\.6\.4-bin\.zip') {
        throw 'No se detecto Gradle 7.6.4. Reemplaza primero gradle-wrapper.properties con el de este parche.'
    }

    if (-not [string]::IsNullOrWhiteSpace($JdkHome)) {
        $JdkHome = [System.IO.Path]::GetFullPath($JdkHome.Trim('"'))
        $javaExe = Join-Path $JdkHome 'bin\java.exe'
        $javacExe = Join-Path $JdkHome 'bin\javac.exe'
        if (-not (Test-Path -LiteralPath $javaExe -PathType Leaf) -or
            -not (Test-Path -LiteralPath $javacExe -PathType Leaf)) {
            throw "JdkHome incorrecto: $JdkHome. Debe ser la carpeta que contiene bin, lib y conf."
        }
        $env:JAVA_HOME = $JdkHome
        $env:Path = (Join-Path $JdkHome 'bin') + ';' + $env:Path
    }

    $java = Get-Command 'java.exe' -ErrorAction SilentlyContinue
    if ($null -eq $java) {
        throw 'No se encontro Java. Configura JAVA_HOME o usa -JdkHome "C:\ruta\jdk-11".'
    }
    $javaVersion = & $java.Source --version | Out-String
    # Java 11 publica --version en stdout, evitando tratamiento de stderr
    # en Windows PowerShell 5.1 con ErrorActionPreference Stop.
    Write-Host "Java detectado:`n$($javaVersion.Trim())"
    if ($javaVersion -notmatch '(?m)^(?:openjdk|java)\s+(?:version\s+"?)?11(?:[.\+"\s]|$)') {
        throw 'Para validar la entrega academica usa JDK 11. Puedes generar la portable con JDK 17 por separado.'
    }

    Push-Location $root
    try {
        # Segun la documentacion oficial de Gradle, ejecutar wrapper dos veces
        # garantiza que se actualicen tambien gradle-wrapper.jar y gradlew(.bat).
        Run-Gradle 'Generar Gradle Wrapper 7.6.4 (primera pasada)' @('wrapper', '--gradle-version', '7.6.4', '--distribution-type', 'bin', '--no-daemon')
        Run-Gradle 'Actualizar tambien JAR y scripts del Wrapper (segunda pasada)' @('wrapper', '--gradle-version', '7.6.4', '--distribution-type', 'bin', '--no-daemon')

        # Verifica el binario contra el checksum oficial publicado en gradle.org.
        $expectedJar = '14dfa961b6704bb3decdea06502781edaa796a82e6da41cd2e1962b14fbe21a3'
        $actualJar = (Get-FileHash -LiteralPath $wrapperJar -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($actualJar -ne $expectedJar) {
            throw "El SHA256 del gradle-wrapper.jar no coincide con Gradle 7.6.4. Se obtuvo $actualJar. No subas un Wrapper sin verificar."
        }
        Write-Host 'Checksum del Wrapper oficial 7.6.4: OK' -ForegroundColor Green

        # El task wrapper puede reescribir las propiedades. Asegura un SHA de distribucion fijo.
        $shaDist = 'bed1da33cca0f557ab13691c77f38bb67388119e4794d113e051039b80af9bb1'
        $p = [System.IO.File]::ReadAllText($properties)
        if ($p -notmatch '(?m)^distributionSha256Sum=') {
            [System.IO.File]::AppendAllText($properties, "distributionSha256Sum=$shaDist`n")
        }
        elseif ($p -notmatch [regex]::Escape("distributionSha256Sum=$shaDist")) {
            throw 'El checksum de la distribucion fue alterado. Comprueba gradle-wrapper.properties.'
        }

        Run-Gradle 'Confirmar version de Gradle y JVM' @('--version')
        if (-not $SoloWrapper) {
            Run-Gradle 'Pruebas JUnit de core' @(':core:test', '--no-daemon')
            Run-Gradle 'Compilar lanzador de escritorio' @(':lwjgl3:classes', '--no-daemon')
        }

        Write-Host "`nLISTO: Gradle 7.6.4 con JDK 11." -ForegroundColor Green
        Write-Host 'Ahora puedes probar el juego con: .\gradlew.bat :lwjgl3:run'
        Write-Host 'El generador portable de GitHub sigue usando JDK 17 con el mismo codigo fuente.'
        Write-Host 'Revisa los archivos del Wrapper modificados antes de subirlos a GitHub.'
    }
    finally {
        Pop-Location
    }
}
catch {
    Write-Host "`nERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
