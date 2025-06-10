function applyPhoneMask(input) {
    let value = input.value;
    let numbers = value.replace(/\D/g, '');
    if (!numbers.startsWith('8') && numbers.length > 0) {
        numbers = '8' + numbers;
    }
    if (numbers.length > 11) {
        numbers = numbers.substring(0, 11);
    }
    let formatted = '';
    if (numbers.length > 0) {
        formatted = '8';
        if (numbers.length > 1) {
            formatted += ' (' + numbers.substring(1, 4);
        }
        if (numbers.length > 4) {
            formatted += ') ' + numbers.substring(4, 7);
        }
        if (numbers.length > 7) {
            formatted += '-' + numbers.substring(7, 9);
        }
        if (numbers.length > 9) {
            formatted += '-' + numbers.substring(9, 11);
        }
    }
    input.value = formatted;
}
function showEditModal(type) {
    const modal = document.getElementById('editFieldModal');
    const fieldLabel = document.getElementById('fieldLabel');
    const fieldInput = document.getElementById('fieldInput');
    const fieldType = document.getElementById('fieldType');
    const currentValue = event.currentTarget.getAttribute('data-current-value') || '';
    if (type === 'email') {
        fieldLabel.textContent = 'Введите новый email:';
        fieldInput.placeholder = 'example@domain.com';
        fieldInput.value = currentValue; // Устанавливаем текущее значение
        fieldInput.type = 'email';
        fieldType.value = 'email';
        fieldInput.removeEventListener('input', phoneInputHandler);
        fieldInput.removeEventListener('keydown', phoneKeydownHandler);
    }
    else if (type === 'phone') {
        fieldLabel.textContent = 'Введите номер телефона:';
        fieldInput.placeholder = '8 (XXX) XXX-XX-XX';
        if (currentValue) {
            fieldInput.value = formatPhoneNumber(currentValue);
        } else {
            fieldInput.value = '8';
        }
        fieldInput.type = 'tel';
        fieldType.value = 'phone';
        fieldInput.addEventListener('input', phoneInputHandler);
        fieldInput.addEventListener('keydown', phoneKeydownHandler);
        setTimeout(() => {
            fieldInput.selectionStart = fieldInput.value.length;
            fieldInput.selectionEnd = fieldInput.value.length;
        }, 0);
    }
    modal.style.display = 'block';
    fieldInput.focus();
}
function phoneInputHandler() {
    applyPhoneMask(this);
}
function phoneKeydownHandler(e) {
    if (e.key === 'Backspace' && this.selectionStart <= 1) {
        e.preventDefault();
    }
    if (e.key.length === 1 && !/\d/.test(e.key)) {
        e.preventDefault();
    }
}
function closeFieldModal() {
    const fieldInput = document.getElementById('fieldInput');
    fieldInput.removeEventListener('input', phoneInputHandler);
    fieldInput.removeEventListener('keydown', phoneKeydownHandler);
    document.getElementById('editFieldModal').style.display = 'none';
}
document.getElementById('editFieldForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const fieldType = document.getElementById('fieldType').value;
    const fieldInput = document.getElementById('fieldInput');
    const form = this;
    if (fieldType === 'phone') {
        const formattedValue = fieldInput.value;
        let phoneValue = formattedValue.replace(/\D/g, '');
        if (!phoneValue.startsWith('8') && phoneValue.length > 0) {
            phoneValue = '8' + phoneValue;
        }
        if (phoneValue.length !== 11) {
            alert('Номер телефона должен содержать 11 цифр (включая 8 в начале)');
            setTimeout(() => {
                fieldInput.value = formattedValue;
                fieldInput.selectionStart = fieldInput.value.length;
                fieldInput.selectionEnd = fieldInput.value.length;
            }, 0);
            return false;
        }
        fieldInput.value = phoneValue;
    }
    fetch(form.action, {
        method: 'POST',
        body: new FormData(form),
        headers: {
            'Accept': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            closeFieldModal();
            alert(data.success);
            window.location.reload();
        } else if (data.error) {
            if (fieldType === 'phone') {
                const phoneValue = fieldInput.value;
                fieldInput.value = formatPhoneNumber(phoneValue);
            }
            alert(data.error);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Произошла ошибка при обновлении данных');
    });
});
function formatPhoneNumber(phone) {
    if (!phone) return '';
    let numbers = phone.replace(/\D/g, '');
    if (!numbers.startsWith('8') && numbers.length > 0) {
        numbers = '8' + numbers;
    }
    let formatted = '8';
    if (numbers.length > 1) formatted += ' (' + numbers.substring(1, 4);
    if (numbers.length > 4) formatted += ') ' + numbers.substring(4, 7);
    if (numbers.length > 7) formatted += '-' + numbers.substring(7, 9);
    if (numbers.length > 9) formatted += '-' + numbers.substring(9, 11);
    return formatted;
}
window.onclick = function(event) {
    const fieldModal = document.getElementById('editFieldModal');
    if (event.target === fieldModal) {
        closeFieldModal();
    }
}