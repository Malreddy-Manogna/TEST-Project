let isAuthenticated = false;

function showSection(section) {
    document.getElementById('mainMenu').style.display = 'none';
    if (section === 'auth') section = 'authenticate';
    document.getElementById(section + 'Section').style.display = 'block';
}

function backToMenu() {
    document.querySelectorAll('.section').forEach(sec => sec.style.display = 'none');
    document.getElementById('mainMenu').style.display = 'block';
}

function logout() {
    isAuthenticated = false;
    updateAuthStatus();
    document.getElementById('encryptBtn').style.display = 'none';
    document.getElementById('decryptBtn').style.display = 'none';
    alert('Logged out successfully.');
}

function updateAuthStatus() {
    const statusDiv = document.getElementById('authStatus');
    statusDiv.textContent = isAuthenticated ? 'Authenticated' : 'Not Authenticated';
    statusDiv.className = isAuthenticated ? 'status authenticated' : 'status not-authenticated';
}

function exitApp() {
    // For web app, perhaps close the window or show a message
    alert('Exiting app. Close the browser tab to exit.');
}

async function enroll() {
    const fileInput = document.getElementById('enrollFile');
    const resultDiv = document.getElementById('enrollResult');
    if (!fileInput.files[0]) {
        showResult(resultDiv, 'Please select a file.', 'error');
        return;
    }
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    try {
        const response = await fetch('/enroll', {
            method: 'POST',
            body: formData
        });
        const result = await response.json();
        showResult(resultDiv, result.success ? 'Enrolled successfully!' : 'Enrollment failed.', result.success ? 'success' : 'error');
    } catch (error) {
        showResult(resultDiv, 'Error: ' + error.message, 'error');
    }
}

async function authenticate() {
    const fileInput = document.getElementById('authFile');
    const resultDiv = document.getElementById('authResult');
    if (!fileInput.files[0]) {
        showResult(resultDiv, 'Please select a file.', 'error');
        return;
    }
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    try {
        const response = await fetch('/authenticate', {
            method: 'POST',
            body: formData
        });
        const result = await response.json();
        isAuthenticated = result.success;
        updateAuthStatus();
        if (isAuthenticated) {
            document.getElementById('encryptBtn').style.display = 'inline-block';
            document.getElementById('decryptBtn').style.display = 'inline-block';
        } else {
            document.getElementById('encryptBtn').style.display = 'none';
            document.getElementById('decryptBtn').style.display = 'none';
        }
        showResult(resultDiv, result.success ? 'Authenticated successfully!' : 'Authentication failed.', result.success ? 'success' : 'error');
    } catch (error) {
        showResult(resultDiv, 'Error: ' + error.message, 'error');
    }
}

async function encrypt() {
    const srcInput = document.getElementById('encryptSrc');
    const destInput = document.getElementById('encryptDest');
    const resultDiv = document.getElementById('encryptResult');
    if (!srcInput.files[0] || !destInput.files[0]) {
        showResult(resultDiv, 'Please select a source file and destination folder.', 'error');
        return;
    }
    const destPath = destInput.files[0].webkitRelativePath.split('/')[0] + '/' + srcInput.files[0].name + '.vault';
    const formData = new FormData();
    formData.append('src', srcInput.files[0]);
    formData.append('dest', destPath);
    try {
        const response = await fetch('/encrypt', {
            method: 'POST',
            body: formData
        });
        const result = await response.json();
        showResult(resultDiv, result.success ? 'Encrypted successfully!' : 'Encryption failed: ' + (result.error || ''), result.success ? 'success' : 'error');
    } catch (error) {
        showResult(resultDiv, 'Error: ' + error.message, 'error');
    }
}

async function decrypt() {
    const srcInput = document.getElementById('decryptSrc');
    const destFolderInput = document.getElementById('decryptDestFolder');
    const filenameInput = document.getElementById('decryptFilename');
    const resultDiv = document.getElementById('decryptResult');
    if (!srcInput.files[0] || !destFolderInput.files[0] || !filenameInput.value.trim()) {
        showResult(resultDiv, 'Please select a source file, destination folder, and enter a filename.', 'error');
        return;
    }
    const destPath = destFolderInput.files[0].webkitRelativePath.split('/')[0] + '/' + filenameInput.value.trim();
    const formData = new FormData();
    formData.append('src', srcInput.files[0]);
    formData.append('dest', destPath);
    try {
        const response = await fetch('/decrypt', {
            method: 'POST',
            body: formData
        });
        const result = await response.json();
        showResult(resultDiv, result.success ? 'Decrypted successfully!' : 'Decryption failed: ' + (result.error || ''), result.success ? 'success' : 'error');
    } catch (error) {
        showResult(resultDiv, 'Error: ' + error.message, 'error');
    }
}

function showResult(div, message, type) {
    div.textContent = message;
    div.className = type;
    div.style.display = 'block';
}
