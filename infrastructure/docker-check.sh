#!/bin/bash

# Script to check if Docker is running and start it if not
# Works on macOS, Linux, and Windows (via WSL)

check_docker() {
    # Check if Docker daemon is running
    if docker info > /dev/null 2>&1; then
        echo "✅ Docker is already running"
        return 0
    else
        echo "⚠️  Docker is not running. Attempting to start..."
        return 1
    fi
}

start_docker() {
    OS="$(uname -s)"
    
    case "$OS" in
        Darwin*)
            # macOS - Start Docker Desktop
            if [ -d "/Applications/Docker.app" ]; then
                echo "🚀 Starting Docker Desktop on macOS..."
                open -a Docker
                echo "⏳ Waiting for Docker to start (this may take 30-60 seconds)..."
                
                # Wait for Docker to be ready (max 60 seconds)
                for i in {1..60}; do
                    if docker info > /dev/null 2>&1; then
                        echo "✅ Docker is now running!"
                        return 0
                    fi
                    sleep 1
                    echo -n "."
                done
                echo ""
                echo "❌ Docker failed to start within 60 seconds. Please start Docker Desktop manually."
                return 1
            else
                echo "❌ Docker Desktop not found. Please install Docker Desktop for macOS."
                return 1
            fi
            ;;
        Linux*)
            # Linux - Start Docker service
            if command -v systemctl > /dev/null 2>&1; then
                echo "🚀 Starting Docker service on Linux..."
                sudo systemctl start docker
                sleep 3
                if docker info > /dev/null 2>&1; then
                    echo "✅ Docker is now running!"
                    return 0
                else
                    echo "❌ Failed to start Docker. Please check Docker installation."
                    return 1
                fi
            else
                echo "❌ systemctl not found. Please start Docker manually."
                return 1
            fi
            ;;
        *)
            echo "❌ Unsupported operating system: $OS"
            echo "Please start Docker manually."
            return 1
            ;;
    esac
}

# Main execution
if ! check_docker; then
    if start_docker; then
        exit 0
    else
        exit 1
    fi
fi

exit 0

