let currentStudentId = null;
let currentTeacherId = null;
let studentAttendanceChart = null;
let teacherAttendanceChart = null;
let studentDebtChart = null;
let teacherDebtChart = null;
function handleResponse(response) {
    if (!response.ok) {
        throw new Error('Ошибка сервера');
    }
    return response.json();
}
function handleSearchError(container, error) {
    console.error("Ошибка при поиске:", error);
    container.innerHTML = `<div class="error-message">Произошла ошибка при поиске</div>`;
}
function handleArchiveError(error) {
    console.error('Error loading archive:', error);
    alert('Ошибка при загрузке архива: ' + error.message);
}
function handleAnalysisError(error) {
    console.error('Error loading analysis:', error);
    alert('Ошибка при загрузке статистики: ' + error.message);
}
function adaptAllTablesForMobile() {
}