<#
.SYNOPSIS
  Restaura la integracion de mejoras de nivel de Deadbeat sin perder los sprites nuevos.
.DESCRIPTION
  Corrige GameWorld.java del commit e13e35b2 (o compatible): 8 errores de
  compilacion porque GameScreen necesita 5 metodos eliminados de GameWorld.
  No modifica GameScreen, imagenes, Gradle, JDK ni el empaquetador portable.
.PARAMETER Probar
  Opcional: despues del arreglo, ejecuta JUnit y compila escritorio con Gradle.
#>
[CmdletBinding()]
param([switch]$Probar)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Cambiar-Exactamente([string]$Original, [string]$Nuevo, [int]$Veces) {
    $encontradas = [regex]::Matches($script:codigo, [regex]::Escape($Original)).Count
    if ($encontradas -ne $Veces) {
        throw "El archivo GameWorld.java ha cambiado: se esperaban $Veces coincidencias del fragmento y se encontraron $encontradas. No se modifico ningun archivo."
    }
    $script:codigo = $script:codigo.Replace($Original, $Nuevo)
}

try {
    $raiz = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
    $archivo = Join-Path $raiz 'core\src\main\java\com\deadbeat\world\GameWorld.java'
    if (-not (Test-Path -LiteralPath $archivo -PathType Leaf)) {
        throw "No se encontro GameWorld.java en $archivo. Extrae el ZIP en la carpeta que contiene DeadBeat."
    }

    $codificacion = New-Object System.Text.UTF8Encoding($false)
    $original = [IO.File]::ReadAllText($archivo, $codificacion)
    $usarCrLf = $original.Contains("`r`n")
    $script:codigo = $original.Replace("`r`n", "`n")

    if ($script:codigo.Contains('public boolean isChoosingUpgrade()') -and
        $script:codigo.Contains('private final UpgradeSelection upgrades = new UpgradeSelection();')) {
        Write-Host 'GameWorld.java ya contiene la integracion de mejoras. No es necesario volver a modificarlo.' -ForegroundColor Green
    } else {
        # Validaciones previas: nunca reescribir una version desconocida o parcialmente modificada.
        if ($script:codigo.Contains('private final UpgradeSelection upgrades = new UpgradeSelection();') -or
            $script:codigo.Contains('public boolean isChoosingUpgrade()')) {
            throw 'Se encontro una correccion parcial. Revisa GameWorld.java antes de ejecutar este parche.'
        }
        if (-not $script:codigo.Contains('drawGuitarProjectile(batch, projectile)') -or
            -not $script:codigo.Contains('drawDrumsticksSpin(batch)')) {
            throw 'No se reconocio la version con los nuevos sprites. Se evita reemplazar el archivo equivocado.'
        }

        Cambiar-Exactamente 'import com.deadbeat.progression.Wave;' @'
import com.deadbeat.progression.Upgrade;
import com.deadbeat.progression.UpgradeSelection;
import com.deadbeat.progression.Wave;
'@ 1

        Cambiar-Exactamente '    private final Wave[] waves;' @'
    private final UpgradeSelection upgrades = new UpgradeSelection();
    private final Wave[] waves;
'@ 1

        Cambiar-Exactamente 'if (!Float.isFinite(delta) || delta <= 0f || !player.isAlive()) {' 'if (!Float.isFinite(delta) || delta <= 0f || !player.isAlive() || isChoosingUpgrade()) {' 2

        Cambiar-Exactamente '                player.addExperience(orb.getAmount());' '                upgrades.award(player.addExperience(orb.getAmount()));' 1

        $apiMejoras = @'
    public boolean isChoosingUpgrade() {
        return upgrades.getPending() > 0;
    }

    public List<Upgrade> getUpgradeChoices() {
        return upgrades.getChoices();
    }

    public int getPendingUpgrades() {
        return upgrades.getPending();
    }

    public void chooseUpgrade(int index) {
        if (player.isAlive() && !(wavesFinished && zombies.isEmpty())) {
            upgrades.choose(index, player);
        }
    }

    public void cancelUpgradeSelection() {
        upgrades.cancel();
    }

'@
        Cambiar-Exactamente '    public List<ExperienceOrb> getOrbs() {' ($apiMejoras + "`n" + '    public List<ExperienceOrb> getOrbs() {') 1

        Cambiar-Exactamente "    public void reset() {`n        player.reset();" "    public void reset() {`n        upgrades.reset();`n        player.reset();" 1

        foreach ($metodo in @('isChoosingUpgrade', 'getUpgradeChoices', 'getPendingUpgrades', 'chooseUpgrade', 'cancelUpgradeSelection')) {
            if (-not $script:codigo.Contains("public ") -or -not $script:codigo.Contains(" $metodo(")) {
                throw "Error interno del parche: falta $metodo. No se guardo ningun cambio."
            }
        }
        if ($usarCrLf) { $script:codigo = $script:codigo.Replace("`n", "`r`n") }
        $respaldo = $archivo + '.antes-de-corregir.bak'
        [IO.File]::Copy($archivo, $respaldo, $true)
        [IO.File]::WriteAllText($archivo, $script:codigo, $codificacion)
        Write-Host 'Sistema de mejoras recuperado en GameWorld.java; nuevos sprites conservados.' -ForegroundColor Green
        Write-Host "Respaldo: $respaldo" -ForegroundColor DarkGray
    }

    if ($Probar) {
        $gradlew = Join-Path $raiz 'gradlew.bat'
        if (-not (Test-Path -LiteralPath $gradlew -PathType Leaf)) { throw 'No se encontro gradlew.bat.' }
        Push-Location $raiz
        try {
            Write-Host 'Ejecutando pruebas de core y compilando escritorio...' -ForegroundColor Cyan
            & $gradlew 'clean' ':core:test' ':lwjgl3:classes' '--no-daemon'
            if ($LASTEXITCODE -ne 0) { throw 'Fallaron pruebas o compilacion. Revisa la salida de Gradle.' }
        } finally { Pop-Location }
        Write-Host 'Gradle finalizo correctamente.' -ForegroundColor Green
    }
} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
