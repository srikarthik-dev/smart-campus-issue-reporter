/**
 * admin.js — Admin dashboard and issue management page logic.
 * Handles real-time statistics, charts, all issues table, and admin actions.
 */

let allIssues = [];
let categoryChart, statusChart, priorityChart;

document.addEventListener('DOMContentLoaded', () => {
  requireRole('admin');
  renderSidebar('admin', 'dashboard');
  updateClock();
  setInterval(updateClock, 60000);

  document.getElementById('searchBtn').addEventListener('click', applyFilters);
  document.getElementById('clearBtn').addEventListener('click', clearFilters);
  document.getElementById('searchInput').addEventListener('keyup', e => {
    if (e.key === 'Enter') applyFilters();
  });

  // Modal close buttons
  document.getElementById('closeAssignModal').addEventListener('click', () => closeModal('assignModal'));
  document.getElementById('closeResolveModal').addEventListener('click', () => closeModal('resolveModal'));
  document.getElementById('cancelAssignBtn').addEventListener('click', () => closeModal('assignModal'));
  document.getElementById('cancelResolveBtn').addEventListener('click', () => closeModal('resolveModal'));
  document.getElementById('confirmAssignBtn').addEventListener('click', confirmAssign);
  document.getElementById('confirmResolveBtn').addEventListener('click', confirmResolve);

  loadDashboard();
  loadAllIssues();
});

async function loadDashboard() {
  try {
    const summary = await DashboardAPI.getSummary();
    renderStats(summary);
    renderCharts(summary);
  } catch (err) {
    console.error('Dashboard load failed:', err.message);
  }
}

function renderStats(summary) {
  const el = document.getElementById('statsGrid');
  if (!el) return;
  el.innerHTML = `
    <div class="stat-card total fade-in-up">
      <div class="stat-icon">📊</div>
      <div class="stat-value">${summary.totalIssues}</div>
      <div class="stat-label">Total Issues</div>
    </div>
    <div class="stat-card open fade-in-up">
      <div class="stat-icon">📋</div>
      <div class="stat-value">${summary.openIssues}</div>
      <div class="stat-label">Open</div>
    </div>
    <div class="stat-card progress fade-in-up">
      <div class="stat-icon">⚙️</div>
      <div class="stat-value">${summary.assignedIssues + summary.inProgressIssues}</div>
      <div class="stat-label">In Progress</div>
    </div>
    <div class="stat-card resolved fade-in-up">
      <div class="stat-icon">✅</div>
      <div class="stat-value">${summary.resolvedIssues}</div>
      <div class="stat-label">Resolved</div>
    </div>
    <div class="stat-card critical fade-in-up">
      <div class="stat-icon">🔴</div>
      <div class="stat-value">${summary.criticalIssues}</div>
      <div class="stat-label">Critical</div>
    </div>
    <div class="stat-card high fade-in-up">
      <div class="stat-icon">🟠</div>
      <div class="stat-value">${summary.highPriorityIssues}</div>
      <div class="stat-label">High Priority</div>
    </div>`;
}

function renderCharts(summary) {
  // Category Chart
  const catCtx = document.getElementById('categoryChart');
  if (catCtx) {
    if (categoryChart) categoryChart.destroy();
    const catLabels = Object.keys(summary.byCategory);
    const catData   = Object.values(summary.byCategory);
    categoryChart = new Chart(catCtx, {
      type: 'doughnut',
      data: {
        labels: catLabels,
        datasets: [{
          data: catData,
          backgroundColor: ['#f59e0b','#3b82f6','#8b5cf6','#06b6d4','#10b981','#ef4444','#6b7280'],
          borderWidth: 0,
          hoverOffset: 8
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { position: 'right', labels: { font: { family: 'Inter', size: 11 }, padding: 12 } } }
      }
    });
  }

  // Status Chart
  const statCtx = document.getElementById('statusChart');
  if (statCtx) {
    if (statusChart) statusChart.destroy();
    const statLabels = Object.keys(summary.byStatus);
    const statData   = Object.values(summary.byStatus);
    const statColors = { OPEN:'#6366f1', ASSIGNED:'#0891b2', IN_PROGRESS:'#d97706', RESOLVED:'#16a34a', CLOSED:'#6b7280' };
    statusChart = new Chart(statCtx, {
      type: 'bar',
      data: {
        labels: statLabels.map(l => l.replace('_',' ')),
        datasets: [{
          label: 'Issues',
          data: statData,
          backgroundColor: statLabels.map(l => statColors[l] + 'cc'),
          borderColor:     statLabels.map(l => statColors[l]),
          borderWidth: 2,
          borderRadius: 6,
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: { beginAtZero: true, ticks: { stepSize: 1, font: { family: 'Inter', size: 11 } }, grid: { color: '#f1f5f9' } },
          x: { ticks: { font: { family: 'Inter', size: 11 } }, grid: { display: false } }
        }
      }
    });
  }

  // Priority Chart
  const priCtx = document.getElementById('priorityChart');
  if (priCtx) {
    if (priorityChart) priorityChart.destroy();
    const priLabels = Object.keys(summary.byPriority);
    const priData   = Object.values(summary.byPriority);
    const priColors = { LOW:'#16a34a', MEDIUM:'#d97706', HIGH:'#ea580c', CRITICAL:'#dc2626' };
    priorityChart = new Chart(priCtx, {
      type: 'pie',
      data: {
        labels: priLabels,
        datasets: [{
          data: priData,
          backgroundColor: priLabels.map(l => priColors[l] + 'cc'),
          borderColor:     priLabels.map(l => priColors[l]),
          borderWidth: 2,
          hoverOffset: 8
        }]
      },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { position: 'right', labels: { font: { family: 'Inter', size: 11 }, padding: 12 } } }
      }
    });
  }
}

async function loadAllIssues() {
  showLoading('issuesContainer', 'Loading all issues…');
  try {
    allIssues = await IssueAPI.getAll();
    renderAttentionSection(allIssues);
    renderIssuesTable(allIssues);
  } catch (err) {
    showError('issuesContainer', err.message);
  }
}

function renderAttentionSection(issues) {
  const container = document.getElementById('attentionContainer');
  const card = document.getElementById('attentionCard');
  if (!container || !card) return;

  // Filter criteria: Priority CRITICAL/HIGH OR SLA 7+ days, and NOT CLOSED/RESOLVED
  const attentionIssues = issues.filter(issue => {
    if (issue.status === 'RESOLVED' || issue.status === 'CLOSED') return false;
    return issue.priority === 'CRITICAL' || issue.priority === 'HIGH' || issue.slaIndicator === '7+ days';
  });

  if (attentionIssues.length === 0) {
    card.style.display = 'none';
    return;
  }

  card.style.display = 'block';

  container.innerHTML = `
    <div class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Code</th>
            <th>Category</th>
            <th>Location</th>
            <th>Priority</th>
            <th>SLA</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${attentionIssues.map(issue => `
            <tr style="background:#fff5f5">
              <td class="issue-code-cell" onclick="window.location.href='/issue-details.html?id=${issue.id}'">${issue.issueCode}</td>
              <td>${CATEGORY_ICONS[issue.category]} ${issue.category}</td>
              <td class="truncate" style="max-width:150px">${issue.location}</td>
              <td>${priorityBadge(issue.priority)}</td>
              <td><span class="badge" style="background:#fee2e2;color:#991b1b;border:1px solid #f87171">⏱ ${issue.slaIndicator || 'Unknown'}</span></td>
              <td>${statusBadge(issue.status)}</td>
              <td>
                <div class="d-flex gap-2">
                  ${issue.status === 'OPEN' || issue.status === 'ASSIGNED' ? `<button class="btn btn-sm btn-secondary" onclick="openAssignModal(${issue.id}, '${issue.status}')">👤 Assign</button>` : ''}
                  ${NEXT_STATUS[issue.status] && issue.status !== 'IN_PROGRESS' ? `<button class="btn btn-sm btn-primary" onclick="quickAdvance(${issue.id}, '${issue.status}')">→ ${NEXT_STATUS[issue.status]}</button>` : ''}
                  ${issue.status === 'IN_PROGRESS' ? `<button class="btn btn-sm btn-success" onclick="openResolveModal(${issue.id})">✅ Resolve</button>` : ''}
                </div>
              </td>
            </tr>`).join('')}
        </tbody>
      </table>
    </div>`;
}

async function applyFilters() {
  const params = {
    keyword:  document.getElementById('searchInput').value,
    status:   document.getElementById('statusFilter').value,
    priority: document.getElementById('priorityFilter').value,
    category: document.getElementById('categoryFilter').value,
  };

  showLoading('issuesContainer', 'Searching…');
  try {
    const results = await IssueAPI.search(params);
    renderIssuesTable(results);
  } catch (err) {
    showError('issuesContainer', err.message);
  }
}

function clearFilters() {
  document.getElementById('searchInput').value = '';
  document.getElementById('statusFilter').value = '';
  document.getElementById('priorityFilter').value = '';
  document.getElementById('categoryFilter').value = '';
  renderIssuesTable(allIssues);
}

function renderIssuesTable(issues) {
  const container = document.getElementById('issuesContainer');

  if (issues.length === 0) {
    showEmpty('issuesContainer', 'No issues found', 'Try adjusting your search filters.');
    return;
  }

  container.innerHTML = `
    <div class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Code</th>
            <th>Category</th>
            <th>Location</th>
            <th>Reported By</th>
            <th>Score</th>
            <th>Priority</th>
            <th>Status</th>
            <th>Assigned To</th>
            <th>Reported</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          ${issues.map(issue => `
            <tr>
              <td class="issue-code-cell" onclick="window.location.href='/issue-details.html?id=${issue.id}'">${issue.issueCode}</td>
              <td>${CATEGORY_ICONS[issue.category]} ${issue.category}</td>
              <td style="max-width:130px" class="truncate">${issue.location}</td>
              <td>${issue.reportedBy}</td>
              <td><span class="badge" style="background:#f1f5f9;color:#475569;border:1px solid #e2e8f0">${issue.priorityScore || 0}</span></td>
              <td>${priorityBadge(issue.priority)}</td>
              <td>${statusBadge(issue.status)}</td>
              <td>${issue.assignedTo || '<span class="text-muted">—</span>'}</td>
              <td class="text-muted">${relativeTime(issue.reportedAt)}</td>
              <td>
                <div class="d-flex gap-2">
                  ${issue.status === 'OPEN' || issue.status === 'ASSIGNED' ? `<button class="btn btn-sm btn-secondary" onclick="openAssignModal(${issue.id}, '${issue.status}')">👤 Assign</button>` : ''}
                  ${NEXT_STATUS[issue.status] && issue.status !== 'IN_PROGRESS' && issue.status !== 'RESOLVED' ? `<button class="btn btn-sm btn-primary" onclick="quickAdvance(${issue.id}, '${issue.status}')">→ ${NEXT_STATUS[issue.status]}</button>` : ''}
                  ${issue.status === 'IN_PROGRESS' ? `<button class="btn btn-sm btn-success" onclick="openResolveModal(${issue.id})">✅ Resolve</button>` : ''}
                  ${issue.status === 'RESOLVED' ? `<button class="btn btn-sm btn-secondary" onclick="closeIssue(${issue.id})">🔒 Close</button>` : ''}
                  <button class="btn btn-sm btn-danger" onclick="deleteIssue(${issue.id})">🗑</button>
                </div>
              </td>
            </tr>`).join('')}
        </tbody>
      </table>
    </div>
    <div class="mt-3 text-small text-muted">Showing ${issues.length} issue${issues.length !== 1 ? 's' : ''}</div>`;
}

// --- Admin Actions ---

let currentIssueId = null;

function openAssignModal(issueId, currentStatus) {
  currentIssueId = issueId;
  document.getElementById('assignStaffName').value = '';
  showModal('assignModal');
}

function openResolveModal(issueId) {
  currentIssueId = issueId;
  document.getElementById('resolutionNotesInput').value = '';
  showModal('resolveModal');
}

async function confirmAssign() {
  const staffName = document.getElementById('assignStaffName').value.trim();
  if (!staffName) {
    alert('Please enter a staff member name.');
    return;
  }
  try {
    await IssueAPI.assign(currentIssueId, { assignedTo: staffName });
    closeModal('assignModal');
    await loadDashboard();
    await loadAllIssues();
  } catch (err) {
    alert('Assignment failed: ' + err.message);
  }
}

async function confirmResolve() {
  const notes = document.getElementById('resolutionNotesInput').value.trim();
  if (!notes || notes.length < 5) {
    alert('Please provide detailed resolution notes (min 5 characters).');
    return;
  }
  try {
    await IssueAPI.resolve(currentIssueId, { resolutionNotes: notes });
    closeModal('resolveModal');
    await loadDashboard();
    await loadAllIssues();
  } catch (err) {
    alert('Resolution failed: ' + err.message);
  }
}

async function quickAdvance(issueId, currentStatus) {
  const next = NEXT_STATUS[currentStatus];
  if (!next) return;
  if (!confirmAction(`Move issue to ${next.replace('_', ' ')}?`)) return;
  try {
    await IssueAPI.updateStatus(issueId, { status: next });
    await loadDashboard();
    await loadAllIssues();
  } catch (err) {
    alert('Status update failed: ' + err.message);
  }
}

async function closeIssue(issueId) {
  if (!confirmAction('Close this issue? This cannot be undone.')) return;
  try {
    await IssueAPI.updateStatus(issueId, { status: 'CLOSED' });
    await loadDashboard();
    await loadAllIssues();
  } catch (err) {
    alert('Failed to close issue: ' + err.message);
  }
}

async function deleteIssue(issueId) {
  if (!confirmAction('⚠️ Permanently delete this issue? This cannot be undone.')) return;
  try {
    await IssueAPI.delete(issueId);
    await loadDashboard();
    await loadAllIssues();
  } catch (err) {
    alert('Delete failed: ' + err.message);
  }
}

function showModal(id) {
  document.getElementById(id).classList.add('visible');
}

function closeModal(id) {
  document.getElementById(id).classList.remove('visible');
}
