#! /bin/bash

if [[ "$1" == "" ]]; then
    echo "USAGE: user-keygen <username>"
    exit
fi

openssl genrsa -out private_key.pem 2048
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out "$1.private.der" -nocrypt
openssl rsa -in private_key.pem -pubout -outform DER -out "$1.public.der"
rm -f private_key.pem

mv *.der keys/clients/
echo "Keys for $1 generated"
