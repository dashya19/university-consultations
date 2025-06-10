function loadArchiveData() {
    fetch('/student/archive')
    .then(response => response.json())
    .then(data => {
        const tableContainer = document.getElementById('archiveTableContainer');
        const tableBody = document.querySelector('#archiveTable tbody');
        const noArchiveMessage = document.getElementById('noArchiveMessage');
        tableBody.innerHTML = '';
        if (data.length === 0) {
            tableContainer.style.display = 'none';
            noArchiveMessage.style.display = 'block';
            return;
        }
        tableContainer.style.display = 'block';
        noArchiveMessage.style.display = 'none';
        data.forEach((record, index) => {
            const row = document.createElement('tr');
            const teacherName = record.teacher ?
                `${record.teacher.lastName} ${record.teacher.firstName} ${record.teacher.patronymic}` :
                "Преподаватель (не указан)";
            const lastName = record.teacher ? record.teacher.lastName : "-";
            const firstName = record.teacher ? record.teacher.firstName : "-";
            const patronymic = record.teacher ? record.teacher.patronymic : "-";
            let attendance = 'Не указано';
            if (record.attended !== null) {
                attendance = record.attended ? 'Присутствовал' : 'Отсутствовал';
            }
            const debtStatus = record.debtStatus || 'Без задолженности';
            const consultationDate = record.consultationDate ?
            new Date(record.consultationDate).toLocaleDateString('ru-RU') : "-";
            row.innerHTML = `
                <td>${index + 1}</td>
                <td>${lastName}</td>
                <td>${firstName}</td>
                <td>${patronymic}</td>
                <td>${consultationDate}</td>
                <td>${record.startTime ? record.startTime.split(':').slice(0, 2).join(':') : '-'}</td>
                <td>${record.endTime ? record.endTime.split(':').slice(0, 2).join(':') : '-'}</td>
                <td>${record.room || '-'}</td>
                <td>${record.reason || '-'}</td>
                <td>${attendance}</td>
                <td>${debtStatus}</td>
                <td>${record.feedback || '-'}</td>
            `;
            tableBody.appendChild(row);
        });
        adaptAllTablesForMobile();
    })
    .catch(error => {
        console.error('Error loading archive data:', error);
        document.getElementById('archiveTableContainer').style.display = 'none';
        document.getElementById('noArchiveMessage').style.display = 'block';
    });
}
document.querySelector('.bottom-box[data-tab="records"]').addEventListener('click', loadArchiveData);