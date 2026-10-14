import { ApiError, createProduct, deleteProduct, getProducts, updateProduct } from './api.js';

const LOW_STOCK_THRESHOLD = 10;
const currencyFormatter = new Intl.NumberFormat(undefined, {
    style: 'currency',
    currency: 'USD'
});

const state = {
    products: [],
    editingId: null
};

const elements = {
    addButton: document.querySelector('#add-product-button'),
    alert: document.querySelector('#page-alert'),
    loading: document.querySelector('#loading-state'),
    empty: document.querySelector('#empty-state'),
    table: document.querySelector('#table-state'),
    tableBody: document.querySelector('#product-table-body'),
    search: document.querySelector('#product-search'),
    totalProducts: document.querySelector('#total-products'),
    totalUnits: document.querySelector('#total-units'),
    lowStockProducts: document.querySelector('#low-stock-products'),
    modal: document.querySelector('#product-modal'),
    modalTitle: document.querySelector('#product-modal-title'),
    form: document.querySelector('#product-form'),
    formAlert: document.querySelector('#form-alert'),
    productId: document.querySelector('#product-id'),
    sku: document.querySelector('#product-sku'),
    name: document.querySelector('#product-name'),
    description: document.querySelector('#product-description'),
    price: document.querySelector('#product-price'),
    quantity: document.querySelector('#product-quantity'),
    saveButton: document.querySelector('#save-product-button'),
    saveLabel: document.querySelector('#save-product-label'),
    saveSpinner: document.querySelector('#save-product-spinner')
};

const bootstrapModal = bootstrap.Modal.getOrCreateInstance(elements.modal);

function setPageAlert(message, type = 'danger', show = true) {
    elements.alert.textContent = message;
    elements.alert.className = `alert alert-${type}${show ? '' : ' d-none'}`;
}

function setViewState(view) {
    elements.loading.classList.toggle('d-none', view !== 'loading');
    elements.empty.classList.toggle('d-none', view !== 'empty');
    elements.table.classList.toggle('d-none', view !== 'table');
}

function updateStats(products) {
    elements.totalProducts.textContent = products.length.toLocaleString();
    elements.totalUnits.textContent = products
        .reduce((total, product) => total + Number(product.quantity || 0), 0)
        .toLocaleString();
    elements.lowStockProducts.textContent = products
        .filter(product => Number(product.quantity) < LOW_STOCK_THRESHOLD)
        .length
        .toLocaleString();
}

function filteredProducts() {
    const query = elements.search.value.trim().toLowerCase();
    if (!query) {
        return state.products;
    }

    return state.products.filter(product =>
        String(product.name || '').toLowerCase().includes(query)
        || String(product.sku || '').toLowerCase().includes(query)
    );
}

function createCell(text, className = '') {
    const cell = document.createElement('td');
    cell.textContent = text;
    if (className) {
        cell.className = className;
    }
    return cell;
}

function createStatusBadge(quantity) {
    const badge = document.createElement('span');
    const numericQuantity = Number(quantity);
    const isOutOfStock = numericQuantity === 0;
    const isLowStock = numericQuantity < LOW_STOCK_THRESHOLD;
    badge.className = `badge rounded-pill ${
        isOutOfStock ? 'text-bg-danger' : isLowStock ? 'text-bg-warning' : 'text-bg-success'
    }`;
    badge.textContent = isOutOfStock ? 'Out of stock' : isLowStock ? 'Low stock' : 'In stock';
    return badge;
}

function createActionButton(label, className, action, productId) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `btn btn-sm ${className}`;
    button.textContent = label;
    button.dataset.action = action;
    button.dataset.productId = productId;
    return button;
}

function renderTable() {
    const products = filteredProducts();
    elements.tableBody.replaceChildren();

    products.forEach(product => {
        const row = document.createElement('tr');
        row.append(
            createCell(product.sku),
            createCell(product.name),
            createCell(product.description || '—', 'description-cell'),
            createCell(currencyFormatter.format(Number(product.price))),
            createCell(String(product.quantity)),
        );

        const statusCell = document.createElement('td');
        statusCell.append(createStatusBadge(product.quantity));
        row.append(statusCell);

        const actionsCell = document.createElement('td');
        actionsCell.className = 'text-end text-nowrap';
        actionsCell.append(
            createActionButton('Edit', 'btn-outline-primary me-2', 'edit', product.id),
            createActionButton('Delete', 'btn-outline-danger', 'delete', product.id)
        );
        row.append(actionsCell);
        elements.tableBody.append(row);
    });

    if (!products.length) {
        elements.empty.replaceChildren();
        const title = document.createElement('p');
        title.className = 'mb-2 fw-semibold';
        title.textContent = state.products.length ? 'No matching products' : 'No products found';
        const description = document.createElement('p');
        description.className = 'text-secondary mb-0';
        description.textContent = state.products.length
            ? 'Try a different name or SKU.'
            : 'Add your first product to populate the catalog.';
        elements.empty.append(title, description);
    }

    setViewState(products.length ? 'table' : 'empty');
}

function renderDashboard() {
    updateStats(state.products);
    renderTable();
}

async function loadProducts() {
    setViewState('loading');
    setPageAlert('', 'danger', false);

    try {
        state.products = await getProducts();
        renderDashboard();
    } catch (error) {
        setViewState('empty');
        elements.empty.replaceChildren();
        const message = document.createElement('p');
        message.className = 'mb-3 text-danger';
        message.textContent = userMessage(error);
        const retryButton = document.createElement('button');
        retryButton.type = 'button';
        retryButton.className = 'btn btn-outline-primary btn-sm';
        retryButton.textContent = 'Retry';
        retryButton.addEventListener('click', loadProducts);
        elements.empty.append(message, retryButton);
    }
}

function userMessage(error) {
    if (error instanceof ApiError) {
        if (error.status === 404) return 'The requested product was not found.';
        if (error.status === 409) return 'That SKU is already in use.';
        if (error.status >= 500) return 'The server could not complete the request. Please try again.';
        return error.message;
    }
    return error.message || 'Something went wrong. Please try again.';
}

function clearFieldErrors() {
    ['sku', 'name', 'description', 'price', 'quantity'].forEach(field => {
        const input = elements[field];
        input.classList.remove('is-invalid');
        document.querySelector(`#${field}-error`).textContent = '';
    });
    elements.formAlert.classList.add('d-none');
    elements.formAlert.textContent = '';
}

function showFieldErrors(payload) {
    const fieldErrors = payload?.fieldErrors || {};
    Object.entries(fieldErrors).forEach(([field, message]) => {
        const input = elements[field];
        const error = document.querySelector(`#${field}-error`);
        if (input && error) {
            input.classList.add('is-invalid');
            error.textContent = message;
        }
    });

    if (!Object.keys(fieldErrors).length) {
        elements.formAlert.textContent = userMessage(new ApiError(400, payload));
        elements.formAlert.classList.remove('d-none');
    }
}

function resetForm() {
    state.editingId = null;
    elements.form.reset();
    elements.productId.value = '';
    elements.modalTitle.textContent = 'Add product';
    elements.saveLabel.textContent = 'Save product';
    clearFieldErrors();
}

function populateForm(product) {
    state.editingId = product.id;
    elements.productId.value = product.id;
    elements.modalTitle.textContent = 'Edit product';
    elements.saveLabel.textContent = 'Update product';
    elements.sku.value = product.sku || '';
    elements.name.value = product.name || '';
    elements.description.value = product.description || '';
    elements.price.value = product.price ?? '';
    elements.quantity.value = product.quantity ?? '';
    clearFieldErrors();
}

function formData() {
    return {
        sku: elements.sku.value.trim(),
        name: elements.name.value.trim(),
        description: elements.description.value.trim() || null,
        price: Number(elements.price.value),
        quantity: Number(elements.quantity.value)
    };
}

function validateForm() {
    clearFieldErrors();
    if (elements.form.checkValidity()) {
        return true;
    }

    elements.form.classList.add('was-validated');
    return false;
}

async function submitForm(event) {
    event.preventDefault();
    if (!validateForm()) return;

    elements.saveButton.disabled = true;
    elements.saveLabel.classList.add('d-none');
    elements.saveSpinner.classList.remove('d-none');

    try {
        const product = formData();
        if (state.editingId) {
            await updateProduct(state.editingId, product);
        } else {
            await createProduct(product);
        }
        bootstrapModal.hide();
        await loadProducts();
    } catch (error) {
        if (error instanceof ApiError && error.status === 400) {
            showFieldErrors(error.payload);
        } else {
            elements.formAlert.textContent = userMessage(error);
            elements.formAlert.classList.remove('d-none');
        }
    } finally {
        elements.saveButton.disabled = false;
        elements.saveLabel.classList.remove('d-none');
        elements.saveSpinner.classList.add('d-none');
    }
}

async function handleTableAction(event) {
    const button = event.target.closest('button[data-action]');
    if (!button) return;

    const product = state.products.find(item => String(item.id) === button.dataset.productId);
    if (!product) return;

    if (button.dataset.action === 'edit') {
        populateForm(product);
        bootstrapModal.show();
        return;
    }

    if (button.dataset.action === 'delete'
        && window.confirm(`Delete "${product.name}"? This action cannot be undone.`)) {
        button.disabled = true;
        try {
            await deleteProduct(product.id);
            await loadProducts();
        } catch (error) {
            setPageAlert(userMessage(error));
        } finally {
            button.disabled = false;
        }
    }
}

elements.addButton.addEventListener('click', resetForm);
elements.form.addEventListener('submit', submitForm);
elements.tableBody.addEventListener('click', handleTableAction);
elements.search.addEventListener('input', renderTable);
elements.modal.addEventListener('hidden.bs.modal', resetForm);

loadProducts();
