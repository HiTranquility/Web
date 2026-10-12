$queries = @("mocmien", "Mộc Miên", "kiem-khi", "tu tiên")
foreach ($q in $queries) {
    $encoded = [System.Web.HttpUtility]::UrlEncode($q)
    $url = "http://localhost:8080/story?q=$encoded"
    $resp = Invoke-WebRequest -Uri $url -UseBasicParsing
    $count = ($resp.Content | Select-String -Pattern 'class="story-card"' -AllMatches).Matches.Count
    Write-Host "Query '$q' -> Results: $count stories found"
}
