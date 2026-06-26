<#
.SYNOPSIS
    部署冒烟测试
.DESCRIPTION
    测试 Gateway 健康检查和基本接口可达性。
.PARAMETER Host
    目标主机地址
.PARAMETER Port
    Gateway 端口，默认 18080
.EXAMPLE
    .\scripts\smoke-test.ps1 -Host 43.213.28.91
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$Host,
    [int]$Port = 18080
)

$baseUrl = "http://${Host}:${Port}"
$passed = 0
$failed = 0

function Test-Endpoint($name, $method, $path, $expectedStatus) {
    $url = "${baseUrl}${path}"
    Write-Host "Testing: $name ... " -NoNewline
    try {
        $response = Invoke-WebRequest -Uri $url -Method $method -UseBasicParsing -TimeoutSec 10 -ErrorAction Stop
        if ($response.StatusCode -eq $expectedStatus) {
            Write-Host "PASS ($($response.StatusCode))" -ForegroundColor Green
            $script:passed++
        } else {
            Write-Host "FAIL (expected $expectedStatus, got $($response.StatusCode))" -ForegroundColor Red
            $script:failed++
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq $expectedStatus) {
            Write-Host "PASS ($statusCode)" -ForegroundColor Green
            $script:passed++
        } else {
            Write-Host "FAIL ($statusCode)" -ForegroundColor Red
            $script:failed++
        }
    }
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Smoke Test: $baseUrl" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Gateway Health
Test-Endpoint "Gateway Health" "GET" "/actuator/health" 200

# Auth endpoints (should return 401/403 without token)
Test-Endpoint "Auth - Register (no body)" "POST" "/auth/register" 400
Test-Endpoint "Auth - Login (no body)" "POST" "/auth/login" 400

# Public endpoints
Test-Endpoint "System - Config" "GET" "/sys/config/registration-enabled" 200
Test-Endpoint "System - Professionals" "GET" "/professionals" 200

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Results: $passed passed, $failed failed" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host "========================================" -ForegroundColor Cyan

if ($failed -gt 0) { exit 1 }