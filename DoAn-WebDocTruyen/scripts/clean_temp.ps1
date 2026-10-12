# Clean safe temp files
$temp = $env:TEMP
Write-Host "Cleaning Windows Temp..."
Get-ChildItem -Path $temp -Recurse -File -ErrorAction SilentlyContinue | Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-1) } | Remove-Item -Force -ErrorAction SilentlyContinue

# Clean browser subagent click feedbacks
$feedback = "C:\Users\Admin\.gemini\antigravity-ide\brain\33441399-0cb8-48ec-8552-f2c73f862264\.system_generated\click_feedback"
if (Test-Path $feedback) {
    Remove-Item -Path "$feedback\*" -Force -Recurse -ErrorAction SilentlyContinue
}

$d = Get-PSDrive C
Write-Host "After cleanup - Free MB: $([math]::Round($d.Free / 1MB, 2))"
Write-Host "After cleanup - Free GB: $([math]::Round($d.Free / 1GB, 2))"
