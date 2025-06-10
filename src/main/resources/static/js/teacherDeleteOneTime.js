function deleteOneTimeConsultation(button) {
    const consultationId = button.getAttribute('data-consultation-id');
    if (!confirm('Вы уверены, что хотите удалить эту разовую консультацию? Все связанные записи студентов также будут удалены.')) {
        return;
    }
    fetch(`/teacher/consultation/delete-one-time/${consultationId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
        }
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => Promise.reject(err));
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            alert(data.success);
            const row = button.closest('tr');
            const table = row.closest('table');
            const section = table.closest('.consultation-section');

            row.remove();

            // Проверяем, остались ли еще строки в таблице
            if (table.querySelectorAll('tbody tr').length === 0) {
                // Полностью заменяем содержимое раздела на сообщение "нет консультаций"
                section.innerHTML = `
                    <h3>Разовые консультации</h3>
                    <div class="no-consultations-message">
                        <img src="/images/calendar-empty.png" alt="Нет консультаций" class="empty-icon">
                        <p>Разовых консультаций пока нет</p>
                    </div>
                `;
            }
        } else {
            throw new Error(data.error || 'Неизвестная ошибка');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ошибка при удалении: ' + error.message);
    });
}