/**
 * HOSPITAL PATIENT RECORD SYSTEM - JAVASCRIPT FRONTEND CONTROLLER
 * Implements Functional Requirements FR1 to FR9 with pure vanilla JavaScript.
 * Clean, well-commented code designed for college viva presentation.
 */

const API_BASE = '/api';
let currentUser = null;
let cachedPatients = [];
let cachedDoctors = [];

// =========================================================================
// INITIALIZATION & SESSION RESTORE
// =========================================================================
document.addEventListener('DOMContentLoaded', () => {
  const savedUser = localStorage.getItem('hospital_user');
  if (savedUser) {
    try {
      currentUser = JSON.parse(savedUser);
      initSessionUI();
    } catch (e) {
      localStorage.removeItem('hospital_user');
    }
  }

  // Set default appointment date to tomorrow
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const dateStr = tomorrow.toISOString().split('T')[0];
  const apptDateInput = document.getElementById('apptDate');
  if (apptDateInput) apptDateInput.value = dateStr;

  const todayStr = new Date().toISOString().split('T')[0];
  const medVisitInput = document.getElementById('medVisitDate');
  if (medVisitInput) medVisitInput.value = todayStr;
});

// =========================================================================
// FR1: LOGIN FUNCTIONALITY
// =========================================================================
async function handleLogin(e) {
  e.preventDefault();
  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value.trim();
 //const role = document.getElementById('loginRole').value;

  try {
    const res = await fetch(`${API_BASE}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    const data = await res.json();
    if (data.success && data.user) {
      currentUser = data.user;
      localStorage.setItem('hospital_user', JSON.stringify(currentUser));
      showToast(`Welcome, ${currentUser.name}! (${currentUser.role})`, 'success');
      initSessionUI();
    } else {
      showToast(data.message || 'Invalid Username, Password,', 'error');
    }
  } catch (err) {
    showToast('Failed to connect to backend server: ' + err.message, 'error');
  }
}

function fillLogin(username, password, role) {
  document.getElementById('loginUsername').value = username;
  document.getElementById('loginPassword').value = password;
  //document.getElementById('loginRole').value = role;
}

// =========================================================================
// FR9: LOGOUT FUNCTIONALITY
// =========================================================================
function logout() {
  currentUser = null;
  localStorage.removeItem('hospital_user');
  document.getElementById('navBar').style.display = 'none';
  document.getElementById('userHeaderSection').style.display = 'none';
  document.getElementById('loginSection').style.display = 'block';

  // Hide all tab panes
  document.querySelectorAll('.tab-pane').forEach(el => el.classList.remove('active'));
  showToast('Logged out successfully.', 'info');
}

// =========================================================================
// UI ROLE-BASED ACCESS & NAVIGATION
// =========================================================================
function initSessionUI() {
  if (!currentUser) return;

  // Header badges
  document.getElementById('headerUserName').textContent = currentUser.name;
  document.getElementById('headerUserRole').textContent = currentUser.role;
  document.getElementById('headerUserRole').className = `role-tag role-${currentUser.role.toLowerCase()}`;
  document.getElementById('userHeaderSection').style.display = 'flex';
  document.getElementById('loginSection').style.display = 'none';
  document.getElementById('navBar').style.display = 'flex';

  // Session box on dashboard
  const sessionEl = document.getElementById('sessionDetails');
  if (sessionEl) {
    sessionEl.textContent = `${currentUser.name} | Role: ${currentUser.role} | Username: ${currentUser.username}`;
  }

  // Filter Nav buttons according to user role
  const navBtns = document.querySelectorAll('.nav-tab-btn');
  navBtns.forEach(btn => {
    const rolesAttr = btn.getAttribute('data-roles');
    if (!rolesAttr) {
      btn.style.display = 'inline-block'; // Dashboard available to all
    } else {
      const allowedRoles = rolesAttr.split(',').map(r => r.trim());
      if (allowedRoles.includes(currentUser.role)) {
        btn.style.display = 'inline-block';
      } else {
        btn.style.display = 'none';
      }
    }
  });

  // Switch to Dashboard
  // Open the first tab this role is allowed to see (Dashboard is Admin only)
  const landingTab = {
    Admin: 'dashboardTab',
    Doctor: 'appointmentsTab',
    Patient: 'appointmentsTab',
    Nurse: 'patientsTab',
    Receptionist: 'patientsTab'
  }[currentUser.role] || 'appointmentsTab';
  switchTab(landingTab);
    // Only Patients can book appointments
  const apptFormCard = document.getElementById('appointmentFormCard');
  if (apptFormCard) {
    apptFormCard.style.display = (currentUser.role === 'Patient') ? 'block' : 'none';
  }
  // Patients don't get an Actions column
  const actionsHeader = document.getElementById('apptActionsHeader');
  if (actionsHeader) {
    actionsHeader.style.display = (currentUser.role === 'Patient') ? 'none' : '';
  }
  const recordFormCard = document.getElementById('recordFormCard');
  if (recordFormCard) {
    recordFormCard.style.display = (currentUser.role === 'Patient') ? 'none' : 'block';
  }
  // Load dropdown lists in background
  loadPatientsAndDoctorsForDropdowns();
}

function switchTab(tabId) {
  // Update nav buttons active state
  document.querySelectorAll('.nav-tab-btn').forEach(btn => {
    if (btn.getAttribute('data-tab') === tabId) {
      btn.classList.add('active');
    } else {
      btn.classList.remove('active');
    }
  });

  // Show target tab pane
  document.querySelectorAll('.tab-pane').forEach(pane => {
    if (pane.id === tabId) {
      pane.classList.add('active');
    } else {
      pane.classList.remove('active');
    }
  });

  // Trigger data loader for the active tab
  if (tabId === 'dashboardTab') loadDashboardStats();
  if (tabId === 'patientsTab') loadPatients();
  if (tabId === 'doctorsTab') loadDoctors();
  if (tabId === 'appointmentsTab') {
    loadPatientsAndDoctorsForDropdowns();
    loadAppointments();
  }
  if (tabId === 'medicalRecordsTab') {
    loadPatientsAndDoctorsForDropdowns();
    loadMedicalRecords();
  }
  if (tabId === 'usersTab') loadUsers();
}

// =========================================================================
// DASHBOARD MODULE
// =========================================================================
async function loadDashboardStats() {
  try {
    const res = await fetch(`${API_BASE}/dashboard/stats`);
    const data = await res.json();
    if (data.success && data.stats) {
      document.getElementById('statPatients').textContent = data.stats.totalPatients || 0;
      document.getElementById('statDoctors').textContent = data.stats.totalDoctors || 0;
      document.getElementById('statAppointments').textContent = data.stats.scheduledAppointments || 0;
      document.getElementById('statRecords').textContent = data.stats.totalMedicalRecords || 0;
    }
  } catch (err) {
    console.error('Failed to load dashboard stats:', err);
  }
}

// =========================================================================
// FR3: PATIENT MANAGEMENT
// =========================================================================
async function loadPatients() {
  const tbody = document.getElementById('patientsTableBody');
  tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Loading patients...</td></tr>';

  try {
    const res = await fetch(`${API_BASE}/patients`);
    const data = await res.json();
    if (data.success) {
      cachedPatients = data.patients || [];
      renderPatientsTable(cachedPatients);
    }
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="7" style="color:red; text-align:center;">Error: ${err.message}</td></tr>`;
  }
}

function renderPatientsTable(list) {
  const tbody = document.getElementById('patientsTableBody');
  if (!list || list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No patient records found.</td></tr>';
    return;
  }

  const isAdminOrRecep = currentUser && (currentUser.role === 'Admin' || currentUser.role === 'Receptionist');

  tbody.innerHTML = list.map(p => `
    <tr>
      <td><strong>#${p.patientId}</strong></td>
      <td>${escapeHtml(p.name)}</td>
      <td>${escapeHtml(p.dob)}</td>
      <td>${escapeHtml(p.gender)}</td>
      <td>${escapeHtml(p.phone)}</td>
      <td>${escapeHtml(p.address)}</td>
      <td>
        ${isAdminOrRecep ? `
          <button class="btn btn-secondary btn-sm" onclick="editPatient(${p.patientId})">✏️ Edit</button>
          <button class="btn btn-danger btn-sm" onclick="deletePatient(${p.patientId})">🗑️ Delete</button>
        ` : `<span style="color:#64748b; font-size:0.8rem;">Read Only</span>`}
      </td>
    </tr>
  `).join('');
}

async function savePatient(e) {
  e.preventDefault();
  const id = document.getElementById('patientId').value;
  const name = document.getElementById('patientName').value.trim();
  const dob = document.getElementById('patientDob').value;
  const gender = document.getElementById('patientGender').value;
  const phone = document.getElementById('patientPhone').value.trim();
  const address = document.getElementById('patientAddress').value.trim();

  //const payload = { name, dob, gender, phone, address };
  //if (id) payload.patientId = id;
  const payload = { name, dob, gender, phone, address };
  if (id) {
    payload.patientId = id;
  } else {
    payload.username = document.getElementById('patientUsername').value.trim();
    payload.password = document.getElementById('patientPassword').value.trim();
  }

  const method = id ? 'PUT' : 'POST';

  try {
    const res = await fetch(`${API_BASE}/patients`, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message || 'Patient record saved successfully!', 'success');
      resetPatientForm();
      loadPatients();
      loadPatientsAndDoctorsForDropdowns();
    } else {
      showToast(data.message || 'Failed to save patient.', 'error');
    }
  } catch (err) {
    showToast('Network error: ' + err.message, 'error');
  }
}

function editPatient(id) {
  const p = cachedPatients.find(item => item.patientId === id);
  if (!p) return;

  document.getElementById('patientId').value = p.patientId;
  document.getElementById('patientName').value = p.name;
  document.getElementById('patientDob').value = p.dob;
  document.getElementById('patientGender').value = p.gender;
  document.getElementById('patientPhone').value = p.phone;
  document.getElementById('patientAddress').value = p.address;

  //document.getElementById('patientFormTitle').textContent = `Edit Patient (ID #${p.patientId})`;
  //document.getElementById('patientSubmitBtn').textContent = 'Update Patient';
  //window.scrollTo({ top: document.getElementById('patientFormCard').offsetTop - 20, behavior: 'smooth' });
  document.getElementById('patientFormTitle').textContent = `Edit Patient (ID #${p.patientId})`;
  document.getElementById('patientSubmitBtn').textContent = 'Update Patient';

  // Login fields are only for new patients
  document.getElementById('patientLoginFields').style.display = 'none';
  document.getElementById('patientUsername').required = false;
  document.getElementById('patientPassword').required = false;

  window.scrollTo({ top: document.getElementById('patientFormCard').offsetTop - 20, behavior: 'smooth' });
}


/*function resetPatientForm() {
  document.getElementById('patientForm').reset();
  document.getElementById('patientId').value = '';
  document.getElementById('patientFormTitle').textContent = 'Add New Patient (FR3)';
  document.getElementById('patientSubmitBtn').textContent = 'Save Patient Record';
}*/
function resetPatientForm() {
  document.getElementById('patientForm').reset();
  document.getElementById('patientId').value = '';
  document.getElementById('patientFormTitle').textContent = 'Add New Patient (FR3)';
  document.getElementById('patientSubmitBtn').textContent = 'Save Patient Record';

  // Show login fields again for new patients
  document.getElementById('patientLoginFields').style.display = '';
  document.getElementById('patientUsername').required = true;
  document.getElementById('patientPassword').required = true;
}

async function deletePatient(id) {
  if (!confirm(`Are you sure you want to delete Patient ID #${id}? This will also delete related appointments and medical records.`)) return;

  try {
    const res = await fetch(`${API_BASE}/patients?id=${id}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      showToast(data.message, 'success');
      loadPatients();
      loadPatientsAndDoctorsForDropdowns();
    } else {
      showToast(data.message, 'error');
    }
  } catch (err) {
    showToast('Delete error: ' + err.message, 'error');
  }
}

// =========================================================================
// FR4: DOCTOR MANAGEMENT
// =========================================================================
async function loadDoctors() {
  const tbody = document.getElementById('doctorsTableBody');
  tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">Loading doctors...</td></tr>';

  try {
    const res = await fetch(`${API_BASE}/doctors`);
    const data = await res.json();
    if (data.success) {
      cachedDoctors = data.doctors || [];
      renderDoctorsTable(cachedDoctors);
    }
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="5" style="color:red; text-align:center;">Error: ${err.message}</td></tr>`;
  }
}

function renderDoctorsTable(list) {
  const tbody = document.getElementById('doctorsTableBody');
  if (!list || list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No doctor profiles found.</td></tr>';
    return;
  }

  tbody.innerHTML = list.map(d => `
    <tr>
      <td><strong>#${d.doctorId}</strong></td>
      <td>${escapeHtml(d.name)}</td>
      <td><span class="badge badge-scheduled">${escapeHtml(d.specialization)}</span></td>
      <td>${escapeHtml(d.phone)}</td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="editDoctor(${d.doctorId})">✏️ Edit</button>
        <button class="btn btn-danger btn-sm" onclick="deleteDoctor(${d.doctorId})">🗑️ Delete</button>
      </td>
    </tr>
  `).join('');
}

async function saveDoctor(e) {
  e.preventDefault();
  const id = document.getElementById('doctorId').value;
  const name = document.getElementById('doctorName').value.trim();
  const specialization = document.getElementById('doctorSpec').value.trim();
  const phone = document.getElementById('doctorPhone').value.trim();

  const payload = { name, specialization, phone };
  if (id) payload.doctorId = id;

  const method = id ? 'PUT' : 'POST';

  try {
    const res = await fetch(`${API_BASE}/doctors`, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message || 'Doctor profile saved successfully!', 'success');
      resetDoctorForm();
      loadDoctors();
      loadPatientsAndDoctorsForDropdowns();
    } else {
      showToast(data.message || 'Failed to save doctor.', 'error');
    }
  } catch (err) {
    showToast('Network error: ' + err.message, 'error');
  }
}

function editDoctor(id) {
  const d = cachedDoctors.find(item => item.doctorId === id);
  if (!d) return;

  document.getElementById('doctorId').value = d.doctorId;
  document.getElementById('doctorName').value = d.name;
  document.getElementById('doctorSpec').value = d.specialization;
  document.getElementById('doctorPhone').value = d.phone;

  document.getElementById('doctorFormTitle').textContent = `Edit Doctor (ID #${d.doctorId})`;
  document.getElementById('doctorSubmitBtn').textContent = 'Update Doctor';
  window.scrollTo({ top: document.getElementById('doctorFormCard').offsetTop - 20, behavior: 'smooth' });
}

function resetDoctorForm() {
  document.getElementById('doctorForm').reset();
  document.getElementById('doctorId').value = '';
  document.getElementById('doctorFormTitle').textContent = 'Add New Doctor (FR4)';
  document.getElementById('doctorSubmitBtn').textContent = 'Save Doctor';
}

async function deleteDoctor(id) {
  if (!confirm(`Are you sure you want to delete Doctor ID #${id}?`)) return;

  try {
    const res = await fetch(`${API_BASE}/doctors?id=${id}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      showToast(data.message, 'success');
      loadDoctors();
      loadPatientsAndDoctorsForDropdowns();
    } else {
      showToast(data.message, 'error');
    }
  } catch (err) {
    showToast('Delete error: ' + err.message, 'error');
  }
}

// =========================================================================
// HELPER: POPULATE DROPDOWNS FOR APPOINTMENTS & MEDICAL RECORDS
// =========================================================================
async function loadPatientsAndDoctorsForDropdowns() {
  try {
    const [pRes, dRes] = await Promise.all([
      fetch(`${API_BASE}/patients`),
      fetch(`${API_BASE}/doctors`)
    ]);
    const pData = await pRes.json();
    const dData = await dRes.json();

    if (pData.success) cachedPatients = pData.patients || [];
    if (dData.success) cachedDoctors = dData.doctors || [];

    // Populate Appointment Patient & Doctor select
    const apptPat = document.getElementById('apptPatientSelect');
    const apptDoc = document.getElementById('apptDoctorSelect');
    const medPat = document.getElementById('medPatientSelect');
    const medDoc = document.getElementById('medDoctorSelect');

    const patientOptions = `<option value="">-- Choose Patient --</option>` +
      cachedPatients.map(p => `<option value="${p.patientId}">#${p.patientId} - ${escapeHtml(p.name)} (${escapeHtml(p.phone)})</option>`).join('');

    const doctorOptions = `<option value="">-- Choose Doctor --</option>` +
      cachedDoctors.map(d => `<option value="${d.doctorId}">#${d.doctorId} - ${escapeHtml(d.name)} (${escapeHtml(d.specialization)})</option>`).join('');

    if (apptPat) apptPat.innerHTML = patientOptions;
    if (medPat) medPat.innerHTML = patientOptions;
    if (apptDoc) apptDoc.innerHTML = doctorOptions;
    if (medDoc) medDoc.innerHTML = doctorOptions;

    // If current user is Patient, preselect themselves
    if (currentUser && currentUser.role === 'Patient') {
      const myPat = cachedPatients.find(p => p.userId === currentUser.userId);
      if (myPat) {
        if (apptPat) { apptPat.value = myPat.patientId; apptPat.disabled = true; }
        if (medPat) medPat.value = myPat.patientId;
      }
    }

    // If current user is Doctor, preselect themselves
    if (currentUser && currentUser.role === 'Doctor') {
      const myDoc = cachedDoctors.find(d => d.userId === currentUser.userId);
      if (myDoc) {
        if (apptDoc) apptDoc.value = myDoc.doctorId;
        if (medDoc) medDoc.value = myDoc.doctorId;
      }
    }
  } catch (err) {
    console.error('Error populating dropdowns:', err);
  }
}

// =========================================================================
// FR5: APPOINTMENT MANAGEMENT
// =========================================================================
async function loadAppointments() {
  const tbody = document.getElementById('appointmentsTableBody');
  tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">Loading appointments...</td></tr>';

  let url = `${API_BASE}/appointments`;

  // If Doctor logged in, filter by their doctorId
  if (currentUser && currentUser.role === 'Doctor') {
    const myDoc = cachedDoctors.find(d => d.userId === currentUser.userId);
    if (myDoc) url += `?doctorId=${myDoc.doctorId}`;
  }

  // If Patient logged in, filter by their patientId
    // If Patient logged in, filter by their patientId
  if (currentUser && currentUser.role === 'Patient') {
    if (cachedPatients.length === 0) await loadPatientsAndDoctorsForDropdowns();
    const myPat = cachedPatients.find(p => p.userId === currentUser.userId);
    if (!myPat) {
      tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">No appointments found.</td></tr>';
      return;
    }
    url += `?patientId=${myPat.patientId}`;
  }

  try {
    const res = await fetch(url);
    const data = await res.json();
    if (data.success) {
      renderAppointmentsTable(data.appointments || []);
    }
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="8" style="color:red; text-align:center;">Error: ${err.message}</td></tr>`;
  }
}

function renderAppointmentsTable(list) {
  const tbody = document.getElementById('appointmentsTableBody');
  if (!list || list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">No appointments found.</td></tr>';
    return;
  }

  tbody.innerHTML = list.map(a => {
    let badgeClass = 'badge-scheduled';
    if (a.status === 'Completed') badgeClass = 'badge-completed';
    if (a.status === 'Cancelled') badgeClass = 'badge-cancelled';
    const isPatient = currentUser && currentUser.role === 'Patient';

    return `
      <tr>
        <td><strong>#${a.appointmentId}</strong></td>
        <td>${escapeHtml(a.patientName)}</td>
        <td>${escapeHtml(a.doctorName)}</td>
        <td>${escapeHtml(a.doctorSpecialization)}</td>
        <td>${escapeHtml(a.appDate)}</td>
        <td>${escapeHtml(a.appTime)}</td>
        <td><span class="badge ${badgeClass}">${escapeHtml(a.status)}</span></td>
                ${isPatient ? '' : `
        <td>
          ${a.status === 'Scheduled' ? `
            <button class="btn btn-success btn-sm" onclick="updateAppointmentStatus(${a.appointmentId}, 'Completed')">✓ Complete</button>
            <button class="btn btn-danger btn-sm" onclick="updateAppointmentStatus(${a.appointmentId}, 'Cancelled')">✕ Cancel</button>
          ` : `
            <span style="color:#64748b; font-size:0.8rem;">Finished</span>
          `}
          ${currentUser && currentUser.role === 'Admin' ? `
            <button class="btn btn-secondary btn-sm" onclick="deleteAppointment(${a.appointmentId})">🗑️</button>
          ` : ''}
        </td>
        `}
      </tr>
      </tr>
    `;
  }).join('');
}

async function bookAppointment(e) {
  e.preventDefault();
  const patientId = document.getElementById('apptPatientSelect').value;
  const doctorId = document.getElementById('apptDoctorSelect').value;
  const appDate = document.getElementById('apptDate').value;
  const appTime = document.getElementById('apptTime').value;
  //const status = document.getElementById('apptStatus').value;
  const status = 'Scheduled';

  if (!patientId || !doctorId || !appDate || !appTime) {
    showToast('Please fill all required appointment fields.', 'error');
    return;
  }

  const payload = { patientId, doctorId, appDate, appTime, status };

  try {
    const res = await fetch(`${API_BASE}/appointments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message || 'Appointment booked successfully!', 'success');
      loadAppointments();
    } else {
      // Shows double-booking or other error message
      showToast(data.message || 'Booking conflict or error.', 'error');
    }
  } catch (err) {
    showToast('Booking failed: ' + err.message, 'error');
  }
}

async function updateAppointmentStatus(id, newStatus) {
  try {
    const res = await fetch(`${API_BASE}/appointments`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ appointmentId: id, status: newStatus })
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message, 'success');
      loadAppointments();
    } else {
      showToast(data.message, 'error');
    }
  } catch (err) {
    showToast('Status update error: ' + err.message, 'error');
  }
}

async function deleteAppointment(id) {
  if (!confirm(`Delete appointment record #${id}?`)) return;
  try {
    const res = await fetch(`${API_BASE}/appointments?id=${id}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      showToast(data.message, 'success');
      loadAppointments();
    }
  } catch (err) {
    showToast('Delete error: ' + err.message, 'error');
  }
}

// =========================================================================
// FR6: MEDICAL RECORD MANAGEMENT
// =========================================================================
async function loadMedicalRecords() {
  const tbody = document.getElementById('medicalRecordsTableBody');
  tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Loading medical records...</td></tr>';

  let url = `${API_BASE}/medical-records`;

  // If Patient logged in, show only their personal history
    // If Patient logged in, show only their personal history
  if (currentUser && currentUser.role === 'Patient') {
    if (cachedPatients.length === 0) await loadPatientsAndDoctorsForDropdowns();
    const myPat = cachedPatients.find(p => p.userId === currentUser.userId);
    if (!myPat) {
      tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No medical check-up records found.</td></tr>';
      return;
    }
    url += `?patientId=${myPat.patientId}`;
  }

  try {
    const res = await fetch(url);
    const data = await res.json();
    if (data.success) {
      renderMedicalRecordsTable(data.records || []);
    }
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="7" style="color:red; text-align:center;">Error: ${err.message}</td></tr>`;
  }
}

function renderMedicalRecordsTable(list) {
  const tbody = document.getElementById('medicalRecordsTableBody');
  if (!list || list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No medical check-up records found.</td></tr>';
    return;
  }

  tbody.innerHTML = list.map(m => `
    <tr>
      <td><strong>#${m.recordId}</strong></td>
      <td><strong>${escapeHtml(m.patientName)}</strong></td>
      <td>${escapeHtml(m.doctorName)}</td>
      <td>${escapeHtml(m.visitDate)}</td>
      <td><span style="color:#0369a1; font-weight:600;">${escapeHtml(m.diagnosis)}</span></td>
      <td>${escapeHtml(m.treatment)}</td>
      <td><em style="color:#475569;">${escapeHtml(m.notes || '-')}</em></td>
    </tr>
  `).join('');
}

async function saveMedicalRecord(e) {
  e.preventDefault();
  const patientId = document.getElementById('medPatientSelect').value;
  const doctorId = document.getElementById('medDoctorSelect').value;
  const visitDate = document.getElementById('medVisitDate').value;
  const diagnosis = document.getElementById('medDiagnosis').value.trim();
  const treatment = document.getElementById('medTreatment').value.trim();
  const notes = document.getElementById('medNotes').value.trim();

  if (!patientId || !doctorId || !visitDate || !diagnosis || !treatment) {
    showToast('Please fill all required check-up fields.', 'error');
    return;
  }

  const payload = { patientId, doctorId, visitDate, diagnosis, treatment, notes };

  try {
    const res = await fetch(`${API_BASE}/medical-records`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message || 'Medical record saved successfully!', 'success');
      document.getElementById('medicalRecordForm').reset();
      const todayStr = new Date().toISOString().split('T')[0];
      document.getElementById('medVisitDate').value = todayStr;
      loadMedicalRecords();
    } else {
      showToast(data.message || 'Failed to save medical record.', 'error');
    }
  } catch (err) {
    showToast('Network error: ' + err.message, 'error');
  }
}

// =========================================================================
// FR7: SEARCH PATIENT RECORDS
// =========================================================================
async function executeSearch() {
  const query = document.getElementById('searchInput').value.trim();
  const tbody = document.getElementById('searchResultsTableBody');
  tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">Searching...</td></tr>';

  try {
    const res = await fetch(`${API_BASE}/search?q=${encodeURIComponent(query)}`);
    const data = await res.json();
    if (data.success) {
      const results = data.results || [];
      if (results.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;">No matching patients found.</td></tr>';
      } else {
        tbody.innerHTML = results.map(p => `
          <tr>
            <td><strong>#${p.patientId}</strong></td>
            <td>${escapeHtml(p.name)}</td>
            <td>${escapeHtml(p.dob)}</td>
            <td>${escapeHtml(p.gender)}</td>
            <td>${escapeHtml(p.phone)}</td>
            <td>${escapeHtml(p.address)}</td>
            <td>
              <button class="btn btn-primary btn-sm" onclick="viewPatientFullDetails(${p.patientId})">👁️ View History</button>
            </td>
          </tr>
        `).join('');
      }
    }
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="7" style="color:red; text-align:center;">Error: ${err.message}</td></tr>`;
  }
}

function clearSearch() {
  document.getElementById('searchInput').value = '';
  document.getElementById('searchResultsTableBody').innerHTML = '<tr><td colspan="7" style="text-align:center;">Enter search term above to find patient records.</td></tr>';
  document.getElementById('patientDetailsCard').style.display = 'none';
}

async function viewPatientFullDetails(patientId) {
  const detailsCard = document.getElementById('patientDetailsCard');
  const detailsContent = document.getElementById('patientDetailContent');
  detailsCard.style.display = 'block';
  detailsContent.innerHTML = 'Loading patient history...';

  try {
    const [patRes, apptRes, medRes] = await Promise.all([
      fetch(`${API_BASE}/patients?id=${patientId}`),
      fetch(`${API_BASE}/appointments?patientId=${patientId}`),
      fetch(`${API_BASE}/medical-records?patientId=${patientId}`)
    ]);

    const patData = await patRes.json();
    const apptData = await apptRes.json();
    const medData = await medRes.json();

    const p = patData.patient;
    const appts = apptData.appointments || [];
    const meds = medData.records || [];

    document.getElementById('patientDetailHeader').textContent = `Full Record: ${p.name} (Patient ID #${p.patientId})`;

    detailsContent.innerHTML = `
      <div style="background:#f8fafc; padding:1rem; border-radius:6px; margin-bottom:1rem; border:1px solid #e2e8f0;">
        <p><strong>Full Name:</strong> ${escapeHtml(p.name)} &nbsp;|&nbsp; <strong>DOB:</strong> ${escapeHtml(p.dob)} &nbsp;|&nbsp; <strong>Gender:</strong> ${escapeHtml(p.gender)}</p>
        <p><strong>Phone:</strong> ${escapeHtml(p.phone)} &nbsp;|&nbsp; <strong>Address:</strong> ${escapeHtml(p.address)}</p>
      </div>

      <h3 style="font-size:1.05rem; margin-bottom:0.5rem; color:#0f172a;">Medical Consultation History (${meds.length})</h3>
      ${meds.length === 0 ? '<p style="color:#64748b; font-size:0.9rem;">No past medical records found.</p>' : `
        <table class="data-table" style="margin-bottom:1.5rem;">
          <thead><tr><th>Visit Date</th><th>Doctor</th><th>Diagnosis</th><th>Treatment</th><th>Notes</th></tr></thead>
          <tbody>
            ${meds.map(m => `<tr><td>${m.visitDate}</td><td>${escapeHtml(m.doctorName)}</td><td><strong>${escapeHtml(m.diagnosis)}</strong></td><td>${escapeHtml(m.treatment)}</td><td>${escapeHtml(m.notes || '-')}</td></tr>`).join('')}
          </tbody>
        </table>
      `}

      <h3 style="font-size:1.05rem; margin-bottom:0.5rem; color:#0f172a;">Appointment Visits (${appts.length})</h3>
      ${appts.length === 0 ? '<p style="color:#64748b; font-size:0.9rem;">No appointments booked for this patient.</p>' : `
        <table class="data-table">
          <thead><tr><th>Date</th><th>Time</th><th>Doctor</th><th>Specialization</th><th>Status</th></tr></thead>
          <tbody>
            ${appts.map(a => `<tr><td>${a.appDate}</td><td>${a.appTime}</td><td>${escapeHtml(a.doctorName)}</td><td>${escapeHtml(a.doctorSpecialization)}</td><td><span class="badge ${a.status==='Completed'?'badge-completed':a.status==='Cancelled'?'badge-cancelled':'badge-scheduled'}">${a.status}</span></td></tr>`).join('')}
          </tbody>
        </table>
      `}
    `;

    detailsCard.scrollIntoView({ behavior: 'smooth' });
  } catch (err) {
    detailsContent.innerHTML = `<p style="color:red;">Failed to load full profile: ${err.message}</p>`;
  }
}

// =========================================================================
// FR8: GENERATE REPORTS
// =========================================================================
async function generateReport() {
  const type = document.getElementById('reportType').value;
  const startDate = document.getElementById('reportStartDate').value;
  const endDate = document.getElementById('reportEndDate').value;

  const titleEl = document.getElementById('reportTitle');
  const thead = document.getElementById('reportTableHead');
  const tbody = document.getElementById('reportTableBody');
  const genDate = document.getElementById('reportGenDate');

  genDate.textContent = 'Generated on: ' + new Date().toLocaleString();
  tbody.innerHTML = '<tr><td>Generating report data...</td></tr>';

  let url = `${API_BASE}/reports?type=${type}`;
  if (startDate) url += `&startDate=${startDate}`;
  if (endDate) url += `&endDate=${endDate}`;

  try {
    const res = await fetch(url);
    const data = await res.json();
    if (!data.success) {
      tbody.innerHTML = `<tr><td style="color:red;">Error: ${data.message}</td></tr>`;
      return;
    }

    const report = data.report || [];

    if (type === 'visits') {
      titleEl.textContent = 'Patient Visits & Medical Consultation Report';
      thead.innerHTML = `
        <tr>
          <th>Record ID</th>
          <th>Visit Date</th>
          <th>Patient Name</th>
          <th>Patient Phone</th>
          <th>Doctor</th>
          <th>Specialization</th>
          <th>Diagnosis</th>
          <th>Treatment</th>
        </tr>
      `;

      if (report.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">No visit records found in this range.</td></tr>';
      } else {
        tbody.innerHTML = report.map(r => `
          <tr>
            <td>#${r.recordId}</td>
            <td>${r.visitDate}</td>
            <td><strong>${escapeHtml(r.patientName)}</strong></td>
            <td>${escapeHtml(r.patientPhone)}</td>
            <td>${escapeHtml(r.doctorName)}</td>
            <td>${escapeHtml(r.specialization)}</td>
            <td>${escapeHtml(r.diagnosis)}</td>
            <td>${escapeHtml(r.treatment)}</td>
          </tr>
        `).join('');
      }
    } else {
      titleEl.textContent = 'Appointments Summary Report';
      thead.innerHTML = `
        <tr>
          <th>Appt ID</th>
          <th>Date</th>
          <th>Time</th>
          <th>Patient Name</th>
          <th>Patient Phone</th>
          <th>Doctor</th>
          <th>Specialization</th>
          <th>Status</th>
        </tr>
      `;

      if (report.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;">No appointments found in this range.</td></tr>';
      } else {
        tbody.innerHTML = report.map(r => `
          <tr>
            <td>#${r.appointmentId}</td>
            <td>${r.date}</td>
            <td>${r.time}</td>
            <td><strong>${escapeHtml(r.patientName)}</strong></td>
            <td>${escapeHtml(r.patientPhone)}</td>
            <td>${escapeHtml(r.doctorName)}</td>
            <td>${escapeHtml(r.specialization)}</td>
            <td><span class="badge ${r.status==='Completed'?'badge-completed':r.status==='Cancelled'?'badge-cancelled':'badge-scheduled'}">${r.status}</span></td>
          </tr>
        `).join('');
      }
    }
  } catch (err) {
    tbody.innerHTML = `<tr><td style="color:red;">Report compilation error: ${err.message}</td></tr>`;
  }
}

// =========================================================================
// FR2: USER MANAGEMENT (ADMIN ONLY)
// =========================================================================
async function loadUsers() {
  const tbody = document.getElementById('usersTableBody');
  tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">Loading user accounts...</td></tr>';

  try {
    const res = await fetch(`${API_BASE}/users`);
    const data = await res.json();
    if (data.success) {
      renderUsersTable(data.users || []);
    }
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="5" style="color:red; text-align:center;">Error: ${err.message}</td></tr>`;
  }
}

function renderUsersTable(list) {
  const tbody = document.getElementById('usersTableBody');
  if (!list || list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;">No users registered.</td></tr>';
    return;
  }

  tbody.innerHTML = list.map(u => `
    <tr>
      <td><strong>#${u.userId}</strong></td>
      <td>${escapeHtml(u.name)}</td>
      <td><span class="role-tag">${escapeHtml(u.role)}</span></td>
      <td><code>${escapeHtml(u.username)}</code></td>
      <td>
        ${u.username === 'admin' ? '<span style="color:#64748b; font-size:0.8rem;">Super Admin</span>' : `
          <button class="btn btn-danger btn-sm" onclick="deleteUser(${u.userId})">🗑️ Delete</button>
        `}
      </td>
    </tr>
  `).join('');
}

async function saveUser(e) {
  e.preventDefault();
  const name = document.getElementById('userName').value.trim();
  const role = document.getElementById('userRole').value;
  const username = document.getElementById('userUsername').value.trim();
  const password = document.getElementById('userPassword').value.trim();

  try {
    const res = await fetch(`${API_BASE}/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, role, username, password })
    });
    const data = await res.json();
    if (data.success) {
      showToast(data.message || 'User account created successfully!', 'success');
      document.getElementById('userForm').reset();
      loadUsers();
    } else {
      showToast(data.message || 'Failed to add user.', 'error');
    }
  } catch (err) {
    showToast('Network error: ' + err.message, 'error');
  }
}

async function deleteUser(id) {
  if (!confirm(`Are you sure you want to delete User Account #${id}?`)) return;

  try {
    const res = await fetch(`${API_BASE}/users?id=${id}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      showToast(data.message, 'success');
      loadUsers();
    } else {
      showToast(data.message, 'error');
    }
  } catch (err) {
    showToast('Delete error: ' + err.message, 'error');
  }
}

// =========================================================================
// SECTION 5.7: DATABASE BACKUP SNAPSHOT
// =========================================================================
async function takeBackup() {
  const out = document.getElementById('backupOutput');
  out.style.display = 'block';
  out.innerHTML = 'Connecting to database snapshot service...';

  try {
    const res = await fetch(`${API_BASE}/backup`);
    const data = await res.json();
    if (data.success) {
      out.innerHTML = `
        <h4 style="color:#16a34a;">✅ Backup Completed Successfully!</h4>
        <p><strong>Snapshot Timestamp:</strong> ${data.timestamp}</p>
        <p><strong>Current Database Counts:</strong></p>
        <ul style="margin-left:1.5rem; margin-top:0.25rem;">
          <li>Total Patients: ${data.counts.totalPatients}</li>
          <li>Total Doctors: ${data.counts.totalDoctors}</li>
          <li>Total Appointments: ${data.counts.totalAppointments}</li>
          <li>Total Medical Records: ${data.counts.totalMedicalRecords}</li>
          <li>Total System Users: ${data.counts.totalUsers}</li>
        </ul>
      `;
      showToast('Database backup completed!', 'success');
    }
  } catch (err) {
    out.innerHTML = `<span style="color:red;">Backup failed: ${err.message}</span>`;
  }
}

// =========================================================================
// UTILITY HELPERS
// =========================================================================
function showToast(message, type = 'info') {
  const toast = document.getElementById('toast');
  toast.textContent = message;
  toast.className = `show toast-${type}`;
  setTimeout(() => {
    toast.className = '';
  }, 3500);
}

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
