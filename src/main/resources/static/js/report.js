/**
 * report.js — Report Issue page logic.
 * Handles form validation, submission, and displaying the
 * engine-calculated priority result after creation.
 */

document.addEventListener('DOMContentLoaded', () => {
  requireRole('student');
  renderSidebar('student', 'report');
  updateClock();
  setInterval(updateClock, 60000);

  // Pre-fill reporter name from localStorage
  const storedName = localStorage.getItem('campus_reporter_name');
  if (storedName) {
    document.getElementById('reportedBy').value = storedName;
  }

  document.getElementById('reportForm').addEventListener('submit', handleSubmit);
  document.getElementById('reportAnotherBtn').addEventListener('click', resetForm);
});

async function handleSubmit(e) {
  e.preventDefault();

  if (!validateForm()) return;

  const submitBtn = document.getElementById('submitBtn');
  submitBtn.disabled = true;
  submitBtn.innerHTML = '<span class="spinner" style="width:16px;height:16px;border-width:2px;margin:0"></span> Submitting…';

  hideAlert('formAlert');

  const data = {
    reportedBy:    document.getElementById('reportedBy').value.trim(),
    category:      document.getElementById('category').value,
    location:      document.getElementById('location').value.trim(),
    description:   document.getElementById('description').value.trim(),
    affectedUsers: parseInt(document.getElementById('affectedUsers').value),
    safetyImpact:  document.getElementById('safetyImpact').checked
  };

  try {
    const issue = await IssueAPI.create(data);
    localStorage.setItem('campus_reporter_name', data.reportedBy);
    showSubmissionResult(issue);
  } catch (err) {
    let msg = err.message;
    if (err.fieldErrors) {
      msg = Object.values(err.fieldErrors).join('<br>');
    }
    showAlert('formAlert', msg, 'error');
    submitBtn.disabled = false;
    submitBtn.innerHTML = '🚀 Submit Issue Report';
  }
}

function validateForm() {
  let valid = true;
  const fields = ['reportedBy', 'category', 'location', 'description', 'affectedUsers'];

  fields.forEach(id => {
    const el = document.getElementById(id);
    const errorEl = document.getElementById(`${id}Error`);
    if (!el.value.trim()) {
      el.classList.add('is-invalid');
      if (errorEl) { errorEl.textContent = 'This field is required.'; errorEl.classList.add('visible'); }
      valid = false;
    } else {
      el.classList.remove('is-invalid');
      if (errorEl) errorEl.classList.remove('visible');
    }
  });

  const desc = document.getElementById('description');
  if (desc.value.trim().length < 10) {
    desc.classList.add('is-invalid');
    const err = document.getElementById('descriptionError');
    if (err) { err.textContent = 'Description must be at least 10 characters.'; err.classList.add('visible'); }
    valid = false;
  }

  const users = parseInt(document.getElementById('affectedUsers').value);
  if (isNaN(users) || users < 1) {
    document.getElementById('affectedUsers').classList.add('is-invalid');
    const err = document.getElementById('affectedUsersError');
    if (err) { err.textContent = 'Please enter a valid number of affected users.'; err.classList.add('visible'); }
    valid = false;
  }

  return valid;
}

function showSubmissionResult(issue) {
  document.getElementById('reportFormSection').style.display = 'none';

  const resultEl = document.getElementById('submissionResult');
  resultEl.style.display = 'block';

  document.getElementById('resultCode').textContent = issue.issueCode;
  document.getElementById('resultPriority').innerHTML = priorityBadge(issue.priority);
  document.getElementById('resultScore').textContent = issue.priorityScore;
  document.getElementById('resultStatus').innerHTML = statusBadge(issue.status);
  document.getElementById('resultCategory').textContent = `${CATEGORY_ICONS[issue.category]} ${issue.category}`;
  document.getElementById('resultLocation').textContent = issue.location;

  renderPriorityScoreBreakdown(issue);

  resultEl.scrollIntoView({ behavior: 'smooth' });
}

function renderPriorityScoreBreakdown(issue) {
  const score = issue.priorityScore || 0;
  const display = document.getElementById('scoreDisplay');
  if (!display) return;

  display.innerHTML = `
    <div class="priority-score-display">
      <div class="priority-score-header">
        <div>
          <div class="priority-score-title">🧠 Priority Engine Breakdown</div>
          <div class="text-small text-muted mt-1">How the score was calculated</div>
        </div>
        <div class="priority-score-total">${score}/100</div>
      </div>
      <div class="score-bars">
        ${scoreBar('Safety Impact', issue.safetyScore || 0, 40, 'safety')}
        ${scoreBar('Affected Users', issue.userScore || 0, 25, 'users')}
        ${scoreBar('Category Risk', issue.categoryScore || 0, 20, 'category')}
        ${scoreBar('Issue Age', issue.ageScore || 0, 15, 'age')}
      </div>
      <div class="mt-3 text-small text-muted">
        Score ≥ 75 → CRITICAL &nbsp;|&nbsp; 50–74 → HIGH &nbsp;|&nbsp; 25–49 → MEDIUM &nbsp;|&nbsp; &lt;25 → LOW
      </div>
    </div>`;
}

function scoreBar(label, value, max, type) {
  const pct = Math.round((value / max) * 100);
  return `
    <div class="score-bar">
      <div class="score-bar-label">${label}</div>
      <div class="score-bar-track">
        <div class="score-bar-fill ${type}" style="width:${pct}%"></div>
      </div>
      <div class="score-bar-value">${value}</div>
    </div>`;
}

function resetForm() {
  document.getElementById('submissionResult').style.display = 'none';
  document.getElementById('reportFormSection').style.display = 'block';
  document.getElementById('reportForm').reset();
  document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
  document.querySelectorAll('.form-error').forEach(el => el.classList.remove('visible'));
  window.scrollTo({ top: 0, behavior: 'smooth' });
}
