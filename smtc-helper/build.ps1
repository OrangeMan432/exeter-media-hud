$CSC = "C:\Program Files\Microsoft Visual Studio\2022\Community\MSBuild\Current\Bin\Roslyn\csc.exe"
$UNION = "C:\Program Files (x86)\Windows Kits\10\UnionMetadata\10.0.26100.0\Windows.winmd"
$RUNTIME = "C:\Windows\Microsoft.NET\assembly\GAC_MSIL\System.Runtime.WindowsRuntime\v4.0_4.0.0.0__b77a5c561934e089\System.Runtime.WindowsRuntime.dll"

& $CSC /target:exe /out:"$PSScriptRoot\smtc.exe" `
    /reference:$UNION `
    /reference:$RUNTIME `
    "$PSScriptRoot\SmtcHelper.cs"

if ($LASTEXITCODE -eq 0) {
    Write-Output "Build succeeded: smtc.exe"
} else {
    Write-Output "Build failed"
    exit 1
}
