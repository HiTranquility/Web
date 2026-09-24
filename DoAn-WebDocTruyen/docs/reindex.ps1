# =============================================================================
#  docs/reindex.ps1 - Quet docs/ va in ra ma so ke tiep + trang thai
# =============================================================================
[CmdletBinding()]
param()

try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { }

$DocsRoot = $PSScriptRoot
if (-not (Test-Path (Join-Path $DocsRoot 'INDEX.md'))) {
    $DocsRoot = Join-Path $PSScriptRoot 'docs'
}

$types = @(
    @{ Prefix = 'ISSUE'; Dir = 'projects\issues' },
    @{ Prefix = 'bug';   Dir = 'projects\bugs' },
    @{ Prefix = 'REQ';   Dir = 'requirements' }
)

function Get-NextId($prefix) {
    $allFiles = Get-ChildItem -Path $DocsRoot -Recurse -Filter '*.md' |
                Where-Object { $_.FullName -notmatch 'templates' }
    $max = 0
    foreach ($file in $allFiles) {
        $content = Get-Content -Path $file.FullName -Raw -ErrorAction SilentlyContinue
        if ($content) {
            $pattern = if ($prefix -eq 'bug') { '(?i)\bbug-(\d+)\b' } else { "\b$prefix-(\d+)\b" }
            $matches = [regex]::Matches($content, $pattern)
            foreach ($m in $matches) {
                $val = [int]$m.Groups[1].Value
                if ($val -gt $max) { $max = $val }
            }
        }
    }
    return ($max + 1)
}

$today = Get-Date -Format 'yyyy-MM-dd'
Write-Host "# INDEX (may sinh) - $today" -ForegroundColor Cyan
Write-Host ''
Write-Host '## Ma ke tiep cho moi loai' -ForegroundColor Yellow
Write-Host ''
Write-Host '| Loai | Ma tiep theo nen dung |'
Write-Host '|---|---|'
foreach ($t in $types) {
    $p = $t.Prefix
    $next = Get-NextId $p
    $code = if ($p -eq 'bug') { 'bug-{0:D3}' -f $next } else { '{0}-{1:D3}' -f $p, $next }
    Write-Host ("| {0} | `{1}` |" -f $p, $code)
}

Write-Host ''
Write-Host '## Doc theo loai' -ForegroundColor Yellow

foreach ($t in $types) {
    $p = $t.Prefix
    $targetDir = Join-Path $DocsRoot $t.Dir
    Write-Host ''
    $dirDisplay = $t.Dir -replace '\\', '/'
    Write-Host ("### {0} - docs/{1}" -f $p, $dirDisplay)
    if (-not (Test-Path $targetDir)) {
        Write-Host '_(chua co thu muc)_'
        continue
    }

    $mdFiles = Get-ChildItem -Path $targetDir -Recurse -Filter '*.md' |
               Where-Object { $_.Name -ne 'README.md' -and $_.Name -ne 'INDEX.md' } |
               Sort-Object FullName

    if ($mdFiles.Count -eq 0) {
        Write-Host '_(chua co doc)_'
        continue
    }

    foreach ($f in $mdFiles) {
        $lines = Get-Content $f.FullName -Encoding UTF8 -ErrorAction SilentlyContinue
        $title = '?'
        $status = ''
        foreach ($line in $lines) {
            if ($line -match '^#\s+(.+)$' -and $title -eq '?') {
                $title = $matches[1].Trim()
            }
            if ($line -match '\|\s*\*\*(Trạng thái|Status|Mức|Kết luận)\*\*\s*\|\s*([^\|]+)\|') {
                $status = $matches[2].Trim()
                break
            }
        }
        $relPath = $f.FullName.Substring($DocsRoot.Length + 1).Replace('\', '/')
        Write-Host ("- `{0}` - {1}" -f $relPath, $title)
        if ($status) {
            Write-Host ("    - Trang thai: {0}" -f $status) -ForegroundColor DarkGray
        }
    }
}

Write-Host ''
Write-Host 'Script quet tu dong tu thu muc va dong Meta Trang thai.' -ForegroundColor DarkCyan
