#!/bin/bash

# JWT Token Regeneration Script for Assignment Testing

echo "=== Regenerating JWT Token for Assignment Testing ==="

# Configuration
BASE_URL="http://localhost:5000"  # Your app runs on port 5000
STUDENT_EMAIL="mugishaprince395@gmail.com"  # Email from your token
STUDENT_PASSWORD="your_password_here"       # Replace with actual password

echo "Attempting to login and get fresh JWT token..."
echo "URL: $BASE_URL/api/auth/login"
echo "Email: $STUDENT_EMAIL"

# Login and get new JWT token
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$STUDENT_EMAIL\",
    \"password\": \"$STUDENT_PASSWORD\"
  }")

echo "Login Response:"
echo "$LOGIN_RESPONSE"

# Extract JWT token (adjust based on your response structure)
JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$JWT_TOKEN" ]; then
    echo "❌ Failed to extract JWT token from login response"
    echo "Please check your credentials and try again"
    exit 1
fi

echo ""
echo "✅ New JWT Token Generated:"
echo "$JWT_TOKEN"

# Save token to file for easy copying
echo "$JWT_TOKEN" > jwt_token.txt
echo ""
echo "💾 Token saved to jwt_token.txt"

# Test the token with assignment submission endpoint
echo ""
echo "🧪 Testing the new token with assignment endpoints..."

# Test 1: Check if we can access protected endpoint
echo "Testing token validation..."
TEST_RESPONSE=$(curl -s -X GET "$BASE_URL/api/assignments/submissions/student/my-submissions" \
  -H "Authorization: Bearer $JWT_TOKEN")

if [[ "$TEST_RESPONSE" == *"success"* ]]; then
    echo "✅ Token validation successful!"
else
    echo "❌ Token validation failed:"
    echo "$TEST_RESPONSE"
fi

echo ""
echo "=== Token Generation Complete ==="
echo "Use this token in your assignment submission requests:"
echo "Authorization: Bearer $JWT_TOKEN"