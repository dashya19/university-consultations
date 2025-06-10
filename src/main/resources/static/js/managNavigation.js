function showMainContent() {
    document.querySelector(".consultation-main-content").style.display = "block";
    document.getElementById("studentSearchSection").style.display = "none";
    document.getElementById("studentArchiveSection").style.display = "none";
    document.getElementById("studentAnalysisSection").style.display = "none";
}
function showSearchSection() {
    document.querySelector(".consultation-main-content").style.display = "none";
    document.getElementById("studentSearchSection").style.display = "block";
    document.getElementById("studentArchiveSection").style.display = "none";
    document.getElementById("studentAnalysisSection").style.display = "none";
}
function showArchiveSection() {
    document.querySelector(".consultation-main-content").style.display = "none";
    document.getElementById("studentSearchSection").style.display = "none";
    document.getElementById("studentArchiveSection").style.display = "block";
    document.getElementById("studentAnalysisSection").style.display = "none";
}
function showStudentAnalysisSection() {
    document.querySelector(".consultation-main-content").style.display = "none";
    document.getElementById("studentSearchSection").style.display = "none";
    document.getElementById("studentArchiveSection").style.display = "none";
    document.getElementById("studentAnalysisSection").style.display = "block";
    document.getElementById("studentAnalysisName").textContent =
        document.getElementById("selectedStudentName").textContent + " : Статистика";
}
function showTeacherMainContent() {
    document.querySelector("#teachers-content .consultation-main-content").style.display = "block";
    document.getElementById("teacherSearchSection").style.display = "none";
    document.getElementById("teacherArchiveSection").style.display = "none";
    document.getElementById("teacherAnalysisSection").style.display = "none";
}
function showTeacherSearchSection() {
    document.querySelector("#teachers-content .consultation-main-content").style.display = "none";
    document.getElementById("teacherSearchSection").style.display = "block";
    document.getElementById("teacherArchiveSection").style.display = "none";
    document.getElementById("teacherAnalysisSection").style.display = "none";
}
function showTeacherArchiveSection() {
    document.querySelector("#teachers-content .consultation-main-content").style.display = "none";
    document.getElementById("teacherSearchSection").style.display = "none";
    document.getElementById("teacherArchiveSection").style.display = "block";
    document.getElementById("teacherAnalysisSection").style.display = "none";
}
function showTeacherAnalysisSection() {
    document.querySelector("#teachers-content .consultation-main-content").style.display = "none";
    document.getElementById("teacherSearchSection").style.display = "none";
    document.getElementById("teacherArchiveSection").style.display = "none";
    document.getElementById("teacherAnalysisSection").style.display = "block";
    document.getElementById("teacherAnalysisName").textContent =
        document.getElementById("selectedTeacherName").textContent + " : Статистика";
}