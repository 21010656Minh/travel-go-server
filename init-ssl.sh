#!/bin/bash
# Initial SSL certificate setup script for Let's Encrypt
# Run this ONCE before starting docker-compose

DOMAIN="travel-social-network.duckdns.org"
EMAIL="your-email@example.com"  # Change this to your email

# Create directories
mkdir -p ./certbot/conf
mkdir -p ./certbot/www

# Create a temporary nginx config for initial certificate
cat > ./nginx/conf.d/default.conf << 'EOF'
server {
    listen 80;
    server_name travel-social-network.duckdns.org;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://$host$request_uri;
    }
}
EOF

echo "Starting nginx for certificate generation..."
docker compose up -d nginx

echo "Waiting for nginx to start..."
sleep 5

echo "Requesting certificate from Let's Encrypt..."
docker run --rm \
    -v "$(pwd)/certbot/conf:/etc/letsencrypt" \
    -v "$(pwd)/certbot/www:/var/www/certbot" \
    certbot/certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email $EMAIL \
    --agree-tos \
    --no-eff-email \
    -d $DOMAIN

echo "Certificate generated! Now update nginx config and restart."
docker compose down
