# PowerShell script for initial SSL certificate setup with Let's Encrypt
# Run this ONCE before starting docker-compose

$DOMAIN = "travel-social-network.duckdns.org"
$EMAIL = "your-email@example.com"  # Change this to your email

# Create directories
New-Item -ItemType Directory -Force -Path ".\certbot\conf"
New-Item -ItemType Directory -Force -Path ".\certbot\www"

# Create a temporary nginx config for initial certificate
$tempConfig = @"
server {
    listen 80;
    server_name travel-social-network.duckdns.org;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://`$host`$request_uri;
    }
}
"@

Set-Content -Path ".\nginx\conf.d\default.conf" -Value $tempConfig

Write-Host "Starting nginx for certificate generation..." -ForegroundColor Yellow
docker compose up -d nginx

Write-Host "Waiting for nginx to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host "Requesting certificate from Let's Encrypt..." -ForegroundColor Yellow
docker run --rm `
    -v "${PWD}/certbot/conf:/etc/letsencrypt" `
    -v "${PWD}/certbot/www:/var/www/certbot" `
    certbot/certbot certonly `
    --webroot `
    --webroot-path=/var/www/certbot `
    --email $EMAIL `
    --agree-tos `
    --no-eff-email `
    -d $DOMAIN

Write-Host "Certificate generated! Now run the full docker-compose." -ForegroundColor Green
docker compose down
