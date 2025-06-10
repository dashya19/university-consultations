document.addEventListener("DOMContentLoaded", () => {
    const searchInput = document.getElementById("teacherSearchInput");
    const resultsContainer = document.getElementById("teacherResults");
    const registerBtn = document.getElementById("registerConsultationBtn");
    const mainContent = document.querySelector(".consultation-main-content");
    const searchSection = document.getElementById("consultationRegistration");
    const consultationsSection = document.getElementById("teacherConsultationSection");
    const backToProfileLinks = document.querySelectorAll(".back-to-profile");
    const backToSearch = document.getElementById("backToSearch");
    const selectedTeacherName = document.getElementById("selectedTeacherName");
    const consultationsList = document.getElementById("consultationsList");
    function showSection(sectionToShow) {
        mainContent.style.display = "none";
        searchSection.style.display = "none";
        consultationsSection.style.display = "none";
        if (sectionToShow === 'main') {
            mainContent.style.display = "block";
        } else if (sectionToShow === 'search') {
            searchSection.style.display = "block";
        } else if (sectionToShow === 'consultations') {
            consultationsSection.style.display = "block";
        }
        if (registerBtn) {
            registerBtn.style.display = "block";
        }
    }
    if (registerBtn) {
        registerBtn.addEventListener("click", () => {
            showSection('search');
            searchInput.focus();
        });
    }
    backToProfileLinks.forEach(link => {
        link.addEventListener("click", () => {
            showSection('main');
        });
    });
    if (backToSearch) {
        backToSearch.addEventListener("click", () => {
            showSection('search');
            searchInput.focus();
        });
    }
    if (searchInput) {
        searchInput.addEventListener("input", () => {
            const query = searchInput.value.trim();
            if (query.length < 3) {
                resultsContainer.innerHTML = "";
                return;
            }
            fetch(`/api/teachers/search?query=${encodeURIComponent(query)}`)
                .then(response => response.json())
                .then(data => {
                    resultsContainer.innerHTML = "";
                    if (data.length === 0) {
                        resultsContainer.innerHTML = `<div class="no-results">Преподаватель не найден</div>`;
                        return;
                    }
                    data.forEach(teacher => {
                        const teacherCard = document.createElement("div");
                        teacherCard.classList.add("teacher-card");
                        teacherCard.innerHTML = `
                            <div class="teacher-info">
                                <strong>${teacher.lastName} ${teacher.firstName} ${teacher.patronymic}</strong><br>
                                ${teacher.position}, ${teacher.academicTitle}
                            </div>
                        `;
                        teacherCard.addEventListener("click", () => {
                            showTeacherConsultations(teacher);
                        });
                        resultsContainer.appendChild(teacherCard);
                    });
                })
                .catch(error => {
                    console.error("Ошибка при получении преподавателей:", error);
                });
        });
    }
    function showTeacherConsultations(teacher) {
        showSection('consultations');
        selectedTeacherName.textContent = `${teacher.lastName} ${teacher.firstName} ${teacher.patronymic}`;
        fetch(`/api/teachers/${teacher.id}/consultations?forceUpdate=true`)
            .then(res => res.json())
            .then(consultations => {
                if (!Array.isArray(consultations) || consultations.length === 0) {
                    consultationsList.innerHTML = `<p>У преподавателя пока нет доступных консультаций.</p>`;
                    return;
                }
                consultationsList.innerHTML = consultations.map((c) => `
                    <div class="consultation-record-block" data-consultation="${c.id}">
                        <div class="consultation-info">
                            <p><strong>Дата:</strong> ${formatDate(c.consultationDate)}</p>
                            <p><strong>Время:</strong> ${c.startTime} - ${c.endTime}</p>
                            <p><strong>Аудитория:</strong> ${c.room}</p>
                            <p class="students-count" data-consultation-id="${c.id}"><strong>Записано:</strong> Загрузка...</p>
                            <div class="signup-container" data-consultation-id="${c.id}">
                                ${!c.cancelled ? `
                                    <button class="btn btn-success signup-btn" onclick="openRegisterModal(${c.id})">
                                        Записаться на консультацию
                                    </button>
                                    <div class="full-message" style="display: none; color: red; margin-top: 5px;">
                                        Консультация заполнена
                                    </div>
                                ` : ''}
                            </div>
                        </div>
                        <hr/>
                    </div>
                `).join('');
                loadSignupsCount();
            })
            .catch(err => {
                console.error("Ошибка при загрузке консультаций:", err);
            });
    }
    function loadSignupsCount() {
        document.querySelectorAll('.students-count').forEach(element => {
            const consultationId = element.getAttribute('data-consultation-id');
            const signupContainer = document.querySelector(`.signup-container[data-consultation-id="${consultationId}"]`);
            fetch(`/api/consultations/${consultationId}/signups-count`)
                .then(response => response.json())
                .then(data => {
                    if (data.max === 10000) {
                        element.innerHTML = `<strong>Записано:</strong> ${data.current} (не ограничено)`;
                    } else {
                        element.innerHTML = `<strong>Записано:</strong> ${data.current}/${data.max}`;
                    }
                    if (signupContainer) {
                        if (data.current >= data.max && data.max !== 10000) {
                            signupContainer.innerHTML = `
                                <div class="full-message" style="color: red;">
                                    Консультация заполнена
                                </div>
                            `;
                        } else {
                            signupContainer.innerHTML = `
                                <button class="btn btn-success signup-btn" onclick="openRegisterModal(${consultationId})">
                                    Записаться на консультацию
                                </button>
                            `;
                        }
                    }
                })
                .catch(error => {
                    console.error('Ошибка:', error);
                    element.innerHTML = `<strong>Записано:</strong> Ошибка загрузки`;
                });
        });
    }
});
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU');
}
function openRegisterModal(consultationId) {
    const modal = document.getElementById("registerConsultationModal");
    const consultationIdInput = document.getElementById("consultationIdInput");
    consultationIdInput.value = consultationId;
    modal.style.display = "block";
}
function closeRegisterModal() {
    document.getElementById("registerConsultationModal").style.display = "none";
}
window.updateSignupCount = function(consultationId) {
    window.location.reload();
};