#!/bin/bash

# Download Pinot Binaries Script
# This script downloads Apache Pinot binaries for local testing

set -e

PINOT_VERSION="1.0.0"
PINOT_DIR="testing/bin"
DOWNLOAD_URL="https://downloads.apache.org/pinot/apache-pinot-${PINOT_VERSION}/apache-pinot-${PINOT_VERSION}-bin.tar.gz"
TARBALL="apache-pinot-${PINOT_VERSION}-bin.tar.gz"

echo "🚀 Downloading Apache Pinot ${PINOT_VERSION}..."

# Create directory if it doesn't exist
mkdir -p ${PINOT_DIR}
cd ${PINOT_DIR}

# Download Pinot if not already present
if [ ! -f "${TARBALL}" ]; then
    echo "📥 Downloading from ${DOWNLOAD_URL}..."
    curl -L -o "${TARBALL}" "${DOWNLOAD_URL}"
else
    echo "✅ Pinot tarball already exists: ${TARBALL}"
fi

# Extract if not already extracted
if [ ! -d "apache-pinot-${PINOT_VERSION}-bin" ]; then
    echo "📦 Extracting Pinot binaries..."
    tar -xzf "${TARBALL}"
    echo "✅ Pinot binaries extracted to: apache-pinot-${PINOT_VERSION}-bin"
else
    echo "✅ Pinot binaries already extracted"
fi

# Create symlink for easy access
if [ ! -L "pinot" ]; then
    ln -sf "apache-pinot-${PINOT_VERSION}-bin" "pinot"
    echo "🔗 Created symlink: pinot -> apache-pinot-${PINOT_VERSION}-bin"
fi

echo "🎉 Pinot download complete!"
echo "📁 Location: ${PINOT_DIR}/pinot"
echo "🚀 Ready to start Pinot components!"
