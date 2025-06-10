function toggleMaxStudents(checkbox, inputId) {
    const maxStudentsInput = document.getElementById(inputId);
    if (checkbox.checked) {
        maxStudentsInput.value = '10000';
        maxStudentsInput.readOnly = true;
        maxStudentsInput.style.backgroundColor = '#C0C0C0';
        maxStudentsInput.style.color = '#C0C0C0';
    } else {
        maxStudentsInput.value = '5';
        maxStudentsInput.readOnly = false;
        maxStudentsInput.style.backgroundColor = '';
        maxStudentsInput.style.color = '';
    }
}