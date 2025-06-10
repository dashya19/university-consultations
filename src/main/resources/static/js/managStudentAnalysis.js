document.addEventListener("DOMContentLoaded", () => {
    function loadStudentAnalysis() {
        if (!currentStudentId) {
            console.error('No student ID available');
            return;
        }
        fetch(`/api/students/${currentStudentId}/attendance-stats`)
            .then(handleResponse)
            .then(data => updateStudentAnalysisUI(data))
            .catch(handleAnalysisError);
        fetch(`/api/students/${currentStudentId}/debt-stats`)
            .then(handleResponse)
            .then(data => updateStudentDebtUI(data))
            .catch(handleAnalysisError);
    }
    function updateStudentAnalysisUI(data) {
        document.getElementById('totalConsultations').textContent = data.total;
        document.getElementById('attendedConsultations').textContent = data.attended;
        document.getElementById('notAttendedConsultations').textContent = data.notAttended;
        document.getElementById('attendancePercentage').textContent = data.attendancePercentage + '%';
        const ctx = document.getElementById('attendanceChart').getContext('2d');
        if (studentAttendanceChart) {
            studentAttendanceChart.destroy();
        }
        studentAttendanceChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['Посещаемость'],
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
                        text: 'Диаграмма посещаемости студента',
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
    function updateStudentDebtUI(data) {
        document.getElementById('totalDebts').textContent = data.total;
        document.getElementById('passedDebts').textContent = data.passed;
        document.getElementById('failedDebts').textContent = data.failed;
        document.getElementById('passedPercentage').textContent = data.passedPercentage + '%';
        const ctx = document.getElementById('debtChart').getContext('2d');
        if (studentDebtChart) {
            studentDebtChart.destroy();
        }
        studentDebtChart = new Chart(ctx, {
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
    document.getElementById("viewStudentAnalysisBtn")?.addEventListener("click", loadStudentAnalysis);
});