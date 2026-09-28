/**
 * issue-details.js — Full issue detail page.
 * Shows all issue fields, status timeline, priority breakdown,
 * and provides admin actions when in admin role.
 */

let currentIssue = null;

document.addEventListener('DOMContentLoaded', () => {
  const role = getRole();
  if (!role) { window.location.href = '/index.html'; return; }

  renderSidebar(role, null);
  updateClock();
  setInterval(updateClock, 60000);

  const params = new URLSearchParams(window.location.search);
  const issueId = params.get('id');

  if (!issueId) {
    showError('detailsContainer', 'No issue ID specified in URL.');
    return;
  }

  loadIssueDetails(issueId);

  // Modal handlers
  const closeAssign  = document.getElementById('closeAssignModal');
  const closeResolve = document.getElementById('closeResolveModal');
  if (closeAssign)  closeAssign.addEventListener('click', () => closeModal('assignModal'));
  if (closeResolve) closeResolve.addEventListener('click', () => closeModal('resolveModal'));

  const cancelAssign  = document.getElementById('cancelAssignBtn');
  const cancelResolve = document.getElementById('cancelResolveBtn');
  if (cancelAssign)  cancelAssign.addEventListener('click', () => closeModal('assignModal'));
  if (cancelResolve) cancelResolve.addEventListener('click', () => closeModal('resolveModal'));

  const confirmAssign  = document.getElementById('confirmAssignBtn');
  const confirmResolve = document.getElementById('confirmResolveBtn');
  if (confirmAssign)  confirmAssign.addEventListener('click', doAssign);
  if (confirmResolve) confirmResolve.addEventListener('click', doResolve);
});

async function loadIssueDetails(issueId) {
  showLoading('detailsContainer', 'Loading issue details…');
  try {
    currentIssue = await IssueAPI.getById(issueId);
    renderIssueDetails(currentIssue);
  } catch (err) {
    showError('detailsContainer', err.message);
  }
}

function renderIssueDetails(issue) {
  const role = getRole();
  const isAdmin = role === 'admin';

  document.title = `${issue.issueCode} — Smart Campus`;
  document.getElementById('pageTitle').textContent = issue.issueCode;
  document.getElementById('pageSubtitle').textContent = issue.location;

  const container = document.getElementById('detailsContainer');
  container.innerHTML = `
    <!-- Top metadata row -->
    <div class="d-flex gap-3 align-center mb-4" style="flex-wrap:wrap">
      ${priorityBadge(issue.priority)}
      ${statusBadge(issue.status)}
      ${issue.safetyImpact ? '<span class="badge" style="background:#fef2f2;color:#dc2626;border:1px solid rgba(220,38,38,0.2)">⚠️ Safety Risk</span>' : ''}
      <span class="badge" style="background:var(--color-surface-2);color:var(--text-main);border:1px solid var(--border-color)">⏱ SLA: ${issue.slaIndicator || 'Unknown'}</span>
      <span class="text-small text-muted">Reported ${relativeTime(issue.reportedAt)}</span>
    </div>

    <!-- Main grid -->
    <div style="display:grid;grid-template-columns:1fr 380px;gap:20px">
      <!-- Left column -->
      <div style="display:flex;flex-direction:column;gap:20px">

        <!-- Issue Info -->
        <div class="card">
          <div class="card-header">
            <div>
              <div class="card-title">Issue Information</div>
              <div class="card-subtitle">${CATEGORY_ICONS[issue.category]} ${issue.category}</div>
            </div>
          </div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px">
            <div>
              <div class="form-label">Issue Code</div>
              <div class="font-mono fw-600" style="color:var(--color-primary)">${issue.issueCode}</div>
            </div>
            <div>
              <div class="form-label">Reported By</div>
              <div class="fw-600">${issue.reportedBy}</div>
            </div>
            <div>
              <div class="form-label">Location</div>
              <div>${issue.location}</div>
            </div>
            <div>
              <div class="form-label">Affected Users</div>
              <div>👥 ${issue.affectedUsers} people</div>
            </div>
            <div>
              <div class="form-label">Reported At</div>
              <div class="text-small">${formatDate(issue.reportedAt)}</div>
            </div>
            <div>
              <div class="form-label">Last Updated</div>
              <div class="text-small">${formatDate(issue.updatedAt)}</div>
            </div>
            ${issue.resolvedAt ? `
            <div>
              <div class="form-label">Resolved At</div>
              <div class="text-small">${formatDate(issue.resolvedAt)}</div>
            </div>` : ''}
            ${issue.assignedTo ? `
            <div>
              <div class="form-label">Assigned To</div>
              <div class="fw-600">👤 ${issue.assignedTo}</div>
            </div>` : ''}
          </div>
          <div>
            <div class="form-label">Description</div>
            <div style="background:var(--color-surface-2);padding:14px;border-radius:var(--radius-sm);line-height:1.7;font-size:14px">
              ${issue.description}
            </div>
          </div>
          ${issue.resolutionNotes ? `
          <div class="mt-4">
            <div class="form-label">Resolution Notes</div>
            <div style="background:var(--color-low-light);padding:14px;border-radius:var(--radius-sm);border-left:4px solid var(--color-resolved);line-height:1.7;font-size:14px">
              ${issue.resolutionNotes}
            </div>
          </div>` : ''}
        </div>

        <!-- Admin Actions -->
        ${isAdmin ? renderAdminActions(issue) : ''}
      </div>

      <!-- Right column -->
      <div style="display:flex;flex-direction:column;gap:20px">

        <!-- Priority Breakdown -->
        <div class="card">
          <div class="card-header">
            <div class="card-title">🧠 Priority Engine</div>
          </div>
          <div class="priority-score-header">
            <div>
              <div class="fw-600">Score: ${issue.priorityScore || 0}/100</div>
              <div class="text-small text-muted mt-1">${issue.priority} priority</div>
            </div>
            <div>${priorityBadge(issue.priority)}</div>
          </div>
          <div class="score-bars mt-3" id="scoreBars"></div>
          <div class="mt-3 text-small text-muted" style="padding:8px;background:var(--color-surface-2);border-radius:var(--radius-sm)">
            ≥75: CRITICAL &nbsp;·&nbsp; 50-74: HIGH &nbsp;·&nbsp; 25-49: MEDIUM &nbsp;·&nbsp; &lt;25: LOW
          </div>
        </div>

        <!-- Status Timeline -->
        <div class="card">
          <div class="card-header">
            <div class="card-title">Issue Lifecycle</div>
          </div>
          ${renderTimeline(issue.status)}
        </div>
      </div>
    </div>`;

  renderScoreBarsDetail(issue);
}

function renderScoreBarsDetail(issue) {
  const el = document.getElementById('scoreBars');
  if (!el) return;

  el.innerHTML = [
    { label:'Safety Impact', val:issue.safetyScore || 0, max:40, type:'safety' },
    { label:'Affected Users', val:issue.userScore || 0, max:25, type:'users' },
    { label:'Category Risk', val:issue.categoryScore || 0, max:20, type:'category' },
    { label:'Issue Age', val:issue.ageScore || 0, max:15, type:'age' }
  ].map(b => `
    <div class="score-bar">
      <div class="score-bar-label">${b.label}</div>
      <div class="score-bar-track">
        <div class="score-bar-fill ${b.type}" style="width:${Math.round(b.val/b.max*100)}%"></div>
      </div>
      <div class="score-bar-value">${b.val}</div>
    </div>`).join('');
}

const STATUS_ORDER = ['OPEN','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED'];

function renderTimeline(currentStatus) {
  const currentIdx = STATUS_ORDER.indexOf(currentStatus);
  return `<div class="timeline">
    ${STATUS_ORDER.map((status, idx) => {
      const isCompleted = idx < currentIdx;
      const isCurrent   = idx === currentIdx;
      const dotClass    = isCompleted ? 'completed' : isCurrent ? 'current' : 'pending';
      const lineClass   = isCompleted ? 'completed' : 'pending';
      const icons = { OPEN:'📋',ASSIGNED:'👤',IN_PROGRESS:'⚙️',RESOLVED:'✅',CLOSED:'🔒' };
      return `
        <div class="timeline-item">
          <div class="timeline-connector">
            <div class="timeline-dot ${dotClass}">${icons[status]}</div>
            ${idx < STATUS_ORDER.length-1 ? `<div class="timeline-line ${lineClass}"></div>` : ''}
          </div>
          <div class="timeline-content">
            <div class="timeline-label">${status.replace('_',' ')}</div>
            <div class="timeline-meta">${isCompleted ? 'Completed' : isCurrent ? 'Current stage' : 'Pending'}</div>
          </div>
        </div>`;
    }).join('')}
  </div>`;
}

function renderAdminActions(issue) {
  const actions = [];

  if (issue.status === 'OPEN' || issue.status === 'ASSIGNED') {
    actions.push(`<button class="btn btn-secondary" onclick="openAssignModal()">👤 Assign to Staff</button>`);
  }
  if (NEXT_STATUS[issue.status] && issue.status !== 'IN_PROGRESS') {
    actions.push(`<button class="btn btn-primary" onclick="quickAdvance('${issue.status}')">→ Move to ${NEXT_STATUS[issue.status].replace('_',' ')}</button>`);
  }
  if (issue.status === 'IN_PROGRESS') {
    actions.push(`<button class="btn btn-success" onclick="openResolveModal()">✅ Mark Resolved</button>`);
  }
  if (issue.status === 'RESOLVED') {
    actions.push(`<button class="btn btn-secondary" onclick="doClose()">🔒 Close Issue</button>`);
  }

  if (actions.length === 0) return '';

  return `
    <div class="card">
      <div class="card-title mb-3">Admin Actions</div>
      <div class="d-flex gap-3" style="flex-wrap:wrap">
        ${actions.join('')}
      </div>
      <div id="actionAlert" style="display:none;margin-top:12px"></div>
    </div>`;
}

// --- Admin action handlers ---

function openAssignModal() {
  document.getElementById('assignStaffName').value = '';
  showModal('assignModal');
}

function openResolveModal() {
  document.getElementById('resolutionNotesInput').value = '';
  showModal('resolveModal');
}

async function quickAdvance(currentStatus) {
  const next = NEXT_STATUS[currentStatus];
  if (!next || !confirmAction(`Move issue to ${next.replace('_',' ')}?`)) return;
  try {
    await IssueAPI.updateStatus(currentIssue.id, { status: next });
    await loadIssueDetails(currentIssue.id);
  } catch (err) {
    alert('Failed: ' + err.message);
  }
}

async function doClose() {
  if (!confirmAction('Close this issue? This cannot be undone.')) return;
  try {
    await IssueAPI.updateStatus(currentIssue.id, { status: 'CLOSED' });
    await loadIssueDetails(currentIssue.id);
  } catch (err) {
    alert('Failed: ' + err.message);
  }
}

async function doAssign() {
  const name = document.getElementById('assignStaffName').value.trim();
  if (!name) { alert('Please enter a staff name.'); return; }
  try {
    await IssueAPI.assign(currentIssue.id, { assignedTo: name });
    closeModal('assignModal');
    await loadIssueDetails(currentIssue.id);
  } catch (err) {
    alert('Assignment failed: ' + err.message);
  }
}

async function doResolve() {
  const notes = document.getElementById('resolutionNotesInput').value.trim();
  if (!notes || notes.length < 5) { alert('Please provide resolution notes (min 5 chars).'); return; }
  try {
    await IssueAPI.resolve(currentIssue.id, { resolutionNotes: notes });
    closeModal('resolveModal');
    await loadIssueDetails(currentIssue.id);
  } catch (err) {
    alert('Resolution failed: ' + err.message);
  }
}

function showModal(id) { document.getElementById(id).classList.add('visible'); }
function closeModal(id) { document.getElementById(id).classList.remove('visible'); }
