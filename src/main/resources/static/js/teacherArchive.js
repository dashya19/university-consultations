function loadTeacherArchive() {
    fetch('/teacher/archive')
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        const tableContainer = document.getElementById('teacherArchiveTableContainer');
        const tableBody = document.querySelector('#teacherArchiveTable tbody');
        const noArchiveMessage = document.getElementById('noTeacherArchiveMessage');
        tableBody.innerHTML = '';
        const visibleRecords = data;
        if (visibleRecords.length === 0) {
            tableContainer.style.display = 'none';
            noArchiveMessage.style.display = 'block';
            return;
        }
        tableContainer.style.display = 'block';
        noArchiveMessage.style.display = 'none';
        visibleRecords.forEach((record, index) => {
            const row = document.createElement('tr');
            row.dataset.recordId = record.id;
            let attendance = '-';
            let attendanceValue = '';
            if (record.attended !== null) {
                attendance = record.attended ? 'Присутствовал' : 'Отсутствовал';
                attendanceValue = record.attended ? 'true' : 'false';
            }
            const debtStatus = record.debtStatus || 'Без задолженности';
            row.innerHTML = `
                <td>${index + 1}</td>
                <td>${new Date(record.consultationDate).toLocaleDateString('ru-RU')}</td>
                <td>${record.startTime ? record.startTime.split(':').slice(0, 2).join(':') : '-'}</td>
                <td>${record.endTime ? record.endTime.split(':').slice(0, 2).join(':') : '-'}</td>
                <td>${record.room}</td>
                <td>${record.student.lastName}</td>
                <td>${record.student.firstName}</td>
                <td>${record.student.patronymic || '-'}</td>
                <td>${record.student.groupName}</td>
                <td>${record.reason || '-'}</td>
                <td class="attendance-cell">
                    <span class="attendance-text">${attendance}</span>
                    <img src="/images/edit.png" alt="Редактировать" class="edit-icon edit-attendance" style="width:16px; height:16px; margin-left:5px; cursor:pointer;">
                    <select class="attendance-select" style="display:none;">
                        <option value="true" ${attendanceValue === 'true' ? 'selected' : ''}>Присутствовал</option>
                        <option value="false" ${attendanceValue === 'false' ? 'selected' : ''}>Отсутствовал</option>
                    </select>
                </td>
                <td class="debt-status-cell">
                    <span class="debt-status-text">${debtStatus}</span>
                    <img src="/images/edit.png" alt="Редактировать" class="edit-icon edit-debt-status" style="width:16px; height:16px; margin-left:5px; cursor:pointer;">
                    <select class="debt-status-select" style="display:none;">
                        <option value="Без задолженности" ${debtStatus === 'Без задолженности' ? 'selected' : ''}>Без задолженности</option>
                        <option value="Сдал задолженность" ${debtStatus === 'Сдал задолженность' ? 'selected' : ''}>Сдал задолженность</option>
                        <option value="Не сдал задолженность" ${debtStatus === 'Не сдал задолженность' ? 'selected' : ''}>Не сдал задолженность</option>
                    </select>
                </td>
                <td class="feedback-cell">
                    <span class="feedback-text">${record.feedback || '-'}</span>
                    <img src="/images/edit.png" alt="Редактировать" class="edit-icon edit-feedback" style="width:16px; height:16px; margin-left:5px; cursor:pointer;">
                    <textarea class="feedback-textarea" style="display:none; width:100%;">${record.feedback || ''}</textarea>
                </td>
                <td>
                    <button class="btn-action update-btn" data-record-id="${record.id}">Обновить</button>
                    <button class="btn-action hide-btn" data-record-id="${record.id}">Скрыть</button>
                </td>
            `;
            tableBody.appendChild(row);
        });
        addEditHandlers();
        addHideHandlers();
        adaptAllTablesForMobile();
    })
    .catch(error => {
        console.error('Error:', error);
        document.getElementById('teacherArchiveTableContainer').style.display = 'none';
        document.getElementById('noTeacherArchiveMessage').style.display = 'block';
        alert('Ошибка при загрузке архива: ' + error.message);
    });
}
function addEditHandlers() {
    document.querySelectorAll('.edit-attendance').forEach(icon => {
        icon.addEventListener('click', function() {
            const cell = this.closest('.attendance-cell');
            const textSpan = cell.querySelector('.attendance-text');
            const select = cell.querySelector('.attendance-select');
            textSpan.style.display = 'none';
            this.style.display = 'none';
            select.style.display = 'inline-block';
        });
    });
    document.querySelectorAll('.edit-debt-status').forEach(icon => {
        icon.addEventListener('click', function() {
            const cell = this.closest('.debt-status-cell');
            const textSpan = cell.querySelector('.debt-status-text');
            const select = cell.querySelector('.debt-status-select');
            textSpan.style.display = 'none';
            this.style.display = 'none';
            select.style.display = 'inline-block';
        });
    });
    document.querySelectorAll('.edit-feedback').forEach(icon => {
        icon.addEventListener('click', function() {
            const cell = this.closest('.feedback-cell');
            const textSpan = cell.querySelector('.feedback-text');
            const textarea = cell.querySelector('.feedback-textarea');
            textSpan.style.display = 'none';
            this.style.display = 'none';
            textarea.style.display = 'block';
            textarea.focus();
        });
    });
    document.querySelectorAll('.update-btn').forEach(button => {
        button.addEventListener('click', function() {
            const recordId = this.getAttribute('data-record-id');
            const row = this.closest('tr');
            const attendanceCell = row.querySelector('.attendance-cell');
            const debtStatusCell = row.querySelector('.debt-status-cell');
            const feedbackCell = row.querySelector('.feedback-cell');
            const attendanceSelect = attendanceCell.querySelector('.attendance-select');
            const attendance = attendanceSelect.style.display === 'none' ?
                null : attendanceSelect.value;
            const debtStatusSelect = debtStatusCell.querySelector('.debt-status-select');
            const debtStatus = debtStatusSelect.style.display === 'none' ?
                debtStatusCell.querySelector('.debt-status-text').textContent : debtStatusSelect.value;
            const feedbackTextarea = feedbackCell.querySelector('.feedback-textarea');
            const feedback = feedbackTextarea.style.display === 'none' ?
                feedbackCell.querySelector('.feedback-text').textContent : feedbackTextarea.value;
            updateArchiveRecord(recordId, attendance, debtStatus, feedback);
        });
    });
}
function addHideHandlers() {
    document.querySelectorAll('.hide-btn').forEach(button => {
        button.addEventListener('click', function() {
            const recordId = this.getAttribute('data-record-id');
            if (!confirm('Вы уверены, что хотите скрыть эту запись? Запись останется в базе данных, но не будет отображаться в вашем архиве.')) {
                return;
            }
            hideArchiveRecord(recordId);
        });
    });
}
function updateArchiveRecord(recordId, attended, debtStatus, feedback) {
    const payload = {
        id: parseInt(recordId),
        attended: attended === null ? null : attended === 'true',
        debtStatus: debtStatus === '-' ? null : debtStatus,
        feedback: feedback === '-' ? null : feedback
    };
    fetch('/teacher/archive/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
        },
        body: JSON.stringify(payload)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                throw new Error(err.error || 'Неизвестная ошибка сервера');
            });
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            const row = document.querySelector(`tr[data-record-id="${recordId}"]`);
            if (row) {
                const attendanceText = row.querySelector('.attendance-text');
                if (attendanceText) {
                    attendanceText.textContent = payload.attended === null ? '-' :
                                               payload.attended ? 'Присутствовал' : 'Отсутствовал';
                }
                const debtStatusText = row.querySelector('.debt-status-text');
                if (debtStatusText) {
                    debtStatusText.textContent = payload.debtStatus || 'Без задолженности';
                }
                const feedbackText = row.querySelector('.feedback-text');
                if (feedbackText) {
                    feedbackText.textContent = payload.feedback || '-';
                }
                const attendanceSelect = row.querySelector('.attendance-select');
                const debtStatusSelect = row.querySelector('.debt-status-select');
                const feedbackTextarea = row.querySelector('.feedback-textarea');
                const editIcons = row.querySelectorAll('.edit-icon');
                if (attendanceSelect) attendanceSelect.style.display = 'none';
                if (debtStatusSelect) debtStatusSelect.style.display = 'none';
                if (feedbackTextarea) feedbackTextarea.style.display = 'none';
                if (attendanceText) attendanceText.style.display = 'inline';
                if (debtStatusText) debtStatusText.style.display = 'inline';
                if (feedbackText) feedbackText.style.display = 'inline';
                editIcons.forEach(icon => icon.style.display = 'inline');
            }
        } else {
            throw new Error(data.error || 'Неизвестная ошибка');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ошибка при обновлении: ' + error.message);
    });
}
function hideArchiveRecord(recordId) {
    fetch(`/teacher/archive/hide/${recordId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
        }
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => {
                if (err && err.error) {
                    throw new Error(err.error);
                } else {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
            });
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            const row = document.querySelector(`tr[data-record-id="${recordId}"]`);
            if (row) row.remove();
            const tableContainer = document.getElementById('teacherArchiveTableContainer');
            const noArchiveMessage = document.getElementById('noTeacherArchiveMessage');
            if (document.querySelectorAll('#teacherArchiveTable tbody tr').length === 0) {
                tableContainer.style.display = 'none';
                noArchiveMessage.style.display = 'block';
            }
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ошибка при скрытии записи: ' + error.message);
    });
}
document.querySelector('.bottom-box[data-tab="archive-records"]').addEventListener('click', loadTeacherArchive);