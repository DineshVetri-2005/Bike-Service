// ============================================================
// RevTune console — talks to the Spring Boot REST API on the
// same origin the page is served from.
// ============================================================

const API = ''; // same-origin: served by Spring Boot itself

const state = {
  user: JSON.parse(localStorage.getItem('revtune_user') || 'null'),
  bikes: [],
  services: [],
  bookings: [],
  editingBikeId: null,
};

// ---------- helpers ----------

function toast(message, ok = true) {
  const el = document.getElementById('toast');
  el.textContent = message;
  el.className = `toast show ${ok ? 'ok' : 'err'}`;
  clearTimeout(toast._t);
  toast._t = setTimeout(() => { el.className = 'toast'; }, 3800);
}

async function api(path, options = {}) {
  const res = await fetch(API + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  let body = null;
  const text = await res.text();
  if (text) { try { body = JSON.parse(text); } catch (e) { body = text; } }

  if (!res.ok) {
    const message = (body && body.message) ? body.message : `Request failed (${res.status})`;
    throw new Error(message);
  }
  return body;
}

function saveSession(user) {
  state.user = user;
  localStorage.setItem('revtune_user', JSON.stringify(user));
  renderSession();
}

function clearSession() {
  state.user = null;
  localStorage.removeItem('revtune_user');
  renderSession();
}

function renderSession() {
  const box = document.getElementById('sessionUser');
  if (state.user) {
    box.textContent = `${state.user.name} (#${state.user.id})`;
  } else {
    box.textContent = '— not signed in —';
  }
}

function requireLogin() {
  if (!state.user) {
    toast('Sign in first — go to Account', false);
    return false;
  }
  return true;
}

// ---------- navigation ----------

const sectionTitles = {
  dashboard: 'Dashboard',
  account: 'Account',
  garage: 'Garage',
  catalog: 'Service Catalog',
  bookings: 'Bookings',
};

document.getElementById('nav').addEventListener('click', (e) => {
  const btn = e.target.closest('.nav-item');
  if (!btn) return;
  const section = btn.dataset.section;

  document.querySelectorAll('.nav-item').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');

  document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
  document.getElementById(`sec-${section}`).classList.add('active');

  document.getElementById('pageTitle').textContent = sectionTitles[section];

  if (section === 'account') loadProfile();
  if (section === 'garage') loadBikes();
  if (section === 'catalog') loadServices();
  if (section === 'bookings') { loadBookings(); loadServices(); populateBookingBikeSelect(); }
  if (section === 'dashboard') loadDashboardStats();
});

document.getElementById('signOutBtn').addEventListener('click', () => {
  clearSession();
  toast('Signed out');
});

// ---------- Account: register / login / profile ----------

document.getElementById('registerForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const f = new FormData(e.target);
  const payload = Object.fromEntries(f.entries());
  try {
    const user = await api('/api/users/register', { method: 'POST', body: JSON.stringify(payload) });
    toast(`Account created — welcome, ${user.name}`);
    e.target.reset();
  } catch (err) {
    toast(err.message, false);
  }
});

document.getElementById('loginForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const f = new FormData(e.target);
  const payload = Object.fromEntries(f.entries());
  try {
    const res = await api('/api/users/login', { method: 'POST', body: JSON.stringify(payload) });
    saveSession({ id: res.userId, name: res.name, email: res.email });
    toast(res.message || 'Signed in');
    e.target.reset();
    loadProfile();
  } catch (err) {
    toast(err.message, false);
  }
});

async function loadProfile() {
  const view = document.getElementById('profileView');
  const form = document.getElementById('updateForm');
  if (!state.user) {
    view.innerHTML = '<p class="muted">Sign in to view your profile.</p>';
    form.classList.add('hidden');
    return;
  }
  try {
    const user = await api(`/api/users/${state.user.id}`);
    view.innerHTML = `
      <div class="item-row">
        <div class="item-main">
          <div class="item-title">${user.name}</div>
          <div class="item-sub">${user.email} · ${user.phone}</div>
          <div class="item-sub">${user.address || 'No address on file'}</div>
        </div>
      </div>`;
    form.classList.remove('hidden');
    form.name.value = user.name;
    form.email.value = user.email;
    form.phone.value = user.phone;
    form.address.value = user.address || '';
    form.password.value = '';
  } catch (err) {
    toast(err.message, false);
  }
}

document.getElementById('updateForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  if (!requireLogin()) return;
  const f = new FormData(e.target);
  const payload = Object.fromEntries(f.entries());
  try {
    const user = await api(`/api/users/${state.user.id}`, { method: 'PUT', body: JSON.stringify(payload) });
    saveSession({ id: user.id, name: user.name, email: user.email });
    toast('Profile updated');
    loadProfile();
  } catch (err) {
    toast(err.message, false);
  }
});

// ---------- Garage: bikes ----------

document.getElementById('bikeForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  if (!requireLogin()) return;
  const f = new FormData(e.target);
  const payload = Object.fromEntries(f.entries());
  payload.userId = state.user.id;
  payload.manufacturingYear = Number(payload.manufacturingYear);

  try {
    if (state.editingBikeId) {
      await api(`/api/bikes/${state.editingBikeId}`, { method: 'PUT', body: JSON.stringify(payload) });
      toast('Bike updated');
      state.editingBikeId = null;
    } else {
      await api('/api/bikes', { method: 'POST', body: JSON.stringify(payload) });
      toast('Bike added to garage');
    }
    e.target.reset();
    loadBikes();
  } catch (err) {
    toast(err.message, false);
  }
});

document.getElementById('refreshBikes').addEventListener('click', loadBikes);

async function loadBikes() {
  const list = document.getElementById('bikeList');
  if (!state.user) {
    list.innerHTML = '<p class="muted">Sign in to see your garage.</p>';
    return;
  }
  try {
    const bikes = await api(`/api/bikes/user/${state.user.id}`);
    state.bikes = bikes;
    if (bikes.length === 0) {
      list.innerHTML = '<p class="muted">No bikes yet — add your first one.</p>';
    } else {
      list.innerHTML = bikes.map(b => `
        <div class="item-row">
          <div class="item-main">
            <div class="item-title">${b.brand} ${b.model}</div>
            <div class="item-sub">${b.bikeNumber} · ${b.bikeType} · ${b.manufacturingYear}</div>
          </div>
          <div class="item-actions">
            <button class="btn-ghost small" onclick="editBike(${b.id})">Edit</button>
            <button class="btn-danger-outline" onclick="deleteBike(${b.id})">Delete</button>
          </div>
        </div>
      `).join('');
    }
    populateBookingBikeSelect();
    loadDashboardStats();
  } catch (err) {
    toast(err.message, false);
  }
}

function editBike(id) {
  const bike = state.bikes.find(b => b.id === id);
  if (!bike) return;
  state.editingBikeId = id;
  const form = document.getElementById('bikeForm');
  form.bikeNumber.value = bike.bikeNumber;
  form.brand.value = bike.brand;
  form.model.value = bike.model;
  form.bikeType.value = bike.bikeType;
  form.manufacturingYear.value = bike.manufacturingYear;
  toast('Editing bike — submit the form to save changes');
}

async function deleteBike(id) {
  try {
    await api(`/api/bikes/${id}`, { method: 'DELETE' });
    toast('Bike removed');
    loadBikes();
  } catch (err) {
    toast(err.message, false);
  }
}

// ---------- Service catalog ----------

document.getElementById('serviceForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const f = new FormData(e.target);
  const payload = Object.fromEntries(f.entries());
  payload.price = Number(payload.price);
  try {
    await api('/api/services', { method: 'POST', body: JSON.stringify(payload) });
    toast('Service added to menu');
    e.target.reset();
    loadServices();
  } catch (err) {
    toast(err.message, false);
  }
});

document.getElementById('refreshServices').addEventListener('click', loadServices);

async function loadServices() {
  const list = document.getElementById('serviceList');
  try {
    const services = await api('/api/services');
    state.services = services;
    list.innerHTML = services.map(s => `
      <div class="item-row">
        <div class="item-main">
          <div class="item-title">${s.serviceName} — ₹${s.price}</div>
          <div class="item-sub">${s.description || 'No description'} · ${s.estimatedTime}</div>
        </div>
      </div>
    `).join('') || '<p class="muted">No services yet.</p>';
    populateBookingServiceSelect();
    loadDashboardStats();
  } catch (err) {
    toast(err.message, false);
  }
}

function populateBookingServiceSelect() {
  const sel = document.getElementById('bookingServiceSelect');
  if (!sel) return;
  sel.innerHTML = state.services.map(s => `<option value="${s.id}">${s.serviceName} — ₹${s.price}</option>`).join('')
    || '<option value="">No services available</option>';
}

function populateBookingBikeSelect() {
  const sel = document.getElementById('bookingBikeSelect');
  if (!sel) return;
  if (!state.user || state.bikes.length === 0) {
    sel.innerHTML = '<option value="">Sign in and add a bike first</option>';
    return;
  }
  sel.innerHTML = state.bikes.map(b => `<option value="${b.id}">${b.brand} ${b.model} (${b.bikeNumber})</option>`).join('');
}

// ---------- Bookings ----------

document.getElementById('bookingForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  if (!requireLogin()) return;
  const f = new FormData(e.target);
  const payload = Object.fromEntries(f.entries());
  payload.userId = state.user.id;
  payload.bikeId = Number(payload.bikeId);
  payload.serviceId = Number(payload.serviceId);
  payload.bookingTime = payload.bookingTime.length === 5 ? payload.bookingTime + ':00' : payload.bookingTime;

  try {
    await api('/api/bookings', { method: 'POST', body: JSON.stringify(payload) });
    toast('Booking confirmed');
    e.target.reset();
    loadBookings();
  } catch (err) {
    toast(err.message, false);
  }
});

document.getElementById('refreshBookings').addEventListener('click', loadBookings);

const badgeClass = {
  BOOKED: 'badge-booked',
  IN_PROGRESS: 'badge-progress',
  COMPLETED: 'badge-completed',
  CANCELLED: 'badge-cancelled',
};

async function loadBookings() {
  const list = document.getElementById('bookingList');
  if (!state.user) {
    list.innerHTML = '<p class="muted">Sign in to see your bookings.</p>';
    return;
  }
  try {
    const bookings = await api(`/api/bookings/user/${state.user.id}`);
    state.bookings = bookings;
    if (bookings.length === 0) {
      list.innerHTML = '<p class="muted">No bookings yet.</p>';
    } else {
      list.innerHTML = bookings.map(b => `
        <div class="item-row">
          <div class="item-main">
            <div class="item-title">Booking #${b.id} <span class="badge ${badgeClass[b.status] || ''}">${b.status}</span></div>
            <div class="item-sub">${b.bookingDate} at ${b.bookingTime} · ₹${b.totalAmount}</div>
          </div>
          <div class="item-actions">
            <select class="status-select" onchange="updateBookingStatus(${b.id}, this.value)">
              ${['BOOKED','IN_PROGRESS','COMPLETED','CANCELLED'].map(s => `<option value="${s}" ${s===b.status?'selected':''}>${s}</option>`).join('')}
            </select>
            <button class="btn-danger-outline" onclick="deleteBooking(${b.id})">Delete</button>
          </div>
        </div>
      `).join('');
    }
    loadDashboardStats();
  } catch (err) {
    toast(err.message, false);
  }
}

async function updateBookingStatus(id, status) {
  try {
    await api(`/api/bookings/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }) });
    toast(`Booking #${id} marked ${status}`);
    loadBookings();
  } catch (err) {
    toast(err.message, false);
  }
}

async function deleteBooking(id) {
  try {
    await api(`/api/bookings/${id}`, { method: 'DELETE' });
    toast('Booking deleted');
    loadBookings();
  } catch (err) {
    toast(err.message, false);
  }
}

// ---------- Dashboard stats ----------

async function loadDashboardStats() {
  document.getElementById('statBikes').textContent = state.user ? state.bikes.length : '—';
  document.getElementById('statServices').textContent = state.services.length || '—';
  document.getElementById('statBookings').textContent = state.user ? state.bookings.length : '—';
}

// ---------- init ----------

(function init() {
  document.getElementById('apiBase').textContent = window.location.host;
  renderSession();
  loadServices();
  if (state.user) { loadBikes(); loadBookings(); }
  loadDashboardStats();
})();
