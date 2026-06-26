<#
.SYNOPSIS
    构建所有微服务 Docker 镜像
.DESCRIPTION
    先执行 mvn package，再逐个构建 Docker 镜像。
.PARAMETER Registry
    镜像仓库地址，默认为本地（不推送）
.PARAMETER Tag
    镜像标签，默认为 latest
.PARAMETER Push
    是否推送到远程仓库
.EXAMPLE
    .\scripts\build.ps1
    .\scripts\build.ps1 -Tag v0.1.0 -Push
#>
param(
    [string]$Registry = "",
    [string]$Tag = "latest",
    [switch]$Push
)

$ErrorActionPreference = "Stop"

$services = @(
    "xueyifang-gateway",
    "xueyifang-auth",
    "xueyifang-user",
    "xueyifang-service",
    "xueyifang-trade",
    "xueyifang-system",
    "xueyifang-file",
    "xueyifang-message"
)

$rootDir = Split-Path -Parent $PSScriptRoot
Set-Location $rootDir

# 1. Maven 构建
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " [1/2] Maven Build" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
mvn clean package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Maven build succeeded." -ForegroundColor Green

# 2. Docker 构建
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " [2/2] Docker Build" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

foreach ($svc in $services) {
    $prefix = if ($Registry) { "$Registry/" } else { "" }
    $imageName = "${prefix}xueyifang/${svc}:${Tag}"

    Write-Host "Building $imageName ..." -ForegroundColor Yellow
    docker build -t $imageName -f "$svc/Dockerfile" .
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Docker build failed for $svc!" -ForegroundColor Red
        exit 1
    }

    if ($Push) {
        Write-Host "Pushing $imageName ..." -ForegroundColor Yellow
        docker push $imageName
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Docker push failed for $svc!" -ForegroundColor Red
            exit 1
        }
    }
}

Write-Host ""
Write-Host "All images built successfully!" -ForegroundColor Green
Write-Host "Images:" -ForegroundColor Cyan
foreach ($svc in $services) {
    $prefix = if ($Registry) { "$Registry/" } else { "" }
    $imageName = "${prefix}xueyifang/${svc}:${Tag}"
    Write-Host "  - $imageName"
}