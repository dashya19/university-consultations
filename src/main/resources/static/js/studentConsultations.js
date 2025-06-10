document.addEventListener('DOMContentLoaded', function() {
    document.querySelector('.consultations-table').addEventListener('click', function(e) {
        if (e.target.classList.contains('btn-delete')) {
            const signupId = e.target.getAttribute('data-signup-id');
            if (confirm('Вы уверены, что хотите отменить запись на эту консультацию?')) {
                fetch('/student/consultation/cancel', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                    },
                    body: `signupId=${signupId}`
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert(data.success);
                        window.location.reload();
                    } else if (data.error) {
                        alert(data.error);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Произошла ошибка при отмене записи');
                });
            }
        }
    });
});