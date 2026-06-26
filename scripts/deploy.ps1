<#
.SYNOPSIS
    构建并部署到远程服务器
.DESCRIPTION
    本地构建 Maven + Docker 镜像，打包上传到服务器并重启服务。
.PARAMETER Server
    目标服务器 SSH 地址，格式: user@host
.PARAMETER Tag
    镜像标签，默认为 latest
.PARAMETER KeyFile
    SSH 私钥路径（可选）
.PARAMETER SkipBuild
    跳过构建步骤，直接使用已有镜像
.EXAMPLE
    .\scripts\deploy.ps1 -Server ubuntu@43.213.28.91 -Tag v0.1.0
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$Server,
    [string]$Tag = "latest",
    [string]$KeyFile = "",
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$rootDir = Split-Path -Parent $PSScriptRoot
Set-Location $rootDir

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

$sshArgs = @("-o", "StrictHostKeyChecking=no")
if ($KeyFile) { $sshArgs += @("-i", $KeyFile) }

function Invoke-Ssh($cmd) {
    & ssh @sshArgs $Server $cmd
}

function Invoke-Scp($src, $dst) {
    & scp @sshArgs -r $src "${Server}:${dst}"
}

# 1. 构建
if (-not $SkipBuild) {
    Write-Host "========================================" -ForegroundColor Cyan
    " Building images with tag: $Tag"
    Write-Host "========================================" -ForegroundColor Cyan
    & "$PSScriptRoot\build.ps1" -Tag $Tag
    if ($LASTEXITCODE -ne 0) { exit 1 }
}

# 2. 保存镜像为 tar 文件
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Saving Docker images" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$tempDir = Join-Path $env:TEMP "xueyifang-deploy-$(Get-Date -Format 'yyyyMMddHHmmss')"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

foreach ($svc in $services) {
    $imageName = "xueyifang/${svc}:${Tag}"
    $tarFile = Join-Path $tempDir "${svc}.tar"
    Write-Host "Saving $imageName ..." -ForegroundColor Yellow
    docker save -o $tarFile $imageName
}

# 3. 上传到服务器
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Uploading to $Server" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Invoke-Ssh "mkdir -p /tmp/xueyifang-deploy"
Invoke-Scp "$tempDir/*.tar" "/tmp/xueyifang-deploy/"

# 4. 在服务器上加载镜像并重启
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Loading images and restarting" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

foreach ($svc in $services) {
    $tarFile = "/tmp/xueyifang-deploy/${svc}.tar"
    Write-Host "Loading ${svc} ..." -ForegroundColor Yellow
    Invoke-Ssh "docker load -i $tarFile && rm -f $tarFile"
}

# 5. 重启服务（使用 docker compose）
Write-Host ""
Write-Host "Restarting services ..." -ForegroundColor Yellow
Invoke-Ssh "cd /opt/xueyifang && TAG=$Tag docker compose -f docker-compose.prod.yml up -d --no-deps gateway auth user service trade system file message"

# 6. 清理临时文件
Remove-Item -Recurse -Force $tempDir

Write-Host ""
Write-Host "Deployment completed!" -ForegroundColor Green
Write-Host "Gateway: http://${Server}:18080" -ForegroundColor Cyan