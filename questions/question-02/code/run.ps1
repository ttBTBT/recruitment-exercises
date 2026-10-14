param([string]$Main = "HelloWorld", [string[]]$ProgramArgs = @(), [string]$JavaHome = $env:JAVA_HOME)
$ErrorActionPreference = "Stop"
$sourceRoot = Join-Path $PSScriptRoot "src"
$classes = Join-Path $PSScriptRoot "build/classes"
New-Item -ItemType Directory -Force $classes | Out-Null
$tempDirectory = Join-Path $PSScriptRoot 'build/temp'
New-Item -ItemType Directory -Force $tempDirectory | Out-Null
$compiler = if ($JavaHome) { Join-Path $JavaHome "bin/javac.exe" } else { "javac" }
$runtime = if ($JavaHome) { Join-Path $JavaHome "bin/java.exe" } else { "java" }
$sources = @(Get-ChildItem -LiteralPath $sourceRoot -Filter *.java -Recurse | ForEach-Object { $_.FullName })
& $compiler -encoding UTF-8 --release 17 -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw "Java编译失败" }
& $runtime '-Dfile.encoding=UTF-8' "-Djava.io.tmpdir=$tempDirectory" -cp $classes "openatom.basics.$Main" @ProgramArgs
if ($LASTEXITCODE -ne 0) { throw "Java程序运行失败" }
