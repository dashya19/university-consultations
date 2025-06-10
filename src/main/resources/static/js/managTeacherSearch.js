document.addEventListener("DOMContentLoaded", () => {
    const teacherSearchInput = document.getElementById("teacherSearchInput");
    const teacherResultsContainer = document.getElementById("teacherResults");
    const selectedTeacherName = document.getElementById("selectedTeacherName");
    if (teacherSearchInput) {
        teacherSearchInput.addEventListener("input", () => {
            const query = teacherSearchInput.value.trim();
            if (query.length < 3) {
                teacherResultsContainer.innerHTML = "";
                return;
            }
            fetch(`/api/teachers/search?query=${encodeURIComponent(query)}`)
                .then(handleResponse)
                .then(data => displayTeacherResults(data, teacherResultsContainer, selectedTeacherName))
                .catch(handleSearchError.bind(null, teacherResultsContainer));
        });
    }
    function displayTeacherResults(data, container, nameElement) {
        container.innerHTML = "";
        if (data.length === 0) {
            container.innerHTML = `<div class="no-results">Преподаватель не найден</div>`;
            return;
        }
        data.forEach(teacher => {
            const teacherCard = document.createElement("div");
            teacherCard.classList.add("teacher-card");
            teacherCard.innerHTML = `
                <div class="teacher-info">
                    <strong>${teacher.lastName} ${teacher.firstName} ${teacher.patronymic}</strong><br>
                    Должность: ${teacher.position}
                </div>
            `;
            teacherCard.addEventListener("click", () => {
                nameElement.textContent = `${teacher.lastName} ${teacher.firstName} ${teacher.patronymic}`;
                loadTeacherArchive(teacher.id);
                showTeacherArchiveSection();
            });
            container.appendChild(teacherCard);
        });
    }
    function loadTeacherArchive(teacherId) {
        currentTeacherId = teacherId;
        fetch(`/api/teachers/${teacherId}/archive`)
            .then(handleResponse)
            .then(data => renderTeacherArchiveTable(data))
            .catch(handleArchiveError);
    }
    function renderTeacherArchiveTable(data) {
        const table = document.getElementById('teacherArchiveTable');
        const tableBody = table.querySelector('tbody');
        const noArchiveMessage = document.getElementById('noTeacherArchiveMessage');
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

            const studentName = `${record.studentLastName} ${record.studentFirstName} ${record.studentPatronymic || ''}`.trim();

            const cells = [
                index + 1,
                new Date(record.consultationDate).toLocaleDateString('ru-RU'),
                record.startTime,
                record.endTime,
                record.room,
                studentName,
                record.studentGroupName,
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

        // Всегда показываем первые 2 колонки (№ и Дата)
        columnsToShow[0] = true;
        columnsToShow[1] = true;

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