#!/bin/bash

echo "🔧 Setting up local PostgreSQL database for IGA application..."

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if PostgreSQL is installed
if ! command -v psql &> /dev/null; then
    echo -e "${YELLOW}PostgreSQL not found. Installing...${NC}"
    
    # Update package list
    sudo apt update
    
    # Install PostgreSQL
    sudo apt install -y postgresql postgresql-contrib
    
    # Start and enable PostgreSQL service
    sudo systemctl start postgresql
    sudo systemctl enable postgresql
    
    echo -e "${GREEN}✅ PostgreSQL installed successfully${NC}"
else
    echo -e "${GREEN}✅ PostgreSQL is already installed${NC}"
fi

# Start PostgreSQL service if not running
sudo systemctl start postgresql

# Create database and user
echo -e "${YELLOW}Creating database and user...${NC}"

# Drop existing database and user if they exist (for clean setup)
sudo -u postgres psql -c "DROP DATABASE IF EXISTS iga_db;" 2>/dev/null
sudo -u postgres psql -c "DROP USER IF EXISTS iga_user;" 2>/dev/null

# Create new database and user
sudo -u postgres psql -c "CREATE DATABASE iga_db;"
sudo -u postgres psql -c "CREATE USER iga_user WITH PASSWORD 'iga_password';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE iga_db TO iga_user;"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON SCHEMA public TO iga_user;" iga_db

echo -e "${GREEN}✅ Database and user created successfully${NC}"

# Set environment variables
echo -e "${YELLOW}Setting environment variables...${NC}"

export DB_URL="jdbc:postgresql://localhost:5432/iga_db"
export DB_USERNAME="iga_user"
export DB_PASSWORD="iga_password"
export JWT_SECRET="mySecretKeyForDevelopmentPurposesThatIsAtLeast32CharactersLongAndSecure"
export JWT_EXPIRATION_MS="86400000"

# Add to current session
echo "export DB_URL=\"jdbc:postgresql://localhost:5432/iga_db\"" >> ~/.bashrc
echo "export DB_USERNAME=\"iga_user\"" >> ~/.bashrc
echo "export DB_PASSWORD=\"iga_password\"" >> ~/.bashrc
echo "export JWT_SECRET=\"mySecretKeyForDevelopmentPurposesThatIsAtLeast32CharactersLongAndSecure\"" >> ~/.bashrc
echo "export JWT_EXPIRATION_MS=\"86400000\"" >> ~/.bashrc

echo -e "${GREEN}✅ Environment variables set${NC}"

# Test database connection
echo -e "${YELLOW}Testing database connection...${NC}"
if PGPASSWORD=iga_password psql -h localhost -U iga_user -d iga_db -c "SELECT version();" &> /dev/null; then
    echo -e "${GREEN}✅ Database connection successful${NC}"
else
    echo -e "${RED}❌ Database connection failed${NC}"
    exit 1
fi

echo -e "${GREEN}"
echo "🎉 Setup completed successfully!"
echo ""
echo "Database Details:"
echo "  Host: localhost"
echo "  Port: 5432"
echo "  Database: iga_db"
echo "  Username: iga_user"
echo "  Password: iga_password"
echo ""
echo "Environment variables have been set for this session and added to ~/.bashrc"
echo ""
echo "To start your application, run:"
echo "  cd /home/prince/Downloads/IGA"
echo "  mvn spring-boot:run"
echo -e "${NC}"

# Optional: Show PostgreSQL status
echo -e "${YELLOW}PostgreSQL service status:${NC}"
sudo systemctl status postgresql --no-pager -l