/**
 * api.js — Centralized API client for the Smart Campus Issue Reporter.
 * All backend communication goes through these functions.
 */

const API_BASE = '/api';

/**
 * Core fetch wrapper with consistent error handling.
 */
async function apiRequest(method, path, body = null) {
  const options = {
    method,
    headers: { 'Content-Type': 'application/json' }
  };
  if (body) options.body = JSON.stringify(body);

  const response = await fetch(`${API_BASE}${path}`, options);

  if (!response.ok) {
    let errorData;
    try { errorData = await response.json(); } catch { errorData = {}; }
    const message = errorData.message || `HTTP ${response.status}: ${response.statusText}`;
    const err = new Error(message);
    err.status = response.status;
    err.fieldErrors = errorData.fieldErrors || null;
    throw err;
  }

  if (response.status === 204) return null;
  return response.json();
}

// --- Issue CRUD ---
const IssueAPI = {
  getAll:    ()         => apiRequest('GET',    '/issues'),
  getById:   (id)       => apiRequest('GET',    `/issues/${id}`),
  create:    (data)     => apiRequest('POST',   '/issues', data),
  update:    (id, data) => apiRequest('PUT',    `/issues/${id}`, data),
  delete:    (id)       => apiRequest('DELETE', `/issues/${id}`),

  assign:       (id, data)   => apiRequest('PUT', `/issues/${id}/assign`, data),
  updateStatus: (id, data)   => apiRequest('PUT', `/issues/${id}/status`, data),
  resolve:      (id, data)   => apiRequest('PUT', `/issues/${id}/resolve`, data),

  search: (params) => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => { if (v) query.append(k, v); });
    return apiRequest('GET', `/issues/search?${query.toString()}`);
  }
};

// --- Dashboard ---
const DashboardAPI = {
  getSummary:    () => apiRequest('GET', '/dashboard/summary'),
  getByCategory: () => apiRequest('GET', '/dashboard/by-category'),
  getByStatus:   () => apiRequest('GET', '/dashboard/by-status'),
  getByPriority: () => apiRequest('GET', '/dashboard/by-priority'),
};

// --- Shared Utilities ---

/**
 * Returns HTML for a priority badge with correct class.
 */
function priorityBadge(priority) {
  const icons = { CRITICAL: '🔴', HIGH: '🟠', MEDIUM: '🟡', LOW: '🟢' };
  return `<span class="badge badge-priority-${priority}">${icons[priority] || ''} ${priority}</span>`;
}

/**
 * Returns HTML for a status badge with correct class.
 */
function statusBadge(status) {
  const icons = {
    OPEN: '📋', ASSIGNED: '👤', IN_PROGRESS: '⚙️', RESOLVED: '✅', CLOSED: '🔒'
  };
  const label = status.replace('_', ' ');
  return `<span class="badge badge-status-${status}">${icons[status] || ''} ${label}</span>`;
}

/**
 * Formats an ISO datetime string for display.
 */
function formatDate(isoString) {
  if (!isoString) return '—';
  return new Date(isoString).toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

/**
 * Returns a relative time string (e.g., "2 hours ago").
 */
function relativeTime(isoString) {
  if (!isoString) return '—';
  const diff = Date.now() - new Date(isoString).getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return 'just now';
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}

/**
 * Returns current role from localStorage. Defaults to 'student'.
 */
function getRole() {
  return localStorage.getItem('campus_role') || 'student';
}

function setRole(role) {
  localStorage.setItem('campus_role', role);
}

/**
 * Redirects to landing page if no role set (or if role doesn't match page requirement).
 */
function requireRole(required) {
  const role = getRole();
  if (!role) { window.location.href = '/index.html'; return; }
  if (required && role !== required) {
    window.location.href = role === 'admin' ? '/admin.html' : '/student.html';
  }
}

/**
 * Shows an alert element with a message.
 */
function showAlert(elementId, message, type = 'error') {
  const el = document.getElementById(elementId);
  if (!el) return;
  el.className = `alert alert-${type}`;
  el.innerHTML = `
    <span class="alert-icon">${type === 'error' ? '❌' : type === 'success' ? '✅' : 'ℹ️'}</span>
    <div>
      <div class="alert-title">${type === 'error' ? 'Error' : type === 'success' ? 'Success' : 'Info'}</div>
      <div class="alert-body">${message}</div>
    </div>`;
  el.style.display = 'flex';
  el.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

function hideAlert(elementId) {
  const el = document.getElementById(elementId);
  if (el) el.style.display = 'none';
}

/**
 * Shows a loading state inside a container.
 */
function showLoading(containerId, message = 'Loading...') {
  const el = document.getElementById(containerId);
  if (el) {
    el.innerHTML = `
      <div class="state-container">
        <div class="spinner"></div>
        <div class="state-message">${message}</div>
      </div>`;
  }
}

/**
 * Shows an empty state inside a container.
 */
function showEmpty(containerId, title = 'No issues found', message = '') {
  const el = document.getElementById(containerId);
  if (el) {
    el.innerHTML = `
      <div class="state-container">
        <div class="state-icon">📭</div>
        <div class="state-title">${title}</div>
        <div class="state-message">${message}</div>
      </div>`;
  }
}

/**
 * Shows an error state inside a container.
 */
function showError(containerId, message = 'Failed to load data') {
  const el = document.getElementById(containerId);
  if (el) {
    el.innerHTML = `
      <div class="state-container">
        <div class="state-icon">⚠️</div>
        <div class="state-title">Something went wrong</div>
        <div class="state-message">${message}</div>
      </div>`;
  }
}

/**
 * Confirmation dialog before destructive actions.
 */
function confirmAction(message) {
  return window.confirm(message);
}

/**
 * Truncates text to a given length.
 */
function truncate(text, maxLen = 80) {
  if (!text) return '—';
  return text.length > maxLen ? text.substring(0, maxLen) + '…' : text;
}

/**
 * Category icon map.
 */
const CATEGORY_ICONS = {
  ELECTRICAL: '⚡', PLUMBING: '🚰', CLASSROOM: '🏫',
  WIFI: '📶', CLEANLINESS: '🧹', SAFETY: '🦺', OTHER: '📌'
};

/**
 * Next valid status transitions map (for UI buttons).
 */
const NEXT_STATUS = {
  OPEN:        'ASSIGNED',
  ASSIGNED:    'IN_PROGRESS',
  IN_PROGRESS: 'RESOLVED',
  RESOLVED:    'CLOSED',
  CLOSED:      null
};
