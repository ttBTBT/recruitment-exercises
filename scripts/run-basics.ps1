param(
    [ValidateRange(0, 8)][int]$Question = 0,
    [ValidateSet('', 'HelloWorld', 'ProductCalculation', 'Toolbox', 'OopDemo', 'PaymentDemo', 'OrderDemo', 'FileDemo', 'TeaDemo', 'BoundaryTests')]
    [string]$Main = '',
    [string[]]$ProgramArgs = @(),
    [string]$JavaHome = $env:JAVA_HOME
)
$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$entries = @{1='HelloWorld';2='ProductCalculation';3='Toolbox';4='OopDemo';5='PaymentDemo';6='OrderDemo';7='FileDemo';8='TeaDemo'}
if ($Question) {
    if ($Main -and $Main -ne $entries[$Question]) { throw 'Question and Main do not match.' }
    $Main = $entries[$Question]
} elseif (-not $Main) { $Main = 'HelloWorld' }
if ($Main -ne 'BoundaryTests') {
    foreach ($number in $entries.Keys) { if ($entries[$number] -eq $Main) { $Question = $number; break } }
}
$label = if ($Question) { 'question-{0:D2}' -f $Question } else { 'all' }
$classes = Join-Path $repoRoot "build/classes/$label"
$tempDirectory = Join-Path $repoRoot 'build/temp'
New-Item -ItemType Directory -Force $classes, $tempDirectory | Out-Null
$numbers = if ($Question) { @($Question) } else { @(1..8) }
$sources = @(
    foreach ($number in $numbers) {
        $sourceRoot = Join-Path $repoRoot ('questions/question-{0:D2}/src' -f $number)
        Get-ChildItem -LiteralPath $sourceRoot -Filter '*.java' -Recurse | ForEach-Object { $_.FullName }
    }
)
if ($Main -eq 'BoundaryTests') {
    $sources += @(Get-ChildItem -LiteralPath (Join-Path $repoRoot 'tests/basics/src') -Filter '*.java' -Recurse | ForEach-Object { $_.FullName })
}
$compiler = if ($JavaHome) { Join-Path $JavaHome 'bin/javac.exe' } else { 'javac' }
$runtime = if ($JavaHome) { Join-Path $JavaHome 'bin/java.exe' } else { 'java' }
& $compiler -encoding UTF-8 --release 17 -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
& $runtime '-Dfile.encoding=UTF-8' "-Djava.io.tmpdir=$tempDirectory" -cp $classes "openatom.basics.$Main" @ProgramArgs
if ($LASTEXITCODE -ne 0) { throw 'Java program failed.' }
