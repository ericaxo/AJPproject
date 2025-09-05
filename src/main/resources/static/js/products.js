// products.js

document.addEventListener('DOMContentLoaded', function () {
    const productList = document.getElementById('product-list');
    const pagination = document.getElementById('pagination');
    const pageSize = 6;
    let currentPage = 0;

    function fetchProducts(page = 0) {
        fetch(`/api/products?page=${page}&size=${pageSize}`)
            .then(res => res.json())
            .then(data => {
                renderProducts(data.products);
                renderPagination(data.currentPage, data.totalPages);
            });
    }

    function renderProducts(products) {
        if (!productList) return;
        productList.innerHTML = '';
        if (products.length === 0) {
            productList.innerHTML = '<div class="text-center py-5">No products found.</div>';
            return;
        }
        products.forEach(product => {
            const div = document.createElement('div');
            div.className = 'bg-white rounded-lg shadow-md overflow-hidden flex flex-col';
            div.innerHTML = `
                <a href="/products/${product.id}">
                    <img src="${product.imageUrl || 'https://images.unsplash.com/photo-1445205170230-053b83016050?w=400&h=400&fit=crop'}" alt="${product.name}" class="w-full h-64 object-cover">
                </a>
                <div class="p-4 flex flex-col flex-1">
                    <a href="/products/${product.id}">
                        <h3 class="text-lg font-semibold text-gray-900 hover:text-purple-600">${product.name}</h3>
                    </a>
                    <div class="flex items-center mt-2">
                        <i class="fas fa-leaf text-green-600 mr-2"></i>
                        
                    </div>
                    <p class="text-gray-600 mt-2 text-lg font-semibold">Rs ${product.price}</p>
                    <form action="/cart/add" method="post" class="mt-4">
                        <input type="hidden" name="productId" value="${product.id}">
                        <button type="submit" class="w-full bg-purple-600 text-white py-2 rounded-lg hover:bg-purple-700 ${product.stockQuantity <= 0 ? 'opacity-50 cursor-not-allowed' : ''}" ${product.stockQuantity <= 0 ? 'disabled' : ''}>
                            <i class="fas fa-cart-plus mr-1"></i>
                            <span>${product.stockQuantity > 0 ? 'Add to Cart' : 'Out of Stock'}</span>
                        </button>
                    </form>
                </div>
            `;
            productList.appendChild(div);
        });
    }

    function renderPagination(current, total) {
        if (!pagination) return;
        pagination.innerHTML = '';
        if (total <= 1) return;
        // Previous
        const prev = document.createElement('button');
        prev.textContent = 'Previous';
        prev.disabled = current === 0;
        prev.className = 'px-3 py-2 rounded-lg border mr-2 ' + (current === 0 ? 'text-gray-400' : 'hover:bg-gray-100');
        prev.onclick = () => fetchProducts(current - 1);
        pagination.appendChild(prev);
        // Page numbers
        for (let i = 0; i < total; i++) {
            const btn = document.createElement('button');
            btn.textContent = i + 1;
            btn.className = 'px-3 py-2 rounded-lg border ' + (i === current ? 'bg-purple-600 text-white' : 'hover:bg-gray-100');
            btn.onclick = () => fetchProducts(i);
            pagination.appendChild(btn);
        }
        // Next
        const next = document.createElement('button');
        next.textContent = 'Next';
        next.disabled = current === total - 1;
        next.className = 'px-3 py-2 rounded-lg border ml-2 ' + (current === total - 1 ? 'text-gray-400' : 'hover:bg-gray-100');
        next.onclick = () => fetchProducts(current + 1);
        pagination.appendChild(next);
    }

    // Only run if productList exists (on product listing page)
    if (productList) {
        fetchProducts();
    }
}); 