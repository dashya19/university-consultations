function deleteConsultationTemplate(button) {
    const templateId = button.getAttribute('data-template-id');
    if (!confirm('Вы уверены, что хотите удалить этот шаблон консультации? Все связанные консультации и записи также будут удалены.')) {
        return;
    }
    fetch(`/teacher/consultation/delete/${templateId}`, {
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
            window.location.reload(); // ← Вот эта строка

            // Проверяем, остались ли еще шаблоны в таблице
            if (table.querySelectorAll('tbody tr').length === 0) {
                // Полностью заменяем содержимое раздела на сообщение "нет консультаций"
                section.innerHTML = `
                    <h3>Регулярные консультации</h3>
                    <div class="no-consultations-message">
                        <img src="/images/calendar-empty.png" alt="Нет консультаций" class="empty-icon">
                        <p>Регулярных консультаций пока нет</p>
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