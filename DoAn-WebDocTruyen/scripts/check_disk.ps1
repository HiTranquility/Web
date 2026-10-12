$d = Get-PSDrive C
Write-Host "Free MB: $([math]::Round($d.Free / 1MB, 2))"
Write-Host "Free GB: $([math]::Round($d.Free / 1GB, 2))"
