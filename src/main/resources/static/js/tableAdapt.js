function adaptAllTablesForMobile() {
    if (window.innerWidth <= 768) {
        document.querySelectorAll('.consultations-table').forEach(table => {
            const headers = Array.from(table.querySelectorAll('thead th')).map(th => th.textContent);
            table.querySelectorAll('tbody td').forEach((cell, index) => {
                cell.setAttribute('data-label', headers[index % headers.length]);
            });
        });
        const consultationTable = document.querySelector('#consultation-content .consultations-table');
        if (consultationTable) {
            const headers = Array.from(consultationTable.querySelectorAll('thead th')).map(th => th.textContent);
            const cells = consultationTable.querySelectorAll('tbody td');
            cells.forEach((cell, index) => {
                const headerIndex = index % headers.length;
                cell.setAttribute('data-label', headers[headerIndex]);
            });
        }
        const recordsTable = document.querySelector('#records-content .consultations-table');
        if (recordsTable) {
            const headers = Array.from(recordsTable.querySelectorAll('thead th')).map(th => th.textContent);
            const cells = recordsTable.querySelectorAll('tbody td');
            cells.forEach((cell, index) => {
                const headerIndex = index % headers.length;
                cell.setAttribute('data-label', headers[headerIndex]);
            });
        }
    }
}
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(adaptAllTablesForMobile, 100);
    window.addEventListener('resize', adaptAllTablesForMobile);
});