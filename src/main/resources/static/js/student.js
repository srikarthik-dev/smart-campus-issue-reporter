/**
 * student.js — Student dashboard page logic.
 * Displays student's personal issue summary and recent issues.
 */

document.addEventListener('DOMContentLoaded', () => {
  requireRole('student');
  renderSidebar('student', 'dashboard');
  updateClock();
  setInterval(updateClock, 60000);
  loadStudentDashboard();
});

async function loadStudentDashboard() {
  const reporterName = localStorage.getItem('campus_reporter_name') || '';

  // Greet user
  if (reporterName) {
    const greetEl = document.getElementById('studentGreeting');
    if (greetEl) greetEl.textContent = `Welcome back, ${reporterName.split(' ')[0]}!`;
  }

  showLoading('recentIssuesContainer', 'Loading your issues…');
  showLoading('statusSummaryContainer', 'Calculating…');

  try {
    const params = reporterName ? { reportedBy: reporterName } : {};
    const allIssues = await IssueAPI.search(params);

    renderStudentStats(allIssues);
    renderRecentIssues(allIssues.slice(0, 5));
  } catch (err) {
    showError('recentIssuesContainer', err.message);
    showError('statusSummaryContainer', err.message);
  }
}

function renderStudentStats(issues) {
  const total    = issues.length;
  const open     = issues.filter(i => i.status === 'OPEN').length;
  const progress = issues.filter(i => ['ASSIGNED','IN_PROGRESS'].includes(i.status)).length;
  const resolved = issues.filter(i => ['RESOLVED','CLOSED'].includes(i.status)).length;

  const el = document.getElementById('studentStats');
  if (!el) return;
  el.innerHTML = `
    <div class="stat-card total fade-in-up">
      <div class="stat-icon">📊</div>
      <div class="stat-value">${total}</div>
      <div class="stat-label">My Issues</div>
    </div>
    <div class="stat-card open fade-in-up">
      <div class="stat-icon">📋</div>
      <div class="stat-value">${open}</div>
      <div class="stat-label">Open</div>
    </div>
    <div class="stat-card progress fade-in-up">
      <div class="stat-icon">⚙️</div>
      <div class="stat-value">${progress}</div>
      <div class="stat-label">In Progress</div>
    </div>
    <div class="stat-card resolved fade-in-up">
      <div class="stat-icon">✅</div>
      <div class="stat-value">${resolved}</div>
      <div class="stat-label">Resolved</div>
    </div>
    ${issues.some(i => i.priority === 'CRITICAL' || i.priority === 'HIGH') ? `
    <div class="stat-card critical fade-in-up" style="background:#fef2f2;border:1px solid #fecaca">
      <div class="stat-icon">🔴</div>
      <div class="stat-value">${issues.filter(i => i.priority === 'CRITICAL' || i.priority === 'HIGH').length}</div>
      <div class="stat-label" style="color:#dc2626">High/Critical</div>
    </div>` : ''}`;

  const statusEl = document.getElementById('statusSummaryContainer');
  if (statusEl) {
    if (total === 0) {
      statusEl.innerHTML = `
        <div class="state-container">
          <div class="state-icon">🎉</div>
          <div class="state-title">All clear!</div>
          <div class="state-message">You haven't reported any issues yet.</div>
        </div>`;
    } else {
      const breakdown = [
        { label: 'Open', count: open, pct: Math.round(open/total*100), color: 'var(--color-open)' },
        { label: 'In Progress', count: progress, pct: Math.round(progress/total*100), color: 'var(--color-in-progress)' },
        { label: 'Resolved', count: resolved, pct: Math.round(resolved/total*100), color: 'var(--color-resolved)' },
      ];
      statusEl.innerHTML = breakdown.map(b => `
        <div style="margin-bottom:12px">
          <div class="d-flex justify-between mb-1">
            <span class="fw-600 text-small">${b.label}</span>
            <span class="text-small text-muted">${b.count} (${b.pct}%)</span>
          </div>
          <div style="background:var(--color-border);border-radius:4px;height:8px;overflow:hidden">
            <div style="width:${b.pct}%;background:${b.color};height:100%;border-radius:4px;transition:width 0.6s ease"></div>
          </div>
        </div>`).join('');
    }
  }
}

function renderRecentIssues(issues) {
  const container = document.getElementById('recentIssuesContainer');
  if (!container) return;

  if (issues.length === 0) {
    showEmpty('recentIssuesContainer', 'No issues yet',
      'Click "Report Issue" to submit your first campus issue report.');
    return;
  }

  container.innerHTML = `
    <div class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Issue Code</th>
            <th>Category</th>
            <th>Location</th>
            <th>Priority</th>
            <th>Status</th>
            <th>Reported</th>
          </tr>
        </thead>
        <tbody>
          ${issues.map(issue => `
            <tr onclick="window.location.href='/issue-details.html?id=${issue.id}'">
              <td class="issue-code-cell">${issue.issueCode}</td>
              <td>${CATEGORY_ICONS[issue.category]} ${issue.category}</td>
              <td class="truncate" style="max-width:160px">${issue.location}</td>
              <td>${priorityBadge(issue.priority)}</td>
              <td>${statusBadge(issue.status)}</td>
              <td class="text-muted">${relativeTime(issue.reportedAt)}</td>
            </tr>`).join('')}
        </tbody>
      </table>
    </div>`;
}
