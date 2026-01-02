#!/bin/bash

echo "=== Keystore Generator for Chat Local ==="
echo

read -p "Enter keystore file name (default: keystore.p12): " keystore
keystore=${keystore:-keystore.p12}

read -p "Enter alias for key (default: chat-local): " alias
alias=${alias:-chat-local}

read -s -p "Enter password for keystore: " ks_pass
echo
read -s -p "Confirm password: " ks_pass_confirm
echo

if [ "$ks_pass" != "$ks_pass_confirm" ]; then
    echo "Passwords do not match. Exiting."
    exit 1
fi

echo
echo "Generating RSA keypair and self-signed certificate..."
keytool -genkeypair \
    -alias "$alias" \
    -keyalg RSA \
    -keysize 2048 \
    -storetype PKCS12 \
    -keystore "$keystore" \
    -validity 365 \
    -storepass "$ks_pass" \
    -keypass "$ks_pass" \
    -dname "CN=localhost, OU=Chat, O=LocalDev, L=City, ST=State, C=BR"

echo
echo "✅ Keystore generated: $keystore"
echo "Alias: $alias"
echo "You can now use this keystore in your Spring Boot or local WSS server."
