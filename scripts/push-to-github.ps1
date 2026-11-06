<#
.SYNOPSIS
    将 TestHub 仓库推送到 GitHub。

.DESCRIPTION
    封装了「校验工作区 -> 配置远端 -> 推送 -> 校验结果」四步，
    支持 HTTPS 与 SSH 两种地址，也支持用 gh CLI 顺带创建仓库。

.EXAMPLE
    # 首次推送（HTTPS，推荐交互式输入凭据）
    .\scripts\push-to-github.ps1 -Repo "testhub" -User "wangtongbo"

.EXAMPLE
    # 使用 SSH
    .\scripts\push-to-github.ps1 -Repo "testhub" -User "wangtongbo" -Protocol ssh

.EXAMPLE
    # 直接给完整地址
    .\scripts\push-to-github.ps1 -RepoUrl "https://github.com/wangtongbo/testhub.git"

.EXAMPLE
    # 只检查，不推送
    .\scripts\push-to-github.ps1 -RepoUrl "https://github.com/me/testhub.git" -DryRun
#>
[CmdletBinding(DefaultParameterSetName = 'Name')]
param(
    [Parameter(ParameterSetName = 'Url', Mandatory = $true)]
    [string]$RepoUrl,

    [Parameter(ParameterSetName = 'Name', Mandatory = $true)]
    [string]$User,

    [Parameter(ParameterSetName = 'Name', Mandatory = $true)]
    [string]$Repo,

    [Parameter(ParameterSetName = 'Name')]
    [ValidateSet('https', 'ssh')]
    [string]$Protocol = 'https',

    # 使用 gh CLI 创建仓库（需已执行 gh auth login）
    [switch]$CreateWithGh,

    # 仓库是否设为私有（仅在 -CreateWithGh 时生效）
    [switch]$PrivateRepo,

    # 只做检查与打印，不实际推送
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# 脚本放在 <repo>/scripts 下，仓库根目录是上一级
$RepoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $RepoRoot

function Write-Step($Text) { Write-Host "`n==> $Text" -ForegroundColor Cyan }
function Write-Ok($Text)   { Write-Host "    [OK] $Text" -ForegroundColor Green }
function Write-Warn2($Text){ Write-Host "    [!]  $Text" -ForegroundColor Yellow }

# ---------------------------------------------------------------- 1. 前置检查
Write-Step "检查环境"
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    throw "未找到 git，请先安装 Git 并加入 PATH"
}
Write-Ok ("git " + (git --version).Replace('git version ', ''))

if (-not (Test-Path (Join-Path $RepoRoot ".git"))) {
    throw "当前目录不是 git 仓库：$RepoRoot"
}

$branch = git rev-parse --abbrev-ref HEAD
Write-Ok "当前分支：$branch"

$dirty = git status --porcelain
if ($dirty) {
    Write-Warn2 "工作区存在未提交的改动："
    $dirty -split "`n" | ForEach-Object { if ($_.Trim()) { Write-Host "        $_" } }
    Write-Warn2 "建议先提交（git add -A; git commit -m ""...""）再推送，否则这些改动不会被上传"
}
else {
    Write-Ok "工作区干净"
}

$commitCount = git rev-list --count HEAD
Write-Ok "提交数：$commitCount"

# ---------------------------------------------------------------- 2. 解析地址
if ($PSCmdlet.ParameterSetName -eq 'Name') {
    if ($Protocol -eq 'ssh') {
        $RepoUrl = "git@github.com:$User/$Repo.git"
    }
    else {
        $RepoUrl = "https://github.com/$User/$Repo.git"
    }
}
Write-Step "目标仓库"
Write-Ok $RepoUrl

# ---------------------------------------------------------------- 3. 创建仓库（可选）
if ($CreateWithGh) {
    Write-Step "使用 gh CLI 创建仓库"
    if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
        Write-Warn2 "未找到 gh CLI，跳过创建；请先在网页上新建仓库 $Repo"
    }
    else {
        $visibility = if ($PrivateRepo) { '--private' } else { '--public' }
        if ($DryRun) {
            Write-Ok "（DryRun）将执行：gh repo create $User/$Repo $visibility --source . --remote origin"
        }
        else {
            gh repo create "$User/$Repo" $visibility --source . --remote origin
            Write-Ok "仓库已创建并绑定 origin"
        }
    }
}

# ---------------------------------------------------------------- 4. 配置远端
Write-Step "配置远端 origin"
$existing = git remote
if ($existing -contains 'origin') {
    $current = git remote get-url origin
    if ($current -ne $RepoUrl) {
        Write-Warn2 "origin 已存在（$current），将更新为 $RepoUrl"
        if (-not $DryRun) { git remote set-url origin $RepoUrl }
    }
    else {
        Write-Ok "origin 已指向目标地址"
    }
}
else {
    if ($DryRun) {
        Write-Ok "（DryRun）将执行：git remote add origin $RepoUrl"
    }
    else {
        git remote add origin $RepoUrl
        Write-Ok "已添加 origin"
    }
}

# ---------------------------------------------------------------- 5. 推送
Write-Step "推送 $branch 到 origin"
if ($DryRun) {
    Write-Ok "（DryRun）将执行：git push -u origin $branch"
}
else {
    git push -u origin $branch
    if ($LASTEXITCODE -ne 0) {
        throw "推送失败。常见原因：仓库不存在 / 无权限 / 需要先执行 gh auth login 或配置 PAT"
    }
    Write-Ok "推送完成"
}

# ---------------------------------------------------------------- 6. 结果
Write-Step "结果"
if ($DryRun) {
    Write-Ok "DryRun 结束，未实际推送"
}
else {
    $webUrl = $RepoUrl -replace '^git@github\.com:', 'https://github.com/' -replace '\.git$', ''
    Write-Ok "仓库地址：$webUrl"
    Write-Ok "提交历史："
    git log --oneline --no-decorate -5 | ForEach-Object { Write-Host "        $_" }
}

Write-Host ""
