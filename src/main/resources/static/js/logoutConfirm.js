document.addEventListener('DOMContentLoaded', function() {
    const logoutLinks = document.querySelectorAll('.dropdown-item[href="/logout"]');
    logoutLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            if (confirm('Вы уверены, что хотите выйти?')) {
                const form = document.createElement('form');
                form.method = 'POST';
                form.action = '/logout';
                const csrfInput = document.createElement('input');
                csrfInput.type = 'hidden';
                csrfInput.name = '_csrf';
                csrfInput.value = document.querySelector('input[name="_csrf"]').value;
                form.appendChild(csrfInput);
                document.body.appendChild(form);
                form.submit();
            }
        });
    });
});