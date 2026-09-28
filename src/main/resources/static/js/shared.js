/**
 * Shared helper: renders the role-appropriate sidebar and clock.
 * Called by every page after requireRole().
 */

function renderSidebar(role, activePage) {
  const isAdmin = role === 'admin';

  const studentNav = `
    <div class="nav-section-label">Student</div>
    <a href="/student.html" class="nav-item ${activePage === 'dashboard' ? 'active' : ''}">
      <span class="nav-icon">🏠</span> Dashboard
    </a>
    <a href="/report.html" class="nav-item ${activePage === 'report' ? 'active' : ''}">
      <span class="nav-icon">📝</span> Report Issue
    </a>
    <a href="/my-issues.html" class="nav-item ${activePage === 'my-issues' ? 'active' : ''}">
      <span class="nav-icon">📋</span> My Issues
    </a>`;

  const adminNav = `
    <div class="nav-section-label">Admin</div>
    <a href="/admin.html" class="nav-item ${activePage === 'dashboard' ? 'active' : ''}">
      <span class="nav-icon">📊</span> Dashboard
    </a>
    <div class="nav-section-label">Tools</div>
    <a href="/admin.html#issues" class="nav-item">
      <span class="nav-icon">🗂️</span> All Issues
    </a>`;

  const sidebarHTML = `
    <div class="sidebar-brand">
      <a href="/index.html" class="brand-logo">
        <div class="logo-icon">🏫</div>
        <div>
          <div class="logo-text">Smart Campus</div>
          <div class="logo-sub">Issue Reporter</div>
        </div>
      </a>
    </div>
    <div class="sidebar-role-badge ${isAdmin ? 'admin' : 'student'}">
      ${isAdmin ? '🔐 Administrator' : '🎓 Student'}
    </div>
    <nav class="sidebar-nav">
      ${isAdmin ? adminNav : studentNav}
    </nav>
    <div class="sidebar-footer">
      <a href="/index.html" class="btn-switch-role" onclick="localStorage.removeItem('campus_role')">
        🔄 Switch Role
      </a>
    </div>`;

  const sidebar = document.getElementById('sidebar');
  if (sidebar) sidebar.innerHTML = sidebarHTML;
}

function updateClock() {
  const el = document.getElementById('topbarClock');
  if (el) {
    el.textContent = new Date().toLocaleString('en-IN', {
      weekday: 'short', day: 'numeric', month: 'short',
      hour: '2-digit', minute: '2-digit'
    });
  }
}
