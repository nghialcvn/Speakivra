# Run on your Windows PC with JDK 17+ and GitHub CLI installed and signed in.
# Keys and passwords stay outside the public checkout; existing keys are never replaced.
param([string]$KeyDirectory = (Join-Path $env:USERPROFILE 'Speakivra-Signing'))
$ErrorActionPreference = 'Stop'
foreach ($command in @('keytool', 'gh')) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) { throw "Install $command first." }
}
& gh auth status
if ($LASTEXITCODE -ne 0) { throw 'Run gh auth login before this script.' }
New-Item -ItemType Directory -Force -Path $KeyDirectory | Out-Null
$keyPath = Join-Path $KeyDirectory 'speakivra-release.jks'
$alias = 'speakivra'
$secure = Read-Host 'Password for the fixed Speakivra signing key (keep a secure backup)' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
try {
    $env:SPEAKIVRA_SETUP_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    if ($env:SPEAKIVRA_SETUP_PASSWORD.Length -lt 12) { throw 'Use at least 12 characters.' }
    if (-not (Test-Path $keyPath)) {
        & keytool -genkeypair -keystore $keyPath -storetype JKS -alias $alias -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=Speakivra' -storepass:env SPEAKIVRA_SETUP_PASSWORD -keypass:env SPEAKIVRA_SETUP_PASSWORD
        if ($LASTEXITCODE -ne 0) { throw 'Cannot create signing key.' }
    }
    $certificatePath = Join-Path $KeyDirectory 'speakivra-certificate.der'
    & keytool -exportcert -keystore $keyPath -alias $alias -file $certificatePath -storepass:env SPEAKIVRA_SETUP_PASSWORD
    if ($LASTEXITCODE -ne 0) { throw 'Cannot read existing key. Do not replace it.' }
    $fingerprint = (Get-FileHash -Algorithm SHA256 $certificatePath).Hash.ToLowerInvariant()
    $values = @{
        SPEAKIVRA_KEYSTORE_BASE64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($keyPath))
        SPEAKIVRA_STORE_PASSWORD = $env:SPEAKIVRA_SETUP_PASSWORD
        SPEAKIVRA_KEY_ALIAS = $alias
        SPEAKIVRA_KEY_PASSWORD = $env:SPEAKIVRA_SETUP_PASSWORD
        SPEAKIVRA_SIGNING_SHA256 = $fingerprint
    }
    foreach ($name in $values.Keys) {
        $values[$name] | & gh secret set $name --repo nghialcvn/Speakivra
        if ($LASTEXITCODE -ne 0) { throw "Cannot save secret $name. Reuse this same key on retry." }
    }
    Write-Host "Signing secrets saved. Certificate SHA-256: $fingerprint"
    Write-Host "Keep an encrypted backup of $keyPath and its password. Never upload them to the repository."
} finally {
    Remove-Item Env:SPEAKIVRA_SETUP_PASSWORD -ErrorAction SilentlyContinue
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    if ($values) { $values.Clear() }
}
