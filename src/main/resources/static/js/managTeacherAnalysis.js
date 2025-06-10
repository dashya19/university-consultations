document.addEventListener("DOMContentLoaded", () => {
    function loadTeacherAnalysis() {
        if (!currentTeacherId) {
            console.error('No teacher ID available');
            return;
        }
        fetch(`/api/teachers/${currentTeacherId}/attendance-stats`)
            .then(handleResponse)
            .then(data => updateTeacherAnalysisUI(data))
            .catch(handleAnalysisError);
        fetch(`/api/teachers/${currentTeacherId}/debt-stats`)
            .then(handleResponse)
            .then(data => updateTeacherDebtUI(data))
            .catch(handleAnalysisError);
    }
    function updateTeacherAnalysisUI(data) {
        document.getElementById('teacherTotalConsultations').textContent = data.total;
        document.getElementById('teacherAttendedConsultations').textContent = data.attended;
        document.getElementById('teacherNotAttendedConsultations').textContent = data.notAttended;
        document.getElementById('teacherAttendancePercentage').textContent = data.attendancePercentage + '%';
        const ctx = document.getElementById('teacherAttendanceChart').getContext('2d');
        if (teacherAttendanceChart) {
            teacherAttendanceChart.destroy();
        }
        teacherAttendanceChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Посещаемость студентов'],
                datasets: [
                    {
                        label: 'Посещено',
                        data: [data.attended],
                        backgroundColor: 'rgba(75, 192, 192, 0.6)',
                        borderColor: 'rgba(75, 192, 192, 1)',
                        borderWidth: 1
                    },
                    {
                        label: 'Пропущено',
                        data: [data.notAttended],
                        backgroundColor: 'rgba(255, 99, 132, 0.6)',
                        borderColor: 'rgba(255, 99, 132, 1)',
                        borderWidth: 1
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1,
                            precision: 0
                        },
                        title: {
                            display: true,
                            text: 'Количество консультаций'
                        }
                    }
                },
                plugins: {
                    title: {
                        display: true,
                        text: 'Диаграмма посещаемости студентов на консультациях',
                        font: {
                            size: 16
                        }
                    },
                    legend: {
                        position: 'top'
                    }
                }
            }
        });
    }
    function updateTeacherDebtUI(data) {
        document.getElementById('teacherTotalDebts').textContent = data.total;
        document.getElementById('teacherPassedDebts').textContent = data.passed;
        document.getElementById('teacherFailedDebts').textContent = data.failed;
        document.getElementById('teacherPassedPercentage').textContent = data.passedPercentage + '%';
        const ctx = document.getElementById('teacherDebtChart').getContext('2d');
        if (teacherDebtChart) {
            teacherDebtChart.destroy();
        }
        teacherDebtChart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: ['Сдано', 'Не сдано'],
                datasets: [{
                    data: [data.passed, data.failed],
                    backgroundColor: ['rgba(75, 192, 192, 0.6)', 'rgba(255, 99, 132, 0.6)'],
                    borderColor: ['rgba(75, 192, 192, 1)', 'rgba(255, 99, 132, 1)'],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: true,
                        text: 'Диаграмма сданных/несданных задолженностей',
                        font: { size: 16 },
                        position: 'top',
                        align: 'center',
                        padding: {
                            bottom: 10
                        }
                    },
                    legend: {
                        position: 'top',
                        align: 'center',
                        labels: {
                            boxWidth: 12,
                            padding: 20,
                            font: {
                                size: 14
                            }
                        },
                        title: {
                            display: false
                        }
                    }
                },
                layout: {
                    padding: {
                        top: 20
                    }
                }
            }
        });
    }
    document.getElementById("viewTeacherAnalysisBtn")?.addEventListener("click", loadTeacherAnalysis);
});