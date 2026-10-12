$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$getResp = Invoke-WebRequest -Uri "http://localhost:8080/auth?action=forgot" -WebSession $session -UseBasicParsing
if ($getResp.Content -match 'name="_csrf" value="([^"]+)"') {
    $token = $matches[1]
    Write-Host "CSRF Token: $token"
    $body = @{
        action = "forgot"
        email = "haiduong@gmail.com"
        _csrf = $token
    }
    $postResp = Invoke-WebRequest -Uri "http://localhost:8080/auth" -Method Post -Body $body -WebSession $session -UseBasicParsing
    Write-Host "POST Status: $($postResp.StatusCode)"
    
    if ($postResp.Content -match 'Chế độ Đồ án') {
        Write-Host "SUCCESS: DEV LINK BANNER FOUND!"
    } else {
        Write-Host "DEV LINK NOT FOUND. Content snippet:"
    }
    
    $postResp.Content -split "`n" | Where-Object { $_ -match 'Chế độ|Đã gửi thư|action=reset|token=' } | ForEach-Object { Write-Host $_.Trim() }
} else {
    Write-Host "CSRF Token not found in GET response"
}
