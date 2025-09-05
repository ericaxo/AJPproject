#!/bin/bash

echo "🚀 Starting Tab on Furniture Application..."
echo "📁 Project: $(pwd)"
echo "⏰ Time: $(date)"
echo ""

# Stop any existing instances
echo "🛑 Stopping any existing instances..."
pkill -f "TabOnFashionApplication" 2>/dev/null || true

# Wait a moment
sleep 2

# Clean and compile
echo "🧹 Cleaning and compiling..."
mvn clean compile -q

# Start the application
echo "🎯 Starting Spring Boot application..."
echo "🌐 Application will be available at: http://localhost:8080"
echo "📊 H2 Console: http://localhost:8080/h2-console"
echo "👤 Admin Login: admin@tabonfurniture.com / admin123"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

# Load environment variables if .env exists
if [ -f "scripts/load_env.sh" ]; then
  # shellcheck source=/dev/null
  source scripts/load_env.sh
fi

mvn spring-boot:run