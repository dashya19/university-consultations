function preValidateFile(input) {
    const file = input.files[0];
    if (!file) return;
    const validTypes = ['image/jpeg', 'image/png'];
    const maxSize = 2 * 1024 * 1024; // 2MB
    if (!validTypes.includes(file.type)) {
        alert('Пожалуйста, выберите файл в формате JPG, JPEG или PNG');
        input.value = '';
        return;
    }
    if (file.size > maxSize) {
        alert('Файл слишком большой. Максимальный размер - 2MB');
        input.value = '';
        return;
    }
    document.getElementById('uploadForm').submit();
}
function validateFile() {
    const input = document.getElementById('file-upload');
    return preValidateFile(input);
}

