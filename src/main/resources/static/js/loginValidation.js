document.querySelector('form').addEventListener('submit', function(event) {
    const loginInput = document.getElementById('login');
    const passwordInput = document.getElementById('password');
    const loginError = document.getElementById('login-error');
    const passwordError = document.getElementById('password-error');
    let isValid = true;
    if (!loginInput.value.trim()) {
        loginError.textContent = 'Пожалуйста, введите логин';
        isValid = false;
    } else {
        loginError.textContent = '';
    }
    if (!passwordInput.value.trim()) {
        passwordError.textContent = 'Пожалуйста, введите пароль';
        isValid = false;
    } else {
        passwordError.textContent = '';
    }
    if (!isValid) {
        event.preventDefault();
    }
});