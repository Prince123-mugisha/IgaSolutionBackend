#!/bin/bash

echo "=== IGA Application Environment Setup ==="
echo

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "Creating .env file from template..."
    cp .env.example .env
    echo ".env file created. Please edit it with your actual values."
else
    echo ".env file already exists."
fi

echo
echo "Current JWT_SECRET status:"
if [ -z "$JWT_SECRET" ]; then
    echo "❌ JWT_SECRET is not set in environment"
    echo "💡 Recommendation: Set JWT_SECRET environment variable"
    echo "   export JWT_SECRET='your-very-secure-secret-key-at-least-32-chars'"
else
    echo "✅ JWT_SECRET is set (length: ${#JWT_SECRET} characters)"
    if [ ${#JWT_SECRET} -lt 32 ]; then
        echo "⚠️  WARNING: JWT_SECRET should be at least 32 characters for security"
    fi
fi

echo
echo "To set environment variables for this session:"
echo "source .env"
echo
echo "To make them permanent, add them to your ~/.bashrc or ~/.zshrc"
echo

# Generate a secure JWT secret if needed
echo "=== JWT Secret Generator ==="
echo "Here's a secure JWT secret you can use:"
openssl rand -base64 32
echo
echo "Copy the above secret and use it as your JWT_SECRET environment variable"