/**
 * my-issues.js — My Issues page for students.
 * Loads all issues reported by the current student with search/filter.
 */

let allIssues = [];

document.addEventListener('DOMContentLoaded', () => {
  requireRole('student');
  renderSidebar('student', 'my-issues');
  updateClock();
  setInterval(updateClock, 60000);

  document.getElementById('searchBtn').addEventListener('click', applyFilters);
  document.getElementById('clearBtn').addEventListener('click', clearFilters);
  document.getElementById('searchInput').addEventListener('keyup', e => {
    if (e.key === 'Enter') applyFilters();
  });

  loadIssues();
});

async function loadIssues() {
  showLoading('issuesContainer', 'Loading your issues…');
  try {
    const reporterName = localStorage.getItem('campus_reporter_name') || '';
    const params = reporterName ? { reportedBy: reporterName } : {};
    allIssues = await IssueAPI.search(params);
    renderIssues(allIssues);
  } catch (err) {
    showError('issuesContainer', err.message);
  }
}

function applyFilters() {
  const keyword  = document.getElementById('searchInput').value.toLowerCase();
  const status   = document.getElementById('statusFilter').value;
  const priority = document.getElementById('priorityFilter').value;
  const category = document.getElementById('categoryFilter').value;

  const filtered = allIssues.filter(issue => {
    const matchesKeyword = !keyword ||
      issue.description.toLowerCase().includes(keyword) ||
      issue.location.toLowerCase().includes(keyword) ||
      issue.issueCode.toLowerCase().includes(keyword);
    const matchesStatus   = !status   || issue.status === status;
    const matchesPriority = !priority || issue.priority === priority;
    const matchesCategory = !category || issue.category === category;
    return matchesKeyword && matchesStatus && matchesPriority && matchesCategory;
  });

  renderIssues(filtered);
}

function clearFilters() {
  document.getElementById('searchInput').value = '';
  document.getElementById('statusFilter').value = '';
  document.getElementById('priorityFilter').value = '';
  document.getElementById('categoryFilter').value = '';
  renderIssues(allIssues);
}

function renderIssues(issues) {
  const container = document.getElementById('issuesContainer');

  if (issues.length === 0) {
    showEmpty('issuesContainer', 'No issues found',
      'Try adjusting your filters or report a new campus issue.');
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
            <th>Description</th>
            <th>SLA</th>
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
              <td style="max-width:140px" class="truncate">${issue.location}</td>
              <td style="max-width:240px" class="truncate">${issue.description}</td>
              <td><span class="badge" style="background:var(--color-surface-2);color:var(--text-main);border:1px solid var(--border-color)">⏱ ${issue.slaIndicator || 'Unknown'}</span></td>
              <td>${priorityBadge(issue.priority)}</td>
              <td>${statusBadge(issue.status)}</td>
              <td class="text-muted">${relativeTime(issue.reportedAt)}</td>
            </tr>`).join('')}
        </tbody>
      </table>
    </div>
    <div class="mt-3 text-small text-muted">Showing ${issues.length} issue${issues.length !== 1 ? 's' : ''}</div>`;
}
