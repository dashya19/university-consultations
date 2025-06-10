function deleteConsultation(button) {
    const consultationId = button.getAttribute('data-consultation-id');
    if (!confirm('Вы уверены, что хотите удалить эту консультацию? Все связанные записи студентов также будут удалены.')) {
        return;
    }
    fetch(`/teacher/consultation/delete-consultation/${consultationId}`, {
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
            row.remove();
            if (table.querySelectorAll('tbody tr').length === 0) {
                const container = table.closest('.consultations-table-container');
                const noConsultationsMessage = container.nextElementSibling;
                if (noConsultationsMessage && noConsultationsMessage.classList.contains('no-consultations-message')) {
                    noConsultationsMessage.style.display = 'block';
                }
            }
            if (data.reloadNeeded) {
                window.location.reload();
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