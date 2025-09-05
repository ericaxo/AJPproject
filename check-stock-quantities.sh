#!/bin/bash

echo "📊 Checking Product Stock Quantities..."
echo "======================================"

# Check if application is running
if curl -s http://localhost:8080/ > /dev/null; then
    echo "✅ Application is running"
    
    # Get products page and extract stock information
    echo ""
    echo "🛍️  Current Product Stock Levels:"
    echo "--------------------------------"
    
    # Use curl to get the products page and extract stock info
    curl -s http://localhost:8080/products | grep -o "Stock: [0-9]*" | head -10 || echo "Stock information not found in HTML"
    
    echo ""
    echo "📈 Stock Summary:"
    echo "----------------"
    echo "• Total products with stock: $(curl -s http://localhost:8080/products | grep -c "Stock:")"
    echo "• Products in stock: $(curl -s http://localhost:8080/products | grep -c "In Stock")"
    echo "• Products out of stock: $(curl -s http://localhost:8080/products | grep -c "Out of Stock")"
    
    echo ""
    echo "🌐 Visit http://localhost:8080/products to see all products with updated stock quantities!"
    
else
    echo "❌ Application is not running. Please start the application first."
fi

