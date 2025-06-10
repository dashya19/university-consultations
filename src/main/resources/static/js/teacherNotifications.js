document.querySelectorAll('.mark-as-read-btn').forEach(button => {
    button.addEventListener('click', function() {
        const notificationId = this.getAttribute('data-notification-id');
        fetch('/teacher/notifications/mark-as-read', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            },
            body: JSON.stringify([parseInt(notificationId)])
        })
        .then(response => {
            if (response.ok) {
                this.closest('.notification').classList.add('read');
                this.closest('.notification').classList.remove('unread');
                this.remove();
                updateUnreadCount();
            }
        });
    });
});
document.getElementById('markAllAsReadBtn').addEventListener('click', function() {
    const notificationIds = Array.from(document.querySelectorAll('.unread'))
        .map(el => parseInt(el.querySelector('.mark-as-read-btn').getAttribute('data-notification-id')));
    if (notificationIds.length === 0) return;
    fetch('/teacher/notifications/mark-as-read', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
        },
        body: JSON.stringify(notificationIds)
    })
    .then(response => {
        if (response.ok) {
            document.querySelectorAll('.unread').forEach(el => {
                el.classList.add('read');
                el.classList.remove('unread');
                el.querySelector('.mark-as-read-btn').remove();
            });
            updateUnreadCount();
        }
    });
});

function updateUnreadCount() {
    fetch('/teacher/notifications/unread-count')
    .then(response => response.json())
    .then(data => {
        const badge = document.getElementById('unreadCountBadge');
        if (badge) { // Добавляем проверку на существование элемента
            if (data.count === 0) {
                badge.style.display = 'none';
            } else {
                badge.textContent = data.count;
                badge.style.display = 'block';
            }
        }
    });
}
document.querySelector('.bottom-box[data-tab="notifications"]').addEventListener('click', updateUnreadCount);