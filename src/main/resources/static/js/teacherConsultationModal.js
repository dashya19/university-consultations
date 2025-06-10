function openConsultationModal() {
    const modal = document.getElementById('createConsultationModal');
    modal.style.display = 'block';
    modal.querySelector('.modal-content').scrollTop = 0;
}
function closeConsultationModal() {
    document.getElementById('createConsultationModal').style.display = 'none';
}
function toggleBiWeeklyOptions(checkbox) {
    const biWeeklyOptions = document.getElementById('biWeeklyOptions');
    biWeeklyOptions.style.display = checkbox.checked ? 'block' : 'none';
}
document.getElementById('createConsultationBtn').addEventListener('click', openConsultationModal);
document.getElementById('consultationForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const maxStudents = document.getElementById('consultationMaxStudents').value;
    if (maxStudents > 10000) {
        alert('Максимальное количество студентов не может превышать 10000');
        return;
    }
    const isBiWeekly = document.getElementById('isBiWeekly').checked;
        const startNextWeek = document.getElementById('startNextWeek').checked;
    const formData = {
        dayOfWeek: document.getElementById('consultationDay').value,  // 1-7
        startTime: document.getElementById('consultationStartTime').value,
        endTime: document.getElementById('consultationEndTime').value,
        room: document.getElementById('consultationRoom').value,
        maxStudents: document.getElementById('consultationMaxStudents').value,
        expirationDate: document.getElementById('expirationDate').value,
        expirationTime: document.getElementById('expirationTime').value,
        isBiWeekly: isBiWeekly,
                startNextWeek: startNextWeek,
        _csrf: document.querySelector('input[name="_csrf"]').value
    };
    if ((formData.expirationDate && !formData.expirationTime) ||
        (!formData.expirationDate && formData.expirationTime)) {
        alert('Для автоматического удаления необходимо указать и дату, и время');
        return;
    }
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
            closeConsultationModal();
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
window.addEventListener('click', function(event) {
    const fieldModal = document.getElementById('editFieldModal');
    const consultationModal = document.getElementById('createConsultationModal');
    if (event.target === fieldModal) {
        closeFieldModal();
    }
    if (event.target === consultationModal) {
        closeConsultationModal();
    }
});