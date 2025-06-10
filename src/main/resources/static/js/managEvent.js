document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("findStudentBtn")?.addEventListener("click", showSearchSection);
    document.getElementById("backToProfileFromSearch")?.addEventListener("click", showMainContent);
    document.getElementById("backToProfileFromArchive")?.addEventListener("click", showMainContent);
    document.getElementById("backToSearchFromArchive")?.addEventListener("click", showSearchSection);
    document.getElementById("backToProfileFromAnalysis")?.addEventListener("click", showMainContent);
    document.getElementById("backToSearchFromAnalysis")?.addEventListener("click", showSearchSection);
    document.getElementById("backToArchiveFromAnalysis")?.addEventListener("click", showArchiveSection);
    document.getElementById("viewStudentAnalysisBtn")?.addEventListener("click", showStudentAnalysisSection);

    document.getElementById("findTeacherBtn")?.addEventListener("click", showTeacherSearchSection);
    document.getElementById("backToProfileFromTeacherSearch")?.addEventListener("click", showTeacherMainContent);
    document.getElementById("backToProfileFromTeacherArchive")?.addEventListener("click", showTeacherMainContent);
    document.getElementById("backToTeacherSearchFromArchive")?.addEventListener("click", showTeacherSearchSection);
    document.getElementById("backToProfileFromTeacherAnalysis")?.addEventListener("click", showTeacherMainContent);
    document.getElementById("backToTeacherSearchFromAnalysis")?.addEventListener("click", showTeacherSearchSection);
    document.getElementById("backToTeacherArchiveFromAnalysis")?.addEventListener("click", showTeacherArchiveSection);
    document.getElementById("viewTeacherAnalysisBtn")?.addEventListener("click", showTeacherAnalysisSection);
});