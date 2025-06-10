document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById("consultationRegisterForm");
    if (registerForm) {
        registerForm.addEventListener("submit", function(e) {
            e.preventDefault();
            const consultationId = document.getElementById("consultationIdInput").value;
            const signupBtn = document.querySelector(`.signup-btn[data-consultation-id="${consultationId}"]`);
            if (signupBtn && signupBtn.disabled) {
                alert("Консультация уже заполнена");
                return;
            }
            const reason = document.getElementById("registrationReason").value;
            const csrfToken = document.querySelector("input[name='_csrf']").value;

            if (!reason.trim()) {
                alert("Пожалуйста, укажите причину записи");
                return;
            }

            fetch("/api/consultations/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "X-CSRF-TOKEN": csrfToken
                },
                body: JSON.stringify({
                    consultationId: parseInt(consultationId),
                    reason: reason
                })
            })
            .then(response => {
                if (response.status === 401) {
                    window.location.href = '/login';
                    return;
                }
                if (!response.ok) {
                    return response.json().then(err => {
                        throw new Error(err.error || "Неизвестная ошибка");
                    });
                }
                return response.json();
            })
            .then(data => {
                if (data.success) {
                    alert(data.success);
                    closeRegisterModal();
                    // Обновляем страницу, чтобы показать новую запись
                    window.location.reload();
                } else {
                    throw new Error(data.error || "Неизвестная ошибка");
                }
            })
            .catch(error => {
                console.error("Error:", error);
                alert(error.message || "Произошла ошибка при записи");
            });
        });
    }
});
function openRegisterModal(consultationId) {
    document.getElementById("consultationIdInput").value = consultationId;
    document.getElementById("registrationReason").value = '';
    document.getElementById("registerConsultationModal").style.display = "block";
}
function closeRegisterModal() {
    document.getElementById("registerConsultationModal").style.display = "none";
}