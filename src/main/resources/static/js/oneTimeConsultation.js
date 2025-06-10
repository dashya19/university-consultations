document.getElementById('createOneTimeConsultationBtn').addEventListener('click', openOneTimeConsultationModal);
function openOneTimeConsultationModal() {
    const modal = document.getElementById('createOneTimeConsultationModal');
    modal.style.display = 'block';
    document.getElementById('oneTimeConsultationDate').min = new Date().toISOString().split('T')[0];
    modal.querySelector('.modal-content').scrollTop = 0;
}
function closeOneTimeConsultationModal() {
    document.getElementById('createOneTimeConsultationModal').style.display = 'none';
}
document.getElementById('oneTimeConsultationForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const maxStudents = document.getElementById('oneTimeConsultationMaxStudents').value;
    if (maxStudents > 10000) {
        alert('Максимальное количество студентов не может превышать 10000');
        return;
    }
    const formData = {
        date: document.getElementById('oneTimeConsultationDate').value,
        startTime: document.getElementById('oneTimeConsultationStartTime').value,
        endTime: document.getElementById('oneTimeConsultationEndTime').value,
        room: document.getElementById('oneTimeConsultationRoom').value,
        maxStudents: document.getElementById('oneTimeConsultationMaxStudents').value,
        isOneTime: true,
        _csrf: document.querySelector('input[name="_csrf"]').value
    };
    fetch('/teacher/consultation/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Accept': 'application/json'
        },
        body: new URLSearchParams(formData)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert(data.success);
            closeOneTimeConsultationModal();
            window.location.reload();
        } else {
            alert(data.error);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Произошла ошибка при создании консультации');
    });
});