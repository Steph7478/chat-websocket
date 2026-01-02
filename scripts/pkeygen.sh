#!/bin/bash

echo "Encrypted key generator"
echo

echo "Choose the algorithm:"
echo "1) RSA"
echo "2) DSA"
echo "3) DH"
echo "4) ECDSA"
echo "5) ECDH"
echo "6) EdDSA"
read -p "Option: " option

case $option in
    1) alg="RSA";;
    2) alg="DSA";;
    3) alg="DH";;
    4) alg="ECDSA";;
    5) alg="ECDH";;
    6) alg="EdDSA";;
    *) echo "Invalid option"; exit 1;;
esac

echo
if [[ "$alg" =~ RSA|DSA|DH ]]; then
    echo "Choose the key size for $alg:"
    echo "1) 2048"
    echo "2) 3072"
    echo "3) 4096"
    echo "4) Custom"
    read -p "Option: " bits_option
    case $bits_option in
        1) bytes="2048";;
        2) bytes="3072";;
        3) bytes="4096";;
        4) read -p "Type custom bits as integer: " bytes;;
        *) echo "Invalid option"; exit 1;;
    esac
fi

echo
read -p "Do you want to encrypt the private key? (y/n): " encrypt_choice

echo
if [[ "$encrypt_choice" =~ [yY] ]]; then
    encrypt="yes"
    echo "Choose a cipher:"
    echo "1) AES-128-CBC"
    echo "2) AES-192-CTR"
    echo "3) AES-256-CBC"
    echo "4) DES-CBC"
    echo "5) 3DES-CBC"
    echo "6) Blowfish-CBC"
    echo "7) Camellia-128-CBC"
    echo "8) Camellia-256-CBC"

    read -p "Option: " cipher_option

    echo
    case $cipher_option in
        1) cipher="aes-128-cbc";;
        2) cipher="aes-192-ctr";;
        3) cipher="aes-256-cbc";;
        4) cipher="des-cbc";;
        5) cipher="des-ede3-cbc";;
        6) cipher="bf-cbc";;
        7) cipher="camellia-128-cbc";;
        8) cipher="camellia-256-cbc";;
        *) echo "Invalid option"; exit 1;;
    esac

    read -s -p "Type the password to encrypt the key: " password
    echo
else
    encrypt="no"
fi

echo
echo "Generating private key..."

if [[ "$alg" =~ RSA|DSA ]]; then
    cmd=(openssl genpkey -algorithm "$alg" -pkeyopt rsa_keygen_bits:"$bytes" -out private_key.pem)
elif [[ "$alg" = "DH" ]]; then
    cmd=(openssl genpkey -algorithm "$alg" -pkeyopt dh_paramgen_prime_len:"$bytes" -out private_key.pem)
elif [[ "$alg" =~ ECDSA|ECDH ]]; then
    cmd=(openssl genpkey -algorithm "$alg" -pkeyopt ec_paramgen_curve:P-256 -out private_key.pem)
elif [[ "$alg" = "EdDSA" ]]; then
    cmd=(openssl genpkey -algorithm "$alg" -out private_key.pem)
fi

if [[ "$encrypt" = "yes" ]]; then
    cmd+=(-"$cipher" -pass pass:"$password")
fi

"${cmd[@]}"

echo "Private key generated as private_key.pem"
