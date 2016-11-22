#!/bin/bash

if [["$1" -eq ""]]; then
    echo 'USAGE: keygen <client-name>'
    exit
fi
openssl genrsa -out "$1.private_key.pem" 2048
openssl pkcs8 -topk8 -inform PEM -outform DER -in "$1.private_key.pem" -out "$1.private.der" -nocrypt
openssl rsa -in "$1.private_key.pem" -pubout -outform DER -out "$1.public.der"

rm -f "$1.private_key.pem"
mv "$1.private.der" "$1.public.der"  ./resources/keys/clients/
