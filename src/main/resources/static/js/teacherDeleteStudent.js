function deleteStudentSignup(button) {
    const signupId = button.getAttribute('data-signup-id');
    document.getElementById('signupIdInput').value = signupId;
    document.getElementById('cancelReason').value = '';
    document.getElementById('cancelSignupModal').style.display = 'block';
}

function closeCancelModal() {
    document.getElementById('cancelSignupModal').style.display = 'none';
}

// Обработка формы отмены записи
document.getElementById('cancelSignupForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const signupId = document.getElementById('signupIdInput').value;
    const reason = document.getElementById('cancelReason').value;

    if (!reason.trim()) {
        alert('Пожалуйста, укажите причину отмены');
        return;
    }

    fetch(`/teacher/signup/delete/${signupId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
        },
        body: JSON.stringify({ reason: reason })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Ошибка при удалении записи');
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            closeCancelModal();
            alert('Запись студента успешно удалена');
            // Обновляем страницу или таблицу
            location.reload();
        } else {
            alert(data.error || 'Ошибка при удалении записи');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ошибка при удалении записи: ' + error.message);
    });
});