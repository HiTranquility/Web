$paths = @(
    "C:\Users\Admin\.gemini\antigravity-ide\brain\33441399-0cb8-48ec-8552-f2c73f862264\.tempmediaStorage",
    "C:\Users\Admin\.gemini\antigravity-ide\brain\33441399-0cb8-48ec-8552-f2c73f862264\.system_generated\click_feedback",
    "$env:TEMP"
)

foreach ($p in $paths) {
    if (Test-Path $p) {
        $size = (Get-ChildItem $p -Recurse -File -ErrorAction SilentlyContinue | Measure-Object -Property Length -Sum).Sum
        Write-Host "$p : $([math]::Round($size / 1MB, 2)) MB"
    }
}
