#!/bin/bash

# ==== Configuration ====
# Your remote username (replace with your CS account username)
USER=jxiong3@mimi.cs.mcgill.ca

# List of CS machines
MACHINES=(
    tr-open-01.cs.mcgill.ca
    tr-open-02.cs.mcgill.ca
    tr-open-03.cs.mcgill.ca
    tr-open-04.cs.mcgill.ca
    tr-open-05.cs.mcgill.ca
)

# ==== Logic ====

if [ ! -f ~/.ssh/id_rsa.pub ]; then
    echo "No SSH key found at ~/.ssh/id_rsa.pub, generating one..."
    ssh-keygen -t rsa -b 4096 -C "$USER@mcgill" -N "" -f ~/.ssh/id_rsa
else
    echo "SSH key already exists, skipping key generation."
fi

for HOST in "${MACHINES[@]}"; do
    echo "----------------------------------------"
    echo "Configuring $HOST ..."
    ssh-copy-id ${USER}@${HOST}
done

echo "========================================"
echo "All machines configured!"
echo "You can now SSH without a password, e.g.: ssh ${USER}@tr-open-01.cs.mcgill.ca"
