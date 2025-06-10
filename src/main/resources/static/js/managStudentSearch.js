document.addEventListener("DOMContentLoaded", () => {
    const searchInput = document.getElementById("studentSearchInput");
    const resultsContainer = document.getElementById("studentResults");
    const selectedStudentName = document.getElementById("selectedStudentName");
    if (searchInput) {
        searchInput.addEventListener("input", () => {
            const query = searchInput.value.trim();
            if (query.length < 3) {
                resultsContainer.innerHTML = "";
                return;
            }
            fetch(`/api/students/search?query=${encodeURIComponent(query)}`)
                .then(handleResponse)
                .then(data => displayStudentResults(data, resultsContainer, selectedStudentName))
                .catch(handleSearchError.bind(null, resultsContainer));
        });
    }
    function displayStudentResults(data, container, nameElement) {
        container.innerHTML = "";
        if (data.length === 0) {
            container.innerHTML = `<div class="no-results">Студент не найден</div>`;
            return;
        }
        data.forEach(student => {
            const studentCard = document.createElement("div");
            studentCard.classList.add("student-card");
            studentCard.innerHTML = `
                <div class="student-info">
                    <strong>${student.lastName} ${student.firstName} ${student.patronymic}</strong><br>
                    Группа: ${student.groupName}
                </div>
            `;
            studentCard.addEventListener("click", () => {
                nameElement.textContent = `${student.lastName} ${student.firstName} ${student.patronymic}`;
                loadStudentArchive(student.id);
                showArchiveSection();
            });
            container.appendChild(studentCard);
        });
    }
    function loadStudentArchive(studentId) {
        currentStudentId = studentId;
        fetch(`/api/students/${studentId}/archive`)
            .then(handleResponse)
            .then(data => renderStudentArchiveTable(data))
            .catch(handleArchiveError);
    }
    function renderStudentArchiveTable(data) {
        const table = document.getElementById('studentArchiveTable');
        const tableBody = table.querySelector('tbody');
        const noArchiveMessage = document.getElementById('noStudentArchiveMessage');
        const headers = table.querySelectorAll('th');

        tableBody.innerHTML = '';

        if (data.length === 0) {
            noArchiveMessage.style.display = 'block';
            table.style.display = 'none';
            return;
        }

        noArchiveMessage.style.display = 'none';
        table.style.display = 'table';

        // Сначала скроем все колонки
        headers.forEach(header => header.style.display = 'none');

        // Массив для отслеживания колонок, которые нужно показать
        const columnsToShow = new Array(headers.length).fill(false);

        data.forEach((record, index) => {
            const row = document.createElement('tr');
            row.dataset.recordId = record.id;

            let attendance = '-';
            if (record.attended !== null) {
                attendance = record.attended ? 'Присутствовал' : 'Отсутствовал';
            }

            const cells = [
                index + 1,
                new Date(record.consultationDate).toLocaleDateString('ru-RU'),
                record.startTime,
                record.endTime,
                record.room,
                `${record.teacherLastName} ${record.teacherFirstName} ${record.teacherPatronymic || ''}`,
                record.reason || '-',
                attendance,
                record.debtStatus || '-',
                record.feedback || 'Нет отзыва'
            ];

            // Проверяем, какие колонки содержат данные
            cells.forEach((cell, i) => {
                if (cell !== '-' && cell !== '') {
                    columnsToShow[i] = true;
                }
            });

            row.innerHTML = cells.map(cell => `<td>${cell}</td>`).join('');
            tableBody.appendChild(row);
        });

        // Показываем только те колонки, которые содержат данные
        headers.forEach((header, i) => {
            if (columnsToShow[i]) {
                header.style.display = 'table-cell';
                // Показываем соответствующие ячейки в каждой строке
                tableBody.querySelectorAll('tr').forEach(row => {
                    if (row.cells[i]) {
                        row.cells[i].style.display = 'table-cell';
                    }
                });
            }
        });

        adaptAllTablesForMobile();
    }
});