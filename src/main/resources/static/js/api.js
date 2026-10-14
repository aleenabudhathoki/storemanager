const PRODUCTS_ENDPOINT = '/api/products';

class ApiError extends Error {
    constructor(status, payload) {
        super(payload?.message || 'The request could not be completed.');
        this.name = 'ApiError';
        this.status = status;
        this.payload = payload;
    }
}

async function request(url, options = {}) {
    let response;

    try {
        response = await fetch(url, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });
    } catch {
        throw new Error('Unable to connect to StoreManager. Check that the server is running and try again.');
    }

    let payload = null;
    if (response.status !== 204) {
        try {
            payload = await response.json();
        } catch {
            payload = null;
        }
    }

    if (!response.ok) {
        throw new ApiError(response.status, payload);
    }

    return payload;
}

export function getProducts() {
    return request(PRODUCTS_ENDPOINT);
}

export function createProduct(product) {
    return request(PRODUCTS_ENDPOINT, {
        method: 'POST',
        body: JSON.stringify(product)
    });
}

export function updateProduct(id, product) {
    return request(`${PRODUCTS_ENDPOINT}/${encodeURIComponent(id)}`, {
        method: 'PUT',
        body: JSON.stringify(product)
    });
}

export function deleteProduct(id) {
    return request(`${PRODUCTS_ENDPOINT}/${encodeURIComponent(id)}`, {
        method: 'DELETE'
    });
}

export { ApiError };
